package org.skriptlang.skript.variables.storage;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.log.ParseLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.variables.SerializedVariable;
import com.google.errorprone.annotations.ThreadSafe;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.skriptlang.skript.addon.SkriptAddon;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.StampedLock;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Saves global variables somewhere where they survive a restart, such as a file or a database.
 * <p>
 * A storage does not hold variables. Skript keeps global variables in memory and serves all reads from there,
 * so a storage is only read once, at startup, and after that only receives the changes it has to save.
 * Which storage a variable is saved to is decided by the storage's name pattern in the config.
 * <p>
 * The lifecycle of a storage, in order:
 * <ol>
 *     <li><b>Configure:</b> Skript reads the storage's config section and calls {@link #configure(SectionNode)}.
 *     If it returns false, the storage is not used.</li>
 *     <li><b>Load:</b> {@link #load(StorageListener)} reads every stored variable and passes it to Skript.
 *     If it throws, Skript does not save to this storage, so that data which could not be read is not overwritten.</li>
 *     <li><b>Save:</b> while the server runs, changes to the storage's variables are queued for it.
 *     The storage's own saving thread drains the queue about every {@link #saveIntervalMillis()} ms
 *     and passes the changes to {@link #save(Batch)}. A batch that fails to save is retried, with backoff,
 *     before anything newer is drained.</li>
 *     <li><b>Stop:</b> on shutdown, the saving thread is stopped and what is left is saved one last time.</li>
 *     <li><b>Close:</b> {@link #close()} releases the storage's resources.</li>
 * </ol>
 * <p>
 * How a change flows: Skript's in-memory variables are for reading, the queue is for writing,
 * and the batch is for saving. Setting a variable changes it in memory and queues the serialized change
 * in the same step, keeping only the last change to each name. The saving thread swaps the queue for an empty one
 * and saves its contents as one {@link Batch}, so writes never wait on I/O.
 * <p>
 * Threads: Skript calls {@link #configure(SectionNode)}, {@link #load(StorageListener)} and {@link #close()}
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
	private @Nullable Pattern pattern;
	private @Nullable File file;
	private boolean ownsFile = false;

	// change queue stuff
	private final StampedLock order = new StampedLock(); // stamped lock since re-entry is not needed.
	// the last unsaved change for each variable, with the epoch it was made in
	private ConcurrentHashMap<String, Pending> pending = new ConcurrentHashMap<>();
	// pending list clears in order
	private ArrayList<Clear> clears = new ArrayList<>();
	private long epoch; // used to drop changes made before a clear when draining. Increments every clear.
	private int sizeHint; // decaying peak hint for drain size, to avoid resizing the pending map too often.
	private static final Pending ALLOCATION_DUMMY = new Pending("", null, 0);

	// saving thread stuff
	private final ReentrantLock saveLock = new ReentrantLock();
	private @Nullable Batch failedBatch; // a batch whose save failed and needs to be retried before draining again. guarded by saveLock.
	private int failures;                // number of saves failed in a row. guarded by saveLock.
	private long intervalNanos;          // read once from saveIntervalMillis() in startSaving
	private volatile boolean saving;     // whether the save loop should be running
	private volatile @Nullable Thread saveThread;
	private static final long STOP_TIMEOUT_MILLIS = 30_000; // how long stopSaving() waits for the saving thread
	private static final int FINAL_SAVE_ATTEMPTS = 3;        // how often stopSaving() tries to save what is left

	protected VariableStorage(SkriptAddon registeringAddon, String type) {
		this.registeringAddon = registeringAddon;
		this.type = type;
	}

	// ---------------------------- CONFIGURATION ----------------------------

	/**
	 * Reads the configuration of this storage from the given node.
	 * Do not do any reading of stored data here; that is done in {@link #load(StorageListener)}.
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

		// read pattern
		String patternValue = getConfigValue(node, "pattern");
		if (patternValue == null)
			return false;
		if (patternValue.equals(".*") || patternValue.equals(".+")) {
			pattern = null; // accepts every name
		} else {
			try {
				pattern = Pattern.compile(patternValue);
			} catch (PatternSyntaxException e) {
				Skript.error("The config entry for 'pattern' in the database '" + getName() + "' is not a valid regex: "
					+ e.getLocalizedMessage());
				return false;
			}
		}

		// read file if needed
		if (requiresFile()) {
			String fileName = getConfigValue(node, "file");
			if (fileName == null)
				return false;
			if (!setUpFile(getFile(fileName)))
				return false;
		}

		boolean success = configure(node);
		if (!success)
			releaseFile();
		return success;
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
	 * Any variable loaded should be passed to {@link StorageListener#loaded(String, SerializedVariable.Value)} as it is loaded.
	 *
	 * @param listener the listener to pass the variables to
	 * @throws IOException if the stored data could not be read completely
	 */
	protected abstract void load(StorageListener listener) throws IOException;

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
	 * Called when Skript is shutting down, or if {@link #load(StorageListener)} fails.
	 */
	@Override
	public abstract void close();

	// ------------------------ CHANGE QUEUE ------------------------

	//<editor-fold desc="change queue logic">
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

		private static final Batch EMPTY = new Batch(List.of(), List.of());

		public boolean isEmpty() {
			return clears.isEmpty() && changes.isEmpty();
		}

	}

	/**
	 * An unsaved change to one variable.
	 *
	 * @param epoch the value of {@link #epoch} when the change was made; the change was made before every clear
	 *              whose epoch is equal or higher
	 */
	private record Pending(String name, SerializedVariable.@Nullable Value value, long epoch) {
	}

	/**
	 * An undrained list clear.
	 *
	 * @param prefix the list prefix, ending in {@code ::}
	 * @param epoch  the value of {@link #epoch} when the list was cleared
	 * @param save   whether the storage has to delete the list; false for clears that came from the storage itself
	 */
	private record Clear(String prefix, long epoch, boolean save) {
	}

	/**
	 * Queues a change to one variable, and runs {@code mapWrite} to change it in Skript's variables.
	 * <p>
	 * {@code mapWrite} runs while the name is locked, so changes to one name reach Skript's variables
	 * and this storage in the same order. It must not call into any storage.
	 *
	 * @param name     the variable's name
	 * @param value    the serialized new value, or null if the variable is deleted
	 * @param mapWrite changes the variable in Skript's variables
	 */
	void write(String name, SerializedVariable.@Nullable Value value, Runnable mapWrite) {
		long stamp = order.readLock();
		try {
			long epoch = this.epoch; // can't change while the shared lock is held
			pending.compute(name, (key, old) -> {
				mapWrite.run();
				return new Pending(key, value, epoch);
			});
		} finally {
			order.unlockRead(stamp);
		}
	}

	/**
	 * Takes this storage's exclusive lock, which waits for every {@link #write} in progress and blocks new ones.
	 *
	 * @return the stamp to pass to {@link #unlockExclusive(long)}
	 */
	long lockExclusive() {
		return order.writeLock();
	}

	/**
	 * Releases the exclusive lock taken by {@link #lockExclusive()}.
	 *
	 * @param stamp the stamp returned by {@link #lockExclusive()}
	 */
	void unlockExclusive(long stamp) {
		order.unlockWrite(stamp);
	}

	/**
	 * Records that a list was cleared. Pending changes under the prefix made before now are dropped when draining.
	 * O(1), whatever the number of pending changes. The caller must hold {@link #lockExclusive()}.
	 *
	 * @param prefix the list prefix, ending in {@code ::}, such as {@code a::} for {@code {a::*}}
	 * @param save   true if the storage has to delete the list,
	 *               false if the clear came from the storage itself and only local changes should be dropped
	 */
	void recordClear(String prefix, boolean save) {
		assert order.isWriteLocked();
		assert prefix.endsWith("::") : prefix;
		clears.add(new Clear(prefix, epoch++, save));
	}

	/**
	 * Queues a change while the exclusive lock is held, such as the new values of a list being set.
	 * Call it after {@link #recordClear(String, boolean)} for the same list,
	 * so that the change is not dropped by that clear. The caller must hold {@link #lockExclusive()}.
	 *
	 * @param name  the variable's name
	 * @param value the serialized new value, or null if the variable is deleted
	 */
	void putPending(String name, SerializedVariable.@Nullable Value value) {
		assert order.isWriteLocked();
		pending.put(name, new Pending(name, value, epoch));
	}

	/**
	 * Applies a change that came from this storage itself, such as another server writing to the same database.
	 * Runs {@code mapWrite} while the name is locked, and drops any unsaved local change to it,
	 * so that the storage's newer value is not overwritten. Does not queue anything.
	 *
	 * @param name     the variable's name
	 * @param mapWrite changes the variable in Skript's variables; must not call into any storage
	 */
	void externalWrite(String name, Runnable mapWrite) {
		long stamp = order.readLock();
		try {
			pending.compute(name, (key, old) -> {
				mapWrite.run();
				return null;
			});
		} finally {
			order.unlockRead(stamp);
		}
	}

	/**
	 * Counts the changes and clears queued but not yet drained, for reporting what is lost at shutdown.
	 *
	 * @return the number of queued changes and clears that have to be saved
	 */
	int countPending() {
		long stamp = order.writeLock();
		try {
			int count = pending.size();
			for (Clear clear : clears) {
				if (clear.save())
					count++;
			}
			return count;
		} finally {
			order.unlockWrite(stamp);
		}
	}

	/**
	 * Takes every change queued since the last drain, for the saving thread to save.
	 * <p>
	 * The goal here is to minimize the time it takes for writers to queue changes, so that the main thread is blocked as little as possible.
	 * The expensive work of filtering out changes made before a clear is done after the lock is released, in this method on the calling thread,
	 * since the storage's thread doesn't have the pressing business of running a server tick.
	 *
	 * @return the changes to save, possibly empty
	 */
	Batch drainPending() {
		ConcurrentHashMap<String, Pending> drained;
		ArrayList<Clear> drainedClears;
		// get replacement map presized to the estimated right size
		ConcurrentHashMap<String, Pending> replacement;
		if (sizeHint > 64) {
			replacement = new ConcurrentHashMap<>((int) Math.min(Integer.MAX_VALUE, sizeHint * 4L / 3 + 1)); // 33% extra to avoid resizing
			// ConcurrentHashMap only allocates its table on the first insert.
			// do that here, on the saving thread, rather than on the first writer
			replacement.put("", ALLOCATION_DUMMY);
			replacement.remove("");
		} else {
			replacement = new ConcurrentHashMap<>();
		}
		// swap the queue for an empty one while holding the exclusive lock, so writers are blocked only briefly
		long stamp = lockExclusive();
		try {
			if (pending.isEmpty() && clears.isEmpty()) {
				sizeHint -= sizeHint / 4;
				return Batch.EMPTY;
			}
			drained = pending;
			drainedClears = clears;
			pending = replacement;
			clears = new ArrayList<>();
		} finally {
			unlockExclusive(stamp);
		}
		// nothing can change the drained queue any more, so the rest needs no lock

		// size hint update!
		sizeHint = Math.max(drained.size(), sizeHint - sizeHint / 4); // 25% decay per drain

		List<SerializedVariable> changes = new ArrayList<>(drained.size());
		// if there are no clears, every change is saved, so we can just unwrap.
		if (drainedClears.isEmpty()) {
			for (Pending change : drained.values())
				changes.add(new SerializedVariable(change.name(), change.value()));
			return new Batch(List.of(), changes);
		}

		// we have clears, so we have to drop changes made before a clear that covers them
		List<String> saved = new ArrayList<>(drainedClears.size());
		// group clears by first name segment, so each change only checks the clears that could cover it
		Map<String, List<Clear>> clearsByFirstSegment = new HashMap<>();
		long lastClearEpoch = -1;
		for (Clear clear : drainedClears) {
			if (clear.save())
				saved.add(clear.prefix());
			clearsByFirstSegment.computeIfAbsent(firstSegment(clear.prefix()), key -> new ArrayList<>(2)).add(clear);
			lastClearEpoch = Math.max(lastClearEpoch, clear.epoch());
		}

		for (Pending change : drained.values()) {
			// a change made after every clear can't be covered by one
			if (change.epoch() > lastClearEpoch || !isCleared(change, clearsByFirstSegment))
				changes.add(new SerializedVariable(change.name(), change.value()));
		}
		return new Batch(saved, changes);
	}

	/**
	 * @return whether a clear made after the change covers its name
	 */
	private static boolean isCleared(Pending change, Map<String, List<Clear>> clearsByFirstSegment) {
		// TODO: the substring in firstSegment is most of the remaining drain cost; hash the segment in place if needed
		List<Clear> candidates = clearsByFirstSegment.get(firstSegment(change.name()));
		if (candidates == null)
			return false;
		for (Clear clear : candidates) {
			if (clear.epoch() >= change.epoch() && change.name().startsWith(clear.prefix()))
				return true;
		}
		return false;
	}

	/**
	 * @return the part of the name before the first {@code ::}, or the whole name if there is none
	 */
	private static String firstSegment(String name) {
		int end = name.indexOf("::");
		return end < 0 ? name : name.substring(0, end);
	}
	//</editor-fold>

	// ---------------------------- SAVING ----------------------------

	//<editor-fold desc="saving thread logic">

	/**
	 * Starts the saving thread, which calls {@link #drainPending()} and {@link #save(Batch)} on its own schedule.
	 * Called once by the router after {@link #load(StorageListener)} succeeds. Storages must not call this themselves.
	 * If {@link #load(StorageListener)} fails, this should never be called and the storage is closed.
	 */
	void startSaving() {
		if (saveThread != null)
			throw new IllegalStateException("Saving thread already started");
		long millis = saveIntervalMillis();
		if (millis <= 0)
			throw new IllegalStateException(getClass().getName() + " returned a save interval of " + millis + " ms");
		intervalNanos = millis * 1_000_000L;
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
			int unsaved = failedBatch.clears().size() + failedBatch.changes().size() + countPending();
			reportSaveFailure(null, "Could not save " + unsaved + " variable changes to the database '" + getName()
				+ "' before shutting down; they are lost");
		} finally {
			saveLock.unlock();
		}
	}

	/**
	 * The save loop, which just keeps calling {@link #saveCycle()} and waiting for the next save time.
	 * Exits when {@link #saving} is set to false.
	 */
	private void saveLoop() {
		while (saving) {
			long start = System.nanoTime();
			long next = intervalNanos;
			try {
				next = saveCycle();
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
			}
			// drain and save the next batch. If it fails, keep it for the next cycle.
			failedBatch = drainPending();
			if (!failedBatch.isEmpty() && !trySave(failedBatch))
				return backoffNanos();
			failedBatch = null;
			backupIfDue();                   // empty until section 6; catches its own exceptions
			return intervalNanos;
		} finally {
			saveLock.unlock();
		}
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

	private void backupIfDue() {
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

}
