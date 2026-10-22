package ch.njol.skript.variables;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Set;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Timespan.TimePeriod;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.google.errorprone.annotations.ThreadSafe;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.Nullable;
import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.log.ParseLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.registrations.Classes;
import org.jetbrains.annotations.VisibleForTesting;
import org.skriptlang.skript.addon.SkriptAddon;

/**
 * A variable storage is holds the means and methods of storing variables.
 * This is usually some sort of database, and could be as simply as a text file.
 * <p>
 * Variable storage itself is responsible for variable management, read and write requests,
 * and loading and unloading the variables to memory.
 * <p>
 * For storing variables on heap, see {@link VariablesMap} which provides thread safe
 * implementation for on heap variable storage.
 */
@ThreadSafe
public abstract class VariableStorage implements Closeable {

	private static final Pattern BACKUP_FILE_PATTERN = Pattern.compile("[0-9]+-[0-9a-f-]{36}\\.csv(?:\\.gz)?");

	protected long saveTaskDelay = 6000;
	protected long saveTaskPeriod = 6000;
	protected int requiredChangesForResave = 1000;
	protected long backupIntervalMillis = 7200000;
	protected int backupsToKeep = -1;
	protected volatile long lastBackup = System.currentTimeMillis();
	private final java.util.concurrent.atomic.AtomicLong backupChanges = new java.util.concurrent.atomic.AtomicLong();
	private volatile long backedUpChanges;

	protected final void markBackupChanged() {
		backupChanges.incrementAndGet();
	}

	private boolean loadSaveOptions(SectionNode node) {
		String[] keys = {"save delay", "save period", "backup interval"};
		long[] defaults = {saveTaskDelay, saveTaskPeriod, backupIntervalMillis};
		for (int i = 0; i < keys.length; i++) {
			String raw = node.getValue(keys[i]);
			if (raw == null)
				continue;
			Timespan span = Timespan.parse(raw);
			if (span == null && raw.equals("0") && i != 1) {
				defaults[i] = 0;
				continue;
			}
			if (span == null || (i == 1 && span.getAs(TimePeriod.TICK) < 1)) {
				Skript.error("Invalid '" + keys[i] + "' for database '" + databaseName + "'");
				return false;
			}
			defaults[i] = span.getAs(i == 2 ? TimePeriod.MILLISECOND : TimePeriod.TICK);
		}
		saveTaskDelay = defaults[0];
		saveTaskPeriod = defaults[1];
		backupIntervalMillis = defaults[2];
		try {
			requiredChangesForResave = Integer.parseInt(node.get("required changes per save", String.valueOf(FlatFileStorage.defaultRequiredChangesForResave)));
			backupsToKeep = Integer.parseInt(node.get("backups to keep", "-1"));
			if (requiredChangesForResave < 1 || backupsToKeep < -1)
				throw new NumberFormatException();
		} catch (NumberFormatException exception) {
			Skript.error("Invalid save threshold or backup retention for database '" + databaseName + "'");
			return false;
		}
		return true;
	}

	/**
	 * Source of the variable storage.
	 */
	private final SkriptAddon source;

	/**
	 * The name of the database
	 */
	private String databaseName;
	private boolean ownsFile;

	/**
	 * The type of the database, i.e. CSV.
	 */
	private final String databaseType;

	/**
	 * The file associated with this variable storage.
	 * Can be {@code null} if no file is required.
	 */
	protected @Nullable File file;

	/**
	 * The pattern of the variable name this storage accepts.
	 * {@code null} for '{@code .*}' or '{@code .*}'.
	 */
	private @Nullable Pattern variableNamePattern;

	protected VariableStorage(SkriptAddon source, String type) {
		assert type != null;
		this.source = source;
		this.databaseType = type;
	}

	/**
	 * Get the config name of a database
	 * <p>
	 * Note: Returns the user set name for the database, ex:
	 * <pre>{@code
	 * default: <- Config Name
	 *    type: CSV
	 * }</pre>
	 * @return name of database
	 */
	protected final String getUserConfigurationName() {
		return databaseName;
	}

