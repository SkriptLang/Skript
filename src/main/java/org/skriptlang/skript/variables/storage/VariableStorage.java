package org.skriptlang.skript.variables.storage;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.log.ParseLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.variables.SerializedVariable;
import ch.njol.skript.variables.Variables;
import com.google.errorprone.annotations.ThreadSafe;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.jetbrains.annotations.Unmodifiable;
import org.skriptlang.skript.addon.SkriptAddon;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Saves global variables somewhere where they survive a restart, such as a file or a database.
 * <p>
 * A storage does not hold variables. Skript keeps global variables in memory and serves all reads from there,
 * so a storage is only read once, at startup, and after that only receives the changes it has to save.
 * Which storage a variable is saved to is decided by the storages' name prefixes in the config:
 * the first storage with a prefix the name starts with saves it.
 * <p>
 * The lifecycle of a storage, in order:
 * <ol>
 *     <li><b>Configure:</b> Skript reads the storage's config section and calls {@link #configure(SectionNode)}.
 *     If it returns false, the storage is not used.</li>
 *     <li><b>Load:</b> {@link #load(LoadListener)} reads every stored variable and passes it to Skript.
 *     If it throws, Skript does not save to this storage, so that data which could not be read is not overwritten.</li>
 *     <li><b>Save:</b> while the server runs, changes to the storage's variables are queued for it.
 *     The storage's own saving thread drains the queue about every {@link #saveIntervalMillis()} ms
 *     and passes the changes to {@link #save(Batch)}. A batch that fails to save is retried, with backoff,
 *     before anything newer is drained. If the storage {@link #supportsBackups() supports backups} and the config
 *     sets a {@code backup interval}, the saving thread also writes a backup every interval.</li>
 *     A storage whose data can also be changed from outside this server reports those changes with
 *     {@link #reportUpdated} and {@link #reportCleared}.</li>
 *     <li><b>Stop:</b> on shutdown, the saving thread is stopped and what is left is saved one last time.</li>
 *     <li><b>Close:</b> {@link #close()} releases the storage's resources.</li>
 * </ol>
 * <p>
 * How a change flows: Skript's in-memory variables are for reading, the queue is for writing,
 * and the batch is for saving. Setting a variable changes it in memory and queues the serialized change
 * in the same step, keeping only the last change to each name. The saving thread swaps the queue for an empty one
 * and saves its contents as one {@link Batch}, so writes never wait on I/O.
 * <p>
 * Threads: Skript calls {@link #configure(SectionNode)}, {@link #load(LoadListener)} and {@link #close()}
 * once each and never concurrently. Changes are queued from any thread, and {@link #save(Batch)} is only ever
 * called by one thread at a time, never the one that made the change. {@link #flush()} may be called from any
 * thread, but blocks it until the save is done.
 * <p>
 * Storages are registered with {@code Variables.registerStorage} under one or more type names,
 * which users then pick with the {@code type} option of a storage's config section.
 */
@ThreadSafe
public abstract class VariableStorage implements Closeable {

	/**
	 * The files currently used by a storage, so that two storages can't write to the same file.
	 */
	private static final Set<File> registeredFiles = ConcurrentHashMap.newKeySet();

	// core id data
	private final SkriptAddon registeringAddon;
	private final String type;

	// config stuff
	private String name;
	private String @Nullable [] prefixes; // null accepts every name
	private @Nullable File file;
	private boolean ownsFile = false;

	// set by the manager if loading failed: the storage keeps its names, but nothing is saved to it.
	// written before the storages are published, so it needs no volatile
	boolean loadFailed;

	// the changes waiting to be saved
	private final ChangeQueue queue = new ChangeQueue();

	// saving thread stuff
	private final ReentrantLock saveLock = new ReentrantLock();
	private @Nullable Batch failedBatch; // a batch whose save failed and needs to be retried before draining again. guarded by saveLock.
	private int failures;                // number of saves failed in a row. guarded by saveLock.
	private long intervalNanos;          // read once from saveIntervalMillis() in startSaving
	private volatile boolean saving;     // whether the save loop should be running
	private volatile @Nullable Thread saveThread;
	private static final long STOP_TIMEOUT_MILLIS = 30_000; // how long stopSaving() waits for the saving thread
	private static final int FINAL_SAVE_ATTEMPTS = 3;        // how often stopSaving() tries to save what is left

	// backup stuff
	private long backupIntervalNanos; // 0 means backups are off. Set while configuring.
	private int backupsToKeep = -1;   // -1 keeps every backup. Set while configuring.
	private @Nullable StorageBackups backups; // created in startSaving. guarded by saveLock.

	protected VariableStorage(SkriptAddon registeringAddon, String type) {
		this.registeringAddon = registeringAddon;
		this.type = type;
	}

	// ---------------------------- CONFIGURATION ----------------------------

	/**
	 * Reads the configuration of this storage from the given node.
	 * Do not do any reading of stored data here; that is done in {@link #load(LoadListener)}.
	 * Log issues with {@link Skript#error(String)} and return false if the configuration is invalid.
	 *
	 * @param node the configuration node
	 * @return true if the configuration was valid and the storage is ready to be used, false if it was invalid and the storage should be disabled
	 */
	protected abstract boolean configure(SectionNode node);

	//<editor-fold desc="config helpers">
	/**
	 * Loads the configuration of this storage from the given node and sets its name.
	 * Calls {@link #configure(SectionNode)} to do the actual configuration.
	 *
	 * @param node the configuration node to use
	 * @return true if the configuration was valid and the storage is ready to be used, false if it was invalid and the storage should be disabled
	 */
	final boolean loadConfig(SectionNode node) {
		name = node.getKey();

		if (!loadPrefix(node))
			return false;

		// read file if needed
		if (requiresFile()) {
			String fileName = getConfigValue(node, "file");
			if (fileName == null)
				return false;
			if (!setUpFile(getFile(fileName)))
				return false;
		}

		if (!loadBackupConfig(node)) {
			releaseFile();
			return false;
		}

		boolean success = configure(node);
		if (!success)
			releaseFile();
		return success;
	}

	/**
	 * Reads the {@code prefix} option, or translates the old {@code pattern} option if there is no prefix.
	 * If neither exists, the storage matches every variable name.
	 * Config files are updated from {@code pattern} to {@code prefix} once, when Skript updates the config,
	 * so a {@code pattern} is only left here if that update was skipped or the pattern couldn't be translated.
	 *
	 * @param node the configuration node
	 * @return false if the options are invalid; an error is printed in that case
	 */
	private boolean loadPrefix(SectionNode node) {
		String configuredPrefix = node.getValue("prefix");
		String configuredPattern = node.getValue("pattern");

		List<String> translated = null;
		if (configuredPattern != null) {
			translated = patternToPrefixes(configuredPattern);
			if (translated == null) {
				Skript.error("The 'pattern' of the database '" + getName() + "' is '" + configuredPattern + "', "
					+ "which must be replaced with a 'prefix' entry instead. Databases now pick their variables "
					+ "by the start of their names only, such as 'prefix: kills::' or 'prefix: a_, b_'.");
				return false;
			}
		}

		if (configuredPrefix != null && translated != null
			&& !Objects.equals(asSet(normalizePrefixes(splitPrefixes(configuredPrefix))), asSet(normalizePrefixes(translated)))) {
			Skript.error("The database '" + getName() + "' has both a 'prefix' ('" + configuredPrefix + "') "
				+ "and a 'pattern' ('" + configuredPattern + "') that don't match. Remove the 'pattern'.");
			return false;
		}

		if (configuredPrefix != null) {
			prefixes = normalizePrefixes(splitPrefixes(configuredPrefix));
		} else {
			prefixes = translated == null ? null : normalizePrefixes(translated);
		}
		return true;
	}

	/**
	 * Splits a configured {@code prefix} value into its prefixes, which are separated by commas.
	 *
	 * @param configured the configured value, such as {@code bank::, tax::}
	 * @return the prefixes, trimmed, without empty ones
	 */
	private static List<String> splitPrefixes(String configured) {
		List<String> split = new ArrayList<>();
		for (String part : configured.split(",")) {
			String trimmed = part.trim();
			if (!trimmed.isEmpty())
				split.add(trimmed);
		}
		return split;
	}

	/**
	 * @param configured prefixes from the config or a translated pattern
	 * @return the prefixes to match names with, without duplicates, lowercased if variables are case-insensitive
	 * since their names are lowercased too. Null if every name is accepted: when there are no prefixes, or one of them
	 * is empty, {@code .*} or {@code .+}.
	 */
	private static String @Nullable [] normalizePrefixes(List<String> configured) {
		Set<String> normalized = new LinkedHashSet<>();
		for (String prefix : configured) {
			if (prefix.isEmpty() || prefix.equals(".*") || prefix.equals(".+"))
				return null; // accepts every name
			normalized.add(Variables.caseInsensitiveVariables ? prefix.toLowerCase(Locale.ENGLISH) : prefix);
		}
		return normalized.isEmpty() ? null : normalized.toArray(new String[0]);
	}

	private static @Nullable Set<String> asSet(String @Nullable [] prefixes) {
		return prefixes == null ? null : Set.of(prefixes);
	}

	/**
	 * Translates an old regex {@code pattern} into prefixes, if the pattern only matched by fixed starts.
	 * <ul>
	 *     <li>{@code .*}, {@code .+} and {@code (?!-).*} matched every name ({@code -} names aren't saved anyway),
	 *     so they become no prefixes, which accepts every name.</li>
	 *     <li>A literal followed by {@code .*} or {@code .+} becomes that literal.
	 *     Backslash escapes like {@code \.} are resolved.</li>
	 *     <li>A group of literal alternatives followed by {@code .*} or {@code .+}, such as {@code (bank|tax).*},
	 *     becomes one prefix per alternative.</li>
	 *     <li>Both may start with {@code (?i)}.</li>
	 *     <li>Anything else, such as {@code (?!x_).*}, {@code db[0-9]::.*} or {@code kills::(a|b).*},
	 *     can't be translated.</li>
	 * </ul>
	 *
	 * @param pattern the old pattern
	 * @return the prefixes, empty for every name, or null if the pattern can't be translated
	 */
	static @Nullable List<String> patternToPrefixes(String pattern) {
		String rest = pattern.trim();
		if (rest.equals(".*") || rest.equals(".+") || rest.equals("(?!-).*") || rest.equals("(?!-).+"))
			return List.of();
		if (rest.startsWith("(?i)"))
			rest = rest.substring(4);
		if (!rest.endsWith(".*") && !rest.endsWith(".+"))
			return null;
		rest = rest.substring(0, rest.length() - 2);

		List<String> alternatives;
		if (rest.startsWith("(") && rest.endsWith(")") && !rest.endsWith("\\)")) {
			String group = rest.substring(rest.startsWith("(?:") ? 3 : 1, rest.length() - 1);
			alternatives = splitAlternatives(group);
		} else {
			alternatives = List.of(rest);
		}

		List<String> prefixes = new ArrayList<>(alternatives.size());
		for (String alternative : alternatives) {
			String literal = regexLiteral(alternative);
			if (literal == null)
				return null;
			if (literal.isEmpty())
				return List.of(); // an empty alternative matched every name
			prefixes.add(literal);
		}
		return prefixes;
	}

	/**
	 * Splits a group's contents at its unescaped {@code |}s, keeping escapes for {@link #regexLiteral(String)}.
	 */
	private static List<String> splitAlternatives(String group) {
		List<String> alternatives = new ArrayList<>();
		StringBuilder current = new StringBuilder();
		for (int i = 0; i < group.length(); i++) {
			char c = group.charAt(i);
			if (c == '\\' && i + 1 < group.length()) {
				current.append(c).append(group.charAt(++i));
			} else if (c == '|') {
				alternatives.add(current.toString());
				current.setLength(0);
			} else {
				current.append(c);
			}
		}
		alternatives.add(current.toString());
		return alternatives;
	}

	/**
	 * @param regex part of a pattern
	 * @return the text it matches, if it only matches that one text, otherwise null
	 */
	private static @Nullable String regexLiteral(String regex) {
		StringBuilder literal = new StringBuilder(regex.length());
		for (int i = 0; i < regex.length(); i++) {
			char c = regex.charAt(i);
			if (c == '\\') {
				if (++i == regex.length())
					return null; // a lone backslash at the end
				char escaped = regex.charAt(i);
				if (Character.isLetterOrDigit(escaped))
					return null; // \d, \w, \Q, back references and the like aren't literals
				literal.append(escaped);
			} else if ("[](){}|?+*^$.".indexOf(c) >= 0) {
				return null; // has regex syntax, so it isn't just a literal
			} else {
				literal.append(c);
			}
		}
		return literal.toString();
	}

	/**
	 * Reads the {@code backup interval} and {@code backups to keep} options. Both are optional:
	 * a missing or zero interval turns backups off, and a missing keep count keeps every backup.
	 * A keep count of 0 also turns backups off, and deletes this storage's old backups when saving starts.
	 *
	 * @param node the configuration node
	 * @return false if an option is invalid; an error is printed in that case
	 */
	private boolean loadBackupConfig(SectionNode node) {
		long intervalMillis = 0;
		String rawInterval = node.getValue("backup interval");
		if (rawInterval != null && !rawInterval.trim().equals("0")) { // a plain 0 isn't a valid timespan
			Timespan interval = parseConfigValue(rawInterval, "backup interval", Timespan.class);
			if (interval == null)
				return false;
			intervalMillis = interval.getAs(Timespan.TimePeriod.MILLISECOND);
		}

		Integer toKeep = getConfigOptional(node, "backups to keep", Integer.class, -1);
		if (toKeep == null)
			return false;
		if (toKeep < -1) {
			Skript.error("The entry for 'backups to keep' in the database '" + getName() + "' must be -1 or more");
			return false;
		}

		if (intervalMillis > 0 && toKeep != 0 && !supportsBackups()) {
			Skript.warning("The database '" + getName() + "' has a backup interval, but databases of the type '"
				+ type + "' can't make backups, so none will be made");
			intervalMillis = 0;
		}
		setBackupOptions(intervalMillis, toKeep);
		return true;
	}

	/**
	 * Sets how often this storage is backed up and how many backups are kept. Called while configuring.
	 * Package-private so tests can set the options without a config.
	 *
	 * @param intervalMillis the time between backups, or 0 for no backups
	 * @param toKeep         how many backups to keep, -1 to keep all of them,
	 *                       or 0 for no backups and to delete the old ones when saving starts
	 */
	void setBackupOptions(long intervalMillis, int toKeep) {
		backupsToKeep = toKeep;
		backupIntervalNanos = toKeep == 0 ? 0 : TimeUnit.MILLISECONDS.toNanos(intervalMillis);
	}

	/**
	 * Creates the given file if needed, checks it can be used, and registers it to this storage.
	 * Prints an error and leaves {@link #file} unset if anything fails.
	 *
	 * @param configured the file as resolved by {@link #getFile(String)}
	 * @return whether the file can be used by this storage
	 */
	private boolean setUpFile(File configured) {
		String prefix = "The file '" + configured + "' of the database '" + getName() + "' ";
		if (configured.exists() && !configured.isFile()) {
			Skript.error(prefix + "must be a file, not a directory");
			return false;
		}

		File canonical;
		try {
			File parent = configured.getAbsoluteFile().getParentFile();
			if (parent != null)
				Files.createDirectories(parent.toPath());
			configured.createNewFile(); // does nothing if it already exists
			// canonicalise only once the file exists, so that its real case is used on case-insensitive file systems
			canonical = configured.getCanonicalFile();
		} catch (IOException e) {
			Skript.error(prefix + "could not be created: " + e.getLocalizedMessage());
			return false;
		}

		if (!canonical.canRead()) {
			Skript.error(prefix + "is not readable");
			return false;
		}
		if (!canonical.canWrite()) {
			Skript.error(prefix + "is not writable");
			return false;
		}
		if (!registeredFiles.add(canonical)) {
			Skript.error(prefix + "is already used by another database");
			return false;
		}
		file = canonical;
		ownsFile = true;
		return true;
	}

	/**
	 * Releases this storage's file, so that another storage can use it.
	 * Called by Skript after {@link #close()}, and when configuration fails. Does nothing if called again.
	 */
	void releaseFile() {
		if (ownsFile) {
			registeredFiles.remove(file);
			ownsFile = false;
		}
	}

	/**
	 * @return the file this storage uses, or null if it {@link #requiresFile() doesn't require one}
	 * or it has not been configured yet
	 */
	protected final @Nullable File getStorageFile() {
		return file;
	}

	/**
	 * Gets the required string value of a key in a configuration node.
	 *
	 * @param node the configuration node
	 * @param key  the key to get
	 * @return the string value of the key, or null if it is missing; an error is printed in that case
	 */
	protected final @Nullable String getConfigValue(SectionNode node, String key) {
		return getConfigValue(node, key, String.class);
	}

	/**
	 * Gets the required value of a key in a configuration node, parsed as the given type.
	 *
	 * @param node the configuration node
	 * @param key  the key to get
	 * @param type the type of the value to get
	 * @param <T>  the type of the value to get
	 * @return the value of the key, or null if it is missing or invalid. An error is printed in both cases
	 */
	protected final <T> @Nullable T getConfigValue(SectionNode node, String key, Class<T> type) {
		String raw = node.getValue(key);
		if (raw == null) {
			Skript.error("The config is missing the entry for '" + key + "' in the database '" + getName() + "'");
			return null;
		}
		return parseConfigValue(raw, key, type);
	}

	/**
	 * Gets the optional value of a key in a configuration node, parsed as the given type.
	 * A missing key is not an error, but an invalid value is.
	 *
	 * @param node         the configuration node
	 * @param key          the key to get
	 * @param type         the type of the value to get
	 * @param defaultValue the value to return if the key is missing
	 * @param <T>          the type of the value to get
	 * @return the value of the key, {@code defaultValue} if it is missing,
	 * or null if it is invalid; an error is printed in that case
	 */
	protected final <T> @Nullable T getConfigOptional(SectionNode node, String key, Class<T> type, T defaultValue) {
		String raw = node.getValue(key);
		if (raw == null)
			return defaultValue;
		return parseConfigValue(raw, key, type);
	}

	/**
	 * Parses a raw configuration value as the given type, printing an error if it is invalid.
	 */
	private <T> @Nullable T parseConfigValue(String raw, String key, Class<T> type) {
		try (ParseLogHandler log = SkriptLogger.startParseLogHandler()) {
			T value = Classes.parse(raw, type, ParseContext.CONFIG);
			if (value == null) {
				log.printError("The entry for '" + key + "' in the database '" + getName() + "' must be "
					+ Classes.getSuperClassInfo(type).getName().withIndefiniteArticle());
			} else {
				log.printLog();
			}
			return value;
		}
	}
	//</editor-fold>

	// ---------------------------- LIFECYCLE ----------------------------

	/**
	 * Initially loads the variables from this storage and passes them to Skript. Called once after configuration.
	 * This is allowed to block for as long as necessary, though as always, faster is better.
	 * Any variable loaded should be passed to {@link LoadListener#loaded(String, SerializedVariable.Value)} as it is loaded.
	 *
	 * @param listener the listener to pass the variables to; only valid until this returns, so don't keep it
	 * @throws IOException if the stored data could not be read completely
	 */
	protected abstract void load(LoadListener listener) throws IOException;

	/**
	 * Saves a batch of changes. Called on this storage's saving thread, one batch at a time, in order.
	 * <p>
	 * Apply {@link Batch#clears()} first, then {@link Batch#changes()}.
	 * When this returns, the batch must be stored durably, at least as far as the operating system:
	 * a crash of the server process after it returns must not lose the batch.
	 * <p>
	 * Throw if the batch could not be saved. Skript keeps it and calls this again with the same batch later,
	 * so saving the same batch twice must be harmless.
	 *
	 * @param batch the changes to save, never empty
	 * @throws IOException if the batch could not be saved
	 */
	protected abstract void save(Batch batch) throws IOException;

	/**
	 * How often queued changes are saved, in milliseconds. Not user configurable by default, since storage performance depends on
	 * ensuring drains are steady and relatively often.
	 * A storage class may override this, e.g. to save more often when other servers wait on its writes.
	 *
	 * @return the time between saves. This is only read once, when saving starts, to set up thread timings.
	 */
	protected long saveIntervalMillis() {
		return 1000;
	}

	/**
	 * Whether this storage can write backups with {@link #writeBackup(OutputStream)}.
	 * If false, a configured {@code backup interval} is ignored with a warning.
	 *
	 * @return true if {@link #writeBackup(OutputStream)} is implemented
	 */
	protected boolean supportsBackups() {
		return false;
	}

	/**
	 * Writes a full copy of the stored variables to {@code out}, as CSV.
	 * Only called if {@link #supportsBackups()} returns true.
	 * <p>
	 * Called on the saving thread, right after a successful save, and nothing is saved while it runs,
	 * so the stored data is complete and doesn't change. Skript handles the backup file itself:
	 * it writes to a temporary file, compresses it if the config says so, and only keeps it if this returns.
	 *
	 * @param out where to write the backup; don't close it
	 * @throws IOException if the backup could not be written
	 */
	protected void writeBackup(OutputStream out) throws IOException {
		throw new UnsupportedOperationException(getClass().getName() + " does not support backups");
	}

	/**
	 * Saves every change queued so far, on the calling thread, without waiting for the saving thread's next cycle.
	 * A batch that failed to save before is retried right away, ignoring the backoff.
	 * <p>
	 * Blocks until the save is done, which can take a while for a remote database,
	 * so don't call it from the main thread outside of shutdown.
	 *
	 * @return true if every change queued before the call is now saved, false if a save failed
	 */
	public final boolean flush() {
		saveLock.lock();
		try {
			saveCycle();
			return failedBatch == null;
		} finally {
			saveLock.unlock();
		}
	}

	/**
	 * Releases any resources this storage holds, such as files or connections.
	 * After this method is called, the storage is not used again.
	 * <p>
	 * Saving has already stopped when this is called, and every change that could be saved has been passed to
	 * {@link #save(Batch)}, so this must not save anything itself.
	 * Called when Skript is shutting down, or if {@link #load(LoadListener)} fails.
	 */
	@Override
	public abstract void close();

	// ------------------------ EXTERNAL CHANGES ------------------------

	/**
	 * Applies the changes a storage reports with {@link #reportUpdated} and {@link #reportCleared}.
	 * Implemented by the manager, which is the only one that sets it.
	 */
	interface ChangeHandler {

		void updated(VariableStorage source, String name, SerializedVariable.@Nullable Value value);

		void cleared(VariableStorage source, String listName);

	}

	// used once a storage has stopped, so late reports from its threads are dropped instead of failing
	private static final ChangeHandler STOPPED = new ChangeHandler() {
		@Override
		public void updated(VariableStorage source, String name, SerializedVariable.@Nullable Value value) {
		}

		@Override
		public void cleared(VariableStorage source, String listName) {
		}
	};

	// null until the storage is running
	private volatile @Nullable ChangeHandler changeHandler;

	/**
	 * Tells Skript that a variable in this storage was changed from outside this server,
	 * for example by another server writing to the same SQL database.
	 * Skript updates the variable and drops any change to it that this server has not saved yet,
	 * so the stored value is kept. The change is never passed back to any storage,
	 * so the storage must already hold it.
	 * <p>
	 * May be called from any thread, but only once loading has finished and the storage is running.
	 * Ignored once the storage has stopped. Skript deserializes the value on the main thread.
	 *
	 * @param name  the name of the variable
	 * @param value the new serialized value of the variable, or {@code null} if the variable was deleted
	 * @throws IllegalStateException    if the storage isn't running yet
	 * @throws IllegalArgumentException if {@code name} is a list, such as {@code a::*}
	 */
	protected final void reportUpdated(String name, SerializedVariable.@Nullable Value value) {
		if (name.endsWith(Variable.SEPARATOR + "*"))
			throw new IllegalArgumentException("Can't update a list as a single variable: " + name);
		handler().updated(this, name, value);
	}

	/**
	 * Tells Skript that a list in this storage was deleted from outside this server.
	 * Skript deletes the list's variables that this storage holds, including those in nested lists,
	 * and drops any changes to them that this server has not saved yet.
	 * A variable with the list's name minus the {@code ::*}, such as {@code a} for {@code a::*}, is not affected.
	 * <p>
	 * May be called from any thread, but only once loading has finished and the storage is running.
	 * Ignored once the storage has stopped.
	 *
	 * @param listName the full name of the list, including the {@code ::*} ending, such as {@code a::*}
	 * @throws IllegalStateException    if the storage isn't running yet
	 * @throws IllegalArgumentException if {@code listName} doesn't end with {@code ::*}
	 */
	protected final void reportCleared(String listName) {
		if (!listName.endsWith(Variable.SEPARATOR + "*"))
			throw new IllegalArgumentException("Not a list name: " + listName);
		handler().cleared(this, listName);
	}

	private ChangeHandler handler() {
		ChangeHandler handler = changeHandler;
		if (handler == null)
			throw new IllegalStateException("The database '" + getName() + "' reported a change before it was running");
		return handler;
	}

	/**
	 * Starts passing reported changes to the given handler. Called once by the manager, when the storage is running.
	 *
	 * @throws IllegalStateException if it was already called
	 */
	void startReceiving(ChangeHandler handler) {
		if (changeHandler != null)
			throw new IllegalStateException("The database '" + getName() + "' already receives changes");
		changeHandler = handler;
	}

	/**
	 * Stops passing reported changes on: any reported from now on are ignored.
	 * Called by {@link #stopSaving()}, and by the manager if the storage fails.
	 */
	void stopReceiving() {
		changeHandler = STOPPED;
	}

	// ------------------------ CHANGE QUEUE ------------------------

	//<editor-fold desc="change queue">
	/**
	 * A batch of changes to a storage's variables, to be saved together.
	 * <p>
	 * Apply {@link #clears()} first, then {@link #changes()}: every change in a batch was made after
	 * every clear in it that covers its name, since changes made before a clear are dropped when draining.
	 *
	 * @param clears  the list prefixes to delete, such as {@code a::} for {@code {a::*}}, in the order they were cleared
	 * @param changes the last change to each variable; a {@code null} value means the variable was deleted
	 */
	public record Batch(List<String> clears, Collection<SerializedVariable> changes) {

		static final Batch EMPTY = new Batch(List.of(), List.of());

		public boolean isEmpty() {
			return clears.isEmpty() && changes.isEmpty();
		}

	}

	/**
	 * @return the queue of changes waiting to be saved to this storage, which the router writes to
	 */
	ChangeQueue queue() {
		return queue;
	}
	//</editor-fold>

	// ---------------------------- SAVING ----------------------------

	//<editor-fold desc="saving thread logic">

	/**
	 * Starts the saving thread, which calls {@link ChangeQueue#drain()} and {@link #save(Batch)} on its own schedule.
	 * Called once by the router after {@link #load(LoadListener)} succeeds. Storages must not call this themselves.
	 * If {@link #load(LoadListener)} fails, this should never be called and the storage is closed.
	 */
	void startSaving() {
		if (saveThread != null)
			throw new IllegalStateException("Saving thread already started");
		long millis = saveIntervalMillis();
		if (millis <= 0)
			throw new IllegalStateException(getClass().getName() + " returned a save interval of " + millis + " ms");
		intervalNanos = millis * 1_000_000L;
		if (backupIntervalNanos > 0 || backupsToKeep == 0) { // keeping none still deletes the old ones
			saveLock.lock();
			try {
				backups = new StorageBackups(getName(), backupFolder(), compressBackups(), backupIntervalNanos,
					backupsToKeep, System.nanoTime(), Clock.systemDefaultZone(), this::writeBackup, this::reportSaveFailure);
				backups.start(); // deletes old backups if none are to be kept
			} finally {
				saveLock.unlock();
			}
		}
		saving = true;
		saveThread = new Thread(this::saveLoop, "Skript variable storage " + getName() + " saving thread");
		saveThread.setDaemon(true);
		saveThread.start();
	}

	/**
	 * Stops the saving thread, then saves what is left on the calling thread.
	 * Called once by the router on shutdown, before {@link #close()}.
	 * <p>
	 * The router must stop sending changes to this storage before calling this:
	 * a change queued after the final save is lost.
	 * <p>
	 * Waits up to {@value #STOP_TIMEOUT_MILLIS} ms for the saving thread to finish its current save.
	 * Then makes up to {@value #FINAL_SAVE_ATTEMPTS} final save attempts, without backoff,
	 * and reports how many changes are still unsaved if they all fail.
	 */
	void stopSaving() {
		stopReceiving(); // nothing reported from now on should change variables mid-shutdown
		Thread thread = saveThread;
		if (thread == null)
			return;
		saving = false;
		LockSupport.unpark(thread);
		try {
			thread.join(STOP_TIMEOUT_MILLIS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		if (thread.isAlive()) {
			// stuck inside save() and holding saveLock, so a final save would get stuck too
			reportSaveFailure(null, "The saving thread of the database '" + getName() + "' did not finish after "
				+ STOP_TIMEOUT_MILLIS / 1000 + " seconds; its unsaved changes are lost");
			return;
		}
		saveThread = null;

		// save thread is done, so we can save what is left on this thread. This is the last chance to save anything.
		saveLock.lock();
		try {
			for (int attempt = 0; attempt < FINAL_SAVE_ATTEMPTS; attempt++) {
				saveCycle();
				if (failedBatch == null)
					return;
			}
			int unsaved = failedBatch.clears().size() + failedBatch.changes().size() + queue.count();
			reportSaveFailure(null, "Could not save " + unsaved + " variable changes to the database '" + getName()
				+ "' before shutting down; they are lost");
		} finally {
			saveLock.unlock();
		}
	}

	/**
	 * The save loop, which just keeps calling {@link #saveCycle()} and {@link #backupIfDue()},
	 * and waiting for the next save time. Exits when {@link #saving} is set to false.
	 * <p>
	 * Backups are only made here, not in {@link #saveCycle()}, so that {@link #flush()} and {@link #stopSaving()}
	 * never make the calling thread wait for a backup.
	 */
	private void saveLoop() {
		while (saving) {
			long start = System.nanoTime();
			long next = intervalNanos;
			try {
				next = saveCycle();
				backupIfDue();
			} catch (Throwable t) { // never let the thread die, or nothing is saved again
				reportSaveFailure(t, "Unexpected error while saving variables to '" + getName() + "'");
			}
			long wait = next - (System.nanoTime() - start);
			if (wait > 0)
				LockSupport.parkNanos(this, wait);
		}
	}

	/**
	 * helper to prevent an exception escaping the saving thread would stop saving forever.
	 * Package-private so tests can hijack this for their own purposes.
	 *
	 * @param t       the exception to report, or null to log only the message as an error
	 * @param message the message to log
	 */
	void reportSaveFailure(@Nullable Throwable t, String message) {
		try {
			if (t == null) {
				Skript.error(message);
			} else {
				Skript.exception(t, message);
			}
		} catch (Throwable logFailure) { // logging must never stop saving
			System.err.println(message);
			if (t != null)
				t.printStackTrace();
		}
	}

	/**
	 * helper to prevent an exception escaping the saving thread would stop saving forever.
	 * Package-private so tests can hijack this for their own purposes.
	 *
	 * @param failures how many saves in a row failed before this one succeeded
	 */
	void reportSaveRecovered(int failures) {
		try {
			Skript.info("Saving variables to '" + getName() + "' works again after " + failures + " failed attempts");
		} catch (Throwable ignored) { // logging must never stop saving
		}
	}

	/**
	 * Does one cycle of saving: drains pending changes and saves them.
	 * If a save fails, the batch is kept and retried on the next cycle.
	 * <p>
	 * Holds {@link #saveLock} while saving, so that only one save is in progress at a time.
	 * Also called by {@link #flush()} to save pending changes immediately
	 * and {@link #stopSaving()} to save pending changes before stopping the saving thread.
	 *
	 * @return how long until the next save cycle, in nanoseconds
	 */
	private long saveCycle() {
		saveLock.lock();
		try {
			if (failedBatch != null) {
				if (!trySave(failedBatch))
					return backoffNanos();            // still failing: don't drain, keep at most one held batch
				failedBatch = null;
				batchSaved();
			}
			// drain and save the next batch. If it fails, keep it for the next cycle.
			failedBatch = queue.drain();
			if (!failedBatch.isEmpty()) {
				if (!trySave(failedBatch))
					return backoffNanos();
				batchSaved();
			}
			failedBatch = null;
			return intervalNanos;
		} finally {
			saveLock.unlock();
		}
	}

	/**
	 * Notes that a batch was saved, so the next backup has something new in it. The caller must hold {@link #saveLock}.
	 */
	private void batchSaved() {
		if (backups != null)
			backups.changed();
	}

	/**
	 * Tries to save a batch, catching any exception and logging it. If it fails, the batch should be kept for the next cycle.
	 *
	 * @param batch the batch to save
	 * @return true if the save succeeded, false if it failed
	 */
	private boolean trySave(Batch batch) {
		try {
			save(batch);
		} catch (IOException | RuntimeException e) {
			failures++;
			if (failures == 1 || failures % 10 == 0)
				reportSaveFailure(e, "Could not save " + batch.changes().size() + " variable changes to '" + getName()
					+ "' (attempt " + failures + "); retrying");
			return false;
		}
		if (failures > 0) {
			reportSaveRecovered(failures);
			failures = 0;
		}
		return true;
	}

	/**
	 * @return The backoff time for the next save cycle, in nanoseconds, based on the number of failures in a row.
	 * The backoff time is capped at 60 seconds, and doubles for each failure up to 6 failures in a row.
	 * This is used to avoid overwhelming the storage with save attempts when it is failing.
	 */
	private long backoffNanos() {
		return TimeUnit.SECONDS.toNanos(Math.min(60, 1 << Math.min(failures - 1, 6)));
	}

	//</editor-fold>

	//<editor-fold desc="backups">
	/**
	 * Writes a backup if one is due, unless a batch is failing to save, since the stored data is then behind.
	 * Called by the save loop only. Holds {@link #saveLock}, so nothing is saved while the backup is written.
	 * Never throws.
	 */
	private void backupIfDue() {
		saveLock.lock();
		try {
			if (backups != null && backups.isOn() && failedBatch == null)
				backups.backupIfDue(System.nanoTime());
		} finally {
			saveLock.unlock();
		}
	}

	/**
	 * @return the folder this storage's backups go in, {@code plugins/Skript/backups/variables/<name>/}.
	 * Package-private so tests can use a temporary folder.
	 */
	Path backupFolder() {
		return Skript.getInstance().getDataFolder().toPath()
			.resolve("backups").resolve("variables").resolve(StorageBackups.safeName(getName()));
	}

	/**
	 * @return whether backups are compressed with gzip, from the {@code compress backups} config option.
	 * Package-private so tests don't need the config.
	 */
	boolean compressBackups() {
		return SkriptConfig.compressBackups.value();
	}
	//</editor-fold>

	// ---------------------------- MISC ----------------------------

	/**
	 * @return Whether this storage requires a file for it to work.
	 */
	protected abstract boolean requiresFile();

	/**
	 * Resolves the configured file name to the filepath this storage uses.
	 * Only called if {@link #requiresFile()} returns true.
	 *
	 * @param fileName the name of the file, as configured in the storage's configuration
	 * @return the filepath that this storage uses to store its variables
	 */
	protected File getFile(String fileName) {
		return new File(fileName);
	}

	/**
	 * @return the addon that registered this storage.
	 */
	public final SkriptAddon getRegisteringAddon() {
		return registeringAddon;
	}

	/**
	 * @return the type of this storage, e.g. "mysql"
	 */
	public final String getType() {
		return type;
	}

	/**
	 * May return null if the storage has not started configuration yet.
	 *
	 * @return the name of this storage, e.g. "default"
	 */
	public final @UnknownNullability String getName() {
		return name;
	}

	/**
	 * Whether a variable belongs in this storage, going by the {@code prefix} in its config.
	 * The name has to start with one of the storage's prefixes. Skript saves each variable to the first storage
	 * that accepts it. Called for every change, so it is kept cheap: usually a single {@link String#startsWith}.
	 *
	 * @param name the variable's name, such as {@code a::1}
	 * @return whether the name starts with one of this storage's prefixes; always true if it accepts every name
	 */
	public final boolean accept(String name) {
		String[] prefixes = this.prefixes;
		if (prefixes == null)
			return true;
		for (String prefix : prefixes) {
			if (name.startsWith(prefix))
				return true;
		}
		return false;
	}

	/**
	 * Whether a variable *could* belong in this storage, based on {@code prefix}, if it continued to match.
	 * Basically, if any prefix starts with this name. or vice versa!
	 *
	 * @param name the variable's name, such as {@code a::}
	 * @return whether one of this storage's prefixes starts with this name; always true if it accepts every name
	 */
	final boolean couldAccept(String name) {
		String[] prefixes = this.prefixes;
		if (prefixes == null)
			return true;
		for (String prefix : prefixes) {
			if (name.startsWith(prefix) || prefix.startsWith(name))
				return true;
		}
		return false;
	}

	/**
	 * @return the prefixes of the names this storage accepts, lowercased if variables are case-insensitive,
	 * or null if it accepts every name (no prefix configured, or an empty one)
	 * @see #accept(String)
	 */
	public final @Nullable @Unmodifiable List<String> getPrefixes() {
		return prefixes == null ? null : List.of(prefixes);
	}

}