	/**
	 * Get the config type of a database
	 * 
	 * @return type of database
	 */
	protected final String getDatabaseType() {
		return databaseType;
	}

	/**
	 * @return The SkriptAddon instance that registered this VariableStorage.
	 */
	public final SkriptAddon getRegisterSource() {
		return source;
	}

	/**
	 * Gets the string value at the given key of the given section node.
	 *
	 * @param sectionNode the section node.
	 * @param key the key.
	 * @return the value, or {@code null} if the value was invalid,
	 * or not found.
	 */
	protected final @Nullable String getValue(SectionNode sectionNode, String key) {
		return getValue(sectionNode, key, String.class);
	}

	/**
	 * Gets the value at the given key of the given section node,
	 * parsed with the given type.
	 *
	 * @param sectionNode the section node.
	 * @param key the key.
	 * @param type the type.
	 * @return the parsed value, or {@code null} if the value was invalid,
	 * or not found.
	 * @param <T> the type.
	 */
	protected final <T> @Nullable T getValue(SectionNode sectionNode, String key, Class<T> type) {
		return getValue(sectionNode, key, type, true);
	}

	/**
	 * Gets the value at the given key of the given section node,
	 * parsed with the given type. Prints no errors, but can return null.
	 *
	 * @param sectionNode the section node.
	 * @param key the key.
	 * @param type the type.
	 * @return the parsed value, or {@code null} if the value was invalid,
	 * or not found.
	 * @param <T> the type.
	 */
	protected final <T> @Nullable T getOptional(SectionNode sectionNode, String key, Class<T> type) {
		return getValue(sectionNode, key, type, false);
	}

	/**
	 * Gets the value at the given key of the given section node,
	 * parsed with the given type.
	 *
	 * @param sectionNode the section node.
	 * @param key the key.
	 * @param type the type.
	 * @param error if Skript should print errors and stop loading.
	 * @return the parsed value, or {@code null} if the value was invalid,
	 * or not found.
	 * @param <T> the type.
	 */
	private <T> @Nullable T getValue(SectionNode sectionNode, String key, Class<T> type, boolean error) {
		String rawValue = sectionNode.getValue(key);
		if (rawValue == null) {
			if (error)
				Skript.error("The config is missing the entry for '" + key + "' in the database '" + databaseName + "'");
			return null;
		}

		try (ParseLogHandler log = SkriptLogger.startParseLogHandler()) {
			T parsedValue = Classes.parse(rawValue, type, ParseContext.CONFIG);
			if (parsedValue == null && error)
				// Parsing failed
				log.printError("The entry for '" + key + "' in the database '" + databaseName + "' must be " +
					Classes.getSuperClassInfo(type).getName().withIndefiniteArticle());
			else
				log.printLog();

			return parsedValue;
		}
	}

	private static final Set<File> registeredFiles = ConcurrentHashMap.newKeySet();

	/**
	 * Loads the configuration for this variable storage
	 * from the given section node. Loads internal required values first in loadConfig.
	 * {@link #load(SectionNode)} is for extending classes.
	 * <p>
	 * This operation may be blocking if the variable storage deserializes some stored variables
	 * as some deserializers must be synced on the main thread.
	 *
	 * @param sectionNode the section node.
	 * @return whether the loading succeeded.
	 */
	@Blocking
	@VisibleForTesting
	public final boolean loadConfig(SectionNode sectionNode) {
		databaseName = sectionNode.getKey();
		if (!loadSaveOptions(sectionNode))
			return false;
		if (backupsToKeep == 0) {
			try {
				pruneBackups();
			} catch (IOException exception) {
				Skript.exception(exception, "Cannot remove disabled variable backups");
				return false;
			}
		}
		String pattern = getValue(sectionNode, "pattern");
		if (pattern == null)
			return false;

		try {
			// Set variable name pattern, see field javadoc for explanation of null value
			variableNamePattern = pattern.equals(".*") || pattern.equals(".+") ? null : Pattern.compile(pattern);
		} catch (PatternSyntaxException e) {
			Skript.error("Invalid pattern '" + pattern + "': " + e.getLocalizedMessage());
			return false;
		}

		if (requiresFile()) {
			// Initialize file
			String fileName = getValue(sectionNode, "file");
			if (fileName == null)
				return false;

			try {
				file = getFile(fileName).getCanonicalFile();
			} catch (IOException exception) {
				Skript.exception(exception, "Cannot resolve database path");
				return false;
			}

			if (file.exists() && !file.isFile()) {
				Skript.error("The database file '" + file.getName() + "' must be an actual file, not a directory.");
				return false;
			}

			// Create the file if it does not exist yet
			try {
				if (!file.exists() && !file.createNewFile()) {
					Skript.error("Cannot create the database file '" + file.getName() + "'");
					return false;
				}
			} catch (IOException e) {
				Skript.error("Cannot create the database file '" + file.getName() + "': " + e.getLocalizedMessage());
				return false;
			}

			// Check for read & write permissions to the file
			if (!file.canWrite()) {
				Skript.error("Cannot write to the database file '" + file.getName() + "'!");
				return false;
			}
			if (!file.canRead()) {
				Skript.error("Cannot read from the database file '" + file.getName() + "'!");
				return false;
			}

			if (!registeredFiles.add(file)) {
				Skript.error("Database `" + databaseName + "` failed to load. The file `" + fileName + "` is already registered to another database.");
				return false;
			}
			ownsFile = true;
		}

		// Load the entries custom to the variable storage
		boolean success = loadAbstract(sectionNode);
		if (!success)
			releaseFile();
		return success;
	}

	/**
	 * Used for abstract extending classes intercepting the
	 * configuration before sending to the final implementation class.
	 * <p>
	 * Override to use this method in AnotherAbstractClass;
	 * VariablesStorage -> AnotherAbstractClass -> FinalImplementation
	 * <p>
	 * This operation may be blocking if the variable storage deserializes some stored variables
	 * as some deserializers must be synced on the main thread.
	 *
	 * @param sectionNode the section node.
	 * @return whether the loading succeeded.
	 */
	@Blocking
	protected boolean loadAbstract(SectionNode sectionNode) {
		return load(sectionNode);
	}

	/**
	 * Loads variables stored here.
	 * <p>
	 * This operation may be blocking if the variable storage deserializes some stored variables
	 * as some deserializers must be synced on the main thread.
	 *
	 * @param sectionNode the section node.
	 * @return Whether the database could be loaded successfully,
	 * i.e. whether the config is correct and all variables could be loaded.
	 */
	@Blocking
	protected abstract boolean load(SectionNode sectionNode);

	/**
	 * Checks if this storage requires a file for storing its data.
	 *
	 * @return if this storage needs a file.
	 */
	protected abstract boolean requiresFile();

	/**
	 * Gets the file needed for this variable storage from the given file name.
	 * <p>
	 * Will only be called if {@link #requiresFile()} is {@code true}.
	 *
	 * @param fileName the given file name.
	 * @return the {@link File} object.
	 */
	protected abstract File getFile(String fileName);

	/**
	 * Reads a variable with given name from this storage.
	 * <p>
	 * The format of returned value follows {@link VariablesMap#getVariable(String)}.
	 * <p>
	 * This method must be thread safe.
	 *
	 * @param name name of the variable
	 * @return value of the variable
	 */
	public abstract @Nullable Object getVariable(String name);

	/**
	 * Sets the given variable to the given value.
	 * <p>
	 * This method accepts list variables,
	 * but these may only be set to {@code null}.
	 * <p>
	 * This method must be thread safe.
	 *
	 * @param name the variable name.
	 * @param value the variable value, {@code null} to delete the variable.
	 */
	public abstract void setVariable(String name, @Nullable Object value);

	/**
	 * Returns the number of currently loaded variables.
	 * <p>
	 * This number may not be fully accurate.
	 *
	 * @return number of loaded variables
	 */
	public abstract long loadedVariables();

	/** Flush pending changes, confirming that they reached durable storage. */
	public boolean flush() {
		return false;
	}

	/** Enumerate persisted names without deserializing their values. */
	public Set<String> getStoredVariableNames() throws IOException {
		return Set.of();
	}

	/** File extension matching the format written by {@link #writeBackup}. */
	protected String backupExtension() {
		return ch.njol.skript.SkriptConfig.compressBackups.value() ? ".csv.gz" : ".csv";
	}

	protected final java.io.OutputStream backupOutput(java.nio.file.Path target) throws IOException {
		var output = java.nio.file.Files.newOutputStream(target);
		try {
			return backupExtension().endsWith(".gz") ? new java.util.zip.GZIPOutputStream(output) : output;
		} catch (IOException exception) {
			output.close();
			throw exception;
		}
	}

	protected void writeBackup(java.nio.file.Path target) throws IOException {
		if (file == null)
			throw new IOException("Storage has no associated file");
		try (var output = backupOutput(target)) {
			java.nio.file.Files.copy(file.toPath(), output);
		}
	}

	protected java.nio.file.Path getBackupDirectory() {
		String name = getUserConfigurationName().replaceAll("[^A-Za-z0-9_.-]", "_");
		return new File(Skript.getInstance().getDataFolder(), "backups/variables/" + name).toPath();
	}

	protected final boolean isBackupDue() {
		return backupsToKeep == 0 || (backupIntervalMillis > 0
			&& backupChanges.get() != backedUpChanges
			&& System.currentTimeMillis() - lastBackup >= backupIntervalMillis);
	}

	protected final void backupIfDue() throws IOException {
		if (backupsToKeep == 0) {
			pruneBackups();
			return;
		}
		long now = System.currentTimeMillis();
		long revision = backupChanges.get();
		if (backupIntervalMillis == 0 || revision == backedUpChanges || now - lastBackup < backupIntervalMillis)
			return;
		java.nio.file.Path directory = getBackupDirectory();
		java.nio.file.Files.createDirectories(directory);
		java.nio.file.Path target = directory.resolve(now + "-" + java.util.UUID.randomUUID() + backupExtension());
		java.nio.file.Path temporary = directory.resolve(target.getFileName() + ".tmp");
		try {
			writeBackup(temporary);
			java.nio.file.Files.move(temporary, target, java.nio.file.StandardCopyOption.ATOMIC_MOVE);
		} catch (IOException exception) {
			java.nio.file.Files.deleteIfExists(temporary);
			throw exception;
		}
		lastBackup = now;
		backedUpChanges = revision;
		pruneBackups();
	}

	private void pruneBackups() throws IOException {
		java.nio.file.Path directory = getBackupDirectory();
		if (backupsToKeep > -1 && java.nio.file.Files.isDirectory(directory)) {
			try (var files = java.nio.file.Files.list(directory)) {
				var backups = files.filter(path -> BACKUP_FILE_PATTERN.matcher(path.getFileName().toString()).matches())
					.sorted(java.util.Comparator.reverseOrder()).toList();
				for (int i = backupsToKeep; i < backups.size(); i++)
					java.nio.file.Files.delete(backups.get(i));
			}
		}
	}

	protected final void releaseFile() {
		if (ownsFile && file != null) {
			registeredFiles.remove(file);
			ownsFile = false;
		}
	}



	/**
	 * Checks if this variable storage accepts the given variable name.
	 *
	 * @param var the variable name.
	 * @return if this storage accepts the variable name.
	 * @see #variableNamePattern
	 */
	public boolean accept(@Nullable String var) {
		if (var == null)
			return false;
		return variableNamePattern == null || variableNamePattern.matcher(var).matches();
	}

	/**
	 * Returns the name pattern accepted by this variable storage
	 *
	 * @return the name pattern, or null if accepting all
	 */
	public @Nullable Pattern getNamePattern() {
		return variableNamePattern;
	}

	/**
	 * Called when Skript gets disabled.
	 */
	@Override
	public abstract void close();

}
