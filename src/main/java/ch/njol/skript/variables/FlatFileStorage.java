package ch.njol.skript.variables;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.ExceptionUtils;
import ch.njol.skript.util.FileUtils;
import ch.njol.skript.util.Task;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.addon.SkriptAddon;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A variable storage that stores its content in a
 * comma-separated value file (CSV file).
 */
public class FlatFileStorage extends VariableStorage {

	private static final Pattern HEX_PATTERN = Pattern.compile("[0-9a-fA-F]*");

	/**
	 * The {@link Charset} used in the CSV storage file.
	 */
	public static final Charset FILE_CHARSET = StandardCharsets.UTF_8;

	/**
	 * The amount of variable changes written since the last full save.
	 *
	 * @see #requiredChangesForResave
	 */
	public static int defaultRequiredChangesForResave = 1000;

	private final AtomicInteger changes = new AtomicInteger(0);

	private static final long AUTO_SAVE_INTERVAL = TimeUnit.MINUTES.toNanos(5);

	// Monotonic time; allow the first automatic save immediately.
	private volatile long lastSaveAttempt = System.nanoTime() - AUTO_SAVE_INTERVAL;

	/**
	 * Whether the storage is being saved now (written to a file).
	 */
	private final AtomicBoolean isSaving = new AtomicBoolean(false);

	/**
	 * Variables map of variables managed by this storage.
	 */
	private final VariablesMap variablesMap = new VariablesMap();
	// Serialized at assignment, never during snapshot writes. Guarded by journalLock.
	private final VariablesMap serializedValues = new VariablesMap();
	private final Set<String> serializationFailures = new HashSet<>();

	private final Object journalLock = new Object();
	private @Nullable CsvJournal journal;
	private long journalSequence;
	private boolean journalFailed;

	private record Snapshot(Map<String, Object> values, long sequence) {}

	private @Nullable Snapshot snapshot() {
		synchronized (journalLock) {
			if (!serializationFailures.isEmpty())
				return null;
			return new Snapshot(serializedValues.getAll(), journalSequence);
		}
	}

	/**
	 * Executor used for scheduling the storage save.
	 */
	private final ExecutorService saveExecutor;

	/**
	 * Task for saving variables into the file.
	 */
	private @Nullable Task saveTask;

	/**
	 * Whether the storage has been closed.
	 */
	private final AtomicBoolean closed = new AtomicBoolean(false);

	/**
	 * Create a new CSV storage of the given name.
	 *
	 * @param source the source of this storage.
	 * @param type the database type i.e. CSV.
	 */
	public FlatFileStorage(SkriptAddon source, String type) {
		super(source, type);
		saveExecutor = Executors.newSingleThreadExecutor(r -> {
			Thread thread = new Thread(r, "FlatFileStorage-Variable-Save-" + source.name() + "-" + type);
			thread.setDaemon(false); // finish save on shutdown
			return thread;
		});
	}

	@Override
	protected final boolean load(SectionNode sectionNode) {
		SkriptLogger.setNode(null);

		if (file == null) {
			assert false : this;
			return false;
		}

		Map<String, SerializedVariable> collected = new java.util.LinkedHashMap<>();
		Map<String, Object> legacyValues = new java.util.LinkedHashMap<>();
		boolean legacy = false;
		boolean hadLegacy = false;
		boolean failed = false;

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(Files.newInputStream(file.toPath()), FILE_CHARSET))) {
			String line;
			int lineNum = 0;
			while ((line = reader.readLine()) != null) {
				lineNum++;

				line = line.trim();

				if (line.startsWith(CsvJournal.CHECKPOINT)) {
					try {
						journalSequence = Long.parseLong(line.substring(CsvJournal.CHECKPOINT.length()));
						if (journalSequence < 0)
							return false;
					} catch (NumberFormatException exception) {
						Skript.error("Invalid CSV journal checkpoint in " + file.getName());
						return false;
					}
				}
				if (line.startsWith("# version:")) {
					try {
						legacy = new ch.njol.skript.util.Version(line.substring(10).trim())
							.isSmallerThan(new ch.njol.skript.util.Version(2, 1));
						hadLegacy |= legacy;
					} catch (IllegalArgumentException exception) {
						Skript.error("Invalid CSV version in " + file.getName());
						return false;
					}
				}
				if (line.isEmpty() || line.startsWith("#"))
					continue;

				String[] split = splitCSV(line);
				if (split == null || split.length != 3) {
					// invalid CSV line
					Skript.error("Invalid CSV row in " + file.getName() + " at line " + lineNum);
					failed = true;
					continue;
				}

				String key = split[0];
				String type = split[1];
				SerializedVariable serializedVariable;
				if (type.equals("null")) {
					serializedVariable = new SerializedVariable(key, null);
				} else {
					try {
						if (legacy) {
							Object value = Classes.deserialize(type, split[2]);
							if (value == null)
								throw new IllegalArgumentException("Invalid legacy value");
							legacyValues.put(key, value);
							collected.remove(key);
							continue;
						} else {
							serializedVariable = new SerializedVariable(key, type, decode(split[2]));
						}
					} catch (IllegalArgumentException exception) {
						Skript.error("Cannot load variable '" + key + "' in " + file.getName() + " at line " + lineNum);
						failed = true;
						continue;
					}
				}
				legacyValues.remove(key);
				collected.put(key, serializedVariable);
			}
		} catch (IOException e) {
			Skript.exception(e, "Failed to load variables from storage");
			return false;
		}

		var deserialized = Classes.deserialize(new HashSet<>(collected.values()));
		if (deserialized == null)
			return false;
		long expected = collected.values().stream().filter(variable -> variable.value() != null).count();
		if (failed || deserialized.size() != expected || hadLegacy) {
			try {
				FileUtils.backup(file);
			} catch (IOException exception) {
				Skript.exception(exception, "Cannot back up variable file before conversion or recovery");
				return false;
			}
		}
		for (SerializedVariable variable : collected.values()) {
			if (deserialized.containsKey(variable.name()))
				serializedValues.setVariable(variable.name(), variable.value());
		}
		// Legacy text has no reusable binary representation; convert it once during loading.
		Set<SerializedVariable> convertedLegacy = Variables.serialize(legacyValues);
		if (convertedLegacy == null)
			return false;
		convertedLegacy.forEach(variable -> serializedValues.setVariable(variable.name(), variable.value()));
		deserialized.putAll(legacyValues);
		deserialized.forEach(variablesMap::setVariable);
		if (hadLegacy)
			changes.incrementAndGet();
		try {
			journal = new CsvJournal(file.toPath().resolveSibling(file.getName() + ".journal"));
			long checkpoint = journalSequence;
			for (CsvJournal.Entry entry : journal.read()) {
				if (entry.sequence() <= checkpoint)
					continue;
				SerializedVariable variable = entry.variable();
				Object value = variable.value() == null ? null : Classes.deserialize(variable.value());
				if (variable.value() != null && value == null)
					throw new IOException("Cannot deserialize journal variable '" + variable.name() + "'");
				variablesMap.setVariable(variable.name(), value);
				serializedValues.setVariable(variable.name(), variable.value());
				markBackupChanged();
				journalSequence = entry.sequence();
				changes.incrementAndGet();
			}
		} catch (IOException exception) {
			// Preserve the original files if journal recovery cannot complete.
			changes.set(0);
			journal = null;
			Skript.exception(exception, "Cannot recover CSV variable journal");
			return false;
		}

		saveTask = new Task(Skript.getInstance(), saveTaskDelay, saveTaskPeriod, true) {
			@Override
			public void run() {
				if (changes.get() >= requiredChangesForResave || isBackupDue())
					saveAsync();
			}
		};

		return true;
	}

	/**
	 * Requests an automatic CSV rewrite, at most once every five minutes.
	 * Snapshot rewrites require the configured change threshold as well as the cooldown.
	 * Backup-only requests do not rewrite the snapshot.
	 */
	private void saveAsync() {
		if (closed.get() || (changes.get() < requiredChangesForResave && !isBackupDue())
			|| System.nanoTime() - lastSaveAttempt < AUTO_SAVE_INTERVAL)
			return;
		if (isSaving.compareAndSet(false, true)) {
			// Another save may have finished between the first check and acquiring the flag.
			if (System.nanoTime() - lastSaveAttempt < AUTO_SAVE_INTERVAL) {
				isSaving.set(false);
				return;
			}
			saveExecutor.execute(() -> {
				try {
					if (changes.get() >= requiredChangesForResave) {
						lastSaveAttempt = System.nanoTime();
						flush();
					} else if (saveLock.tryLock()) {
						try {
							backupIfDue();
						} catch (IOException exception) {
							Skript.exception(exception, "Cannot back up CSV storage");
						} finally {
							saveLock.unlock();
						}
					}
				} catch (Throwable exception) {
					Skript.exception(exception, "Unexpected error saving CSV database '"
						+ getUserConfigurationName() + "'");
				} finally {
					isSaving.set(false);
				}
			});
		}
	}

	/**
	 * Completely rewrites the CSV file.
	 */
	private final java.util.concurrent.locks.ReentrantLock saveLock = new java.util.concurrent.locks.ReentrantLock();

	private boolean performSave() {
		if (!saveLock.tryLock())
			return false;
		try {
			assert file != null;
			Snapshot snapshot = snapshot();
			if (snapshot == null) {
				Skript.error("Cannot save CSV snapshot: failed variable serialization must be corrected by setting or deleting the affected variables.");
				return false;
			}
			File tempFile = new File(file.getParentFile(), file.getName() + ".temp");

			try (PrintWriter pw = new PrintWriter(tempFile, FILE_CHARSET)) {
				pw.println("# === Skript's variable storage ===");
				pw.println("# Please do not modify this file manually!");
				pw.println("#");
				pw.println("# version: " + Skript.getVersion());
				pw.println(CsvJournal.CHECKPOINT + snapshot.sequence());
				pw.println();

				snapshot.values().entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> {
					SerializedVariable.Value value = (SerializedVariable.Value) entry.getValue();
					writeCSV(pw, entry.getKey(), value.type(), encode(value.data()));
				});

				pw.println();
				pw.flush();
				if (pw.checkError())
					throw new IOException("Failed writing variable snapshot");
				pw.close();
				try {
					backupIfDue();
				} catch (IOException exception) {
					Skript.error("Cannot back up database '" + getUserConfigurationName()
						+ "'; continuing with the variable save: " + ExceptionUtils.toString(exception));
				}
				synchronized (journalLock) {
					// Publish the checkpoint before removing any journal records.
					Files.move(tempFile.toPath(), file.toPath(), java.nio.file.StandardCopyOption.ATOMIC_MOVE,
						java.nio.file.StandardCopyOption.REPLACE_EXISTING);
					if (journal != null) {
						try {
							if (!journalFailed) {
								journal.compact(snapshot.sequence());
							} else if (snapshot.sequence() == journalSequence) {
								// All changes are now in the snapshot, including failed appends.
								journal.reset();
								journalFailed = false;
							}
						} catch (IOException exception) {
							Skript.exception(exception, "CSV snapshot saved, but journal cleanup failed");
						}
					}
				}
				lastSaveAttempt = System.nanoTime();
				return true;
			} catch (IOException e) {
				Skript.error("Unable to make a save of the database '" + getUserConfigurationName() +
					"'; changes remain unsaved: " + ExceptionUtils.toString(e));
				return false;
			}
		} finally {
			saveLock.unlock();
		}
	}

	@Override
	protected boolean requiresFile() {
		return true;
	}

	@Override
	protected File getFile(String fileName) {
		return new File(fileName);
	}

	@Override
	public @Nullable Object getVariable(String name) {
		return variablesMap.getVariable(name);
	}

	/**
	 * Captures serialized bytes at assignment for journaling and asynchronous snapshots.
	 * Mutable values must be assigned again after modification to persist the new state.
	 */
	@Override
	public void setVariable(String name, @Nullable Object value) {
		// Serialize before acquiring the lock: serializers may wait for the main thread.
		Map<String, Object> single = new java.util.HashMap<>();
		single.put(name, value);
		Set<SerializedVariable> serialized = Variables.serialize(single);
		int currentChanges;
		synchronized (journalLock) {
			if (closed.get())
				throw new IllegalStateException("Variable storage is closed");
			variablesMap.setVariable(name, value);
			if (serialized == null) {
				serializationFailures.add(name);
			} else {
				serializedValues.setVariable(name, serialized.iterator().next().value());
				if (name.endsWith("::*")) {
					String prefix = name.substring(0, name.length() - 1);
					serializationFailures.removeIf(key -> key.startsWith(prefix));
				} else {
					serializationFailures.remove(name);
				}
			}
			markBackupChanged();
			long sequence = ++journalSequence;
			currentChanges = changes.incrementAndGet();
			if (journal != null && !journalFailed && serialized != null) {
				try {
					journal.append(new CsvJournal.Entry(sequence, serialized.iterator().next()));
				} catch (IOException exception) {
					journalFailed = true;
					Skript.exception(exception, "Cannot append variable journal; changes remain in memory until a snapshot succeeds");
				}
			} else if (serialized == null) {
				Skript.error("Cannot journal variable '" + name + "'; set or delete it again to retry serialization");
			}
		}
		if (currentChanges >= requiredChangesForResave) {
			saveAsync();
		}
	}

	@Override
	public boolean flush() {
		int pending = changes.getAndSet(0);
		boolean success = false;
		try {
			success = performSave();
			return success;
		} finally {
			if (!success)
				changes.addAndGet(pending);
		}
	}

	@Override
	public Set<String> getStoredVariableNames() {
		return variablesMap.getAll().keySet();
	}

	@Override
	public long loadedVariables() {
		return variablesMap.size();
	}

	@Override
	public void close() {
		if (!closed.compareAndSet(false, true))
			return;
		boolean interrupted = Thread.interrupted();
		boolean locked = false;
		boolean safeToRelease = false;
		try {
			if (saveTask != null) {
				saveTask.cancel();
				saveTask = null;
			}
			// Interrupt a worker waiting for main-thread serialization before waiting for it.
			saveExecutor.shutdownNow();
			long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(30);
			long warningAt = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
			boolean warned = false;
			while (System.nanoTime() < deadline) {
				try {
					// Wait for the worker's bookkeeping as well as its file operations.
					if (saveExecutor.awaitTermination(1, TimeUnit.SECONDS)) {
						long remaining = deadline - System.nanoTime();
						if (remaining > 0 && saveLock.tryLock(Math.min(remaining,
							TimeUnit.SECONDS.toNanos(1)), TimeUnit.NANOSECONDS)) {
							locked = true;
							break;
						}
					}
				} catch (InterruptedException exception) {
					// Finish the shutdown attempt, then restore the caller's interrupt status.
					interrupted = true;
				}
				if (!warned && System.nanoTime() >= warningAt) {
					Skript.warning("Waiting for an active variable save for database '"
						+ getUserConfigurationName() + "' before the final save.");
					warned = true;
				}
			}
			if (!locked) {
				Skript.error("Timed out after 30 seconds waiting to save database '"
					+ getUserConfigurationName() + "'. A save may be stalled; unsaved changes may be lost."
					+ " File ownership is retained while a writer may still be active.");
				return;
			}
			safeToRelease = true;
			// The worker has finished restoring pending changes after any failed save.
			if (changes.get() > 0 && !flush())
				Skript.error("Final save failed for database '" + getUserConfigurationName()
					+ "'. Unsaved changes will be lost when the server stops.");
		} finally {
			if (locked)
				saveLock.unlock();
			if (safeToRelease)
				releaseFile();
			if (interrupted)
				Thread.currentThread().interrupt();
		}
	}

	/**
	 * Encode the given byte array to a hexadecimal string.
	 *
	 * @param data the byte array to encode.
	 * @return the hex string.
	 */
	static String encode(byte[] data) {
		char[] encoded = new char[data.length * 2];

		for (int i = 0; i < data.length; i++) {
			encoded[2 * i] = Character.toUpperCase(Character.forDigit((data[i] & 0xF0) >>> 4, 16));
			encoded[2 * i + 1] = Character.toUpperCase(Character.forDigit(data[i] & 0xF, 16));
		}

		return new String(encoded);
	}

	/**
	 * Decodes the given hexadecimal string to a byte array.
	 *
	 * @param hex the hex string to encode.
	 * @return the byte array.
	 */
	static byte[] decode(String hex) {
		if ((hex.length() & 1) != 0 || !HEX_PATTERN.matcher(hex).matches())
			throw new IllegalArgumentException("Invalid hexadecimal value");
		byte[] decoded = new byte[hex.length() / 2];

		for (int i = 0; i < decoded.length; i++) {
			decoded[i] = (byte) ((Character.digit(hex.charAt(2 * i), 16) << 4) + Character.digit(hex.charAt(2 * i + 1), 16));
		}

		return decoded;
	}

	/**
	 * A regex pattern of a line in a CSV file.
	 * <ul>
	 * <li>{@code (?<=^|,)}: assert that the match is preceded by the start of the line or a comma</li>
	 * <li>{@code (?:([^",]*)|"((?:[^"]+|"")*)")}: match either a quoted or unquoted value</li>
	 * <ul>
	 * 	<li>- {@code ([^",]*)}: match an unquoted value</li>
	 * 	<li>- {@code "((?:[^"]+|"")*)"}: match a quoted value</li>
	 * </ul>
	 * <li>{@code (?:,|$)}: match either a comma or the end of the line</li>
	 * </ul>
	 */
	private static final Pattern CSV_LINE_PATTERN = Pattern.compile("(?<=^|,)\\s*(?:([^\",]*)|\"((?:[^\"]+|\"\")*)\")\\s*(?:,|$)");

	/**
	 * Splits the given CSV line into its values.
	 *
	 * @param line the CSV line.
	 * @return the array of values.
	 *
	 * @see #CSV_LINE_PATTERN
	 */
	static String @Nullable [] splitCSV(String line) {
		Matcher matcher = CSV_LINE_PATTERN.matcher(line);

		int lastEnd = 0;
		ArrayList<String> result = new ArrayList<>();

		while (matcher.find()) {
			if (lastEnd != matcher.start())
				return null; // other stuff in between finds

			if (matcher.group(1) != null) {
				// unquoted, leave as is
				result.add(matcher.group(1).trim());
			} else {
				// quoted, remove quotes
				result.add(matcher.group(2).replace("\"\"", "\""));
			}

			lastEnd = matcher.end();
		}

		if (lastEnd != line.length())
			return null; // other stuff after last find

		return result.toArray(new String[0]);
	}

	/**
	 * A regex pattern to check if a string contains whitespace.
	 * <p>
	 * Use with {@link Matcher#find()} to search the whole string for whitespace.
	 */
	private static final Pattern CONTAINS_WHITESPACE = Pattern.compile("\\s");

	/**
	 * Writes the given 3 values as a CSV value to the given {@link PrintWriter}.
	 *
	 * @param printWriter the print writer.
	 * @param values the values, must have a length of {@code 3}.
	 */
	static void writeCSV(PrintWriter printWriter, String... values) {
		assert values.length == 3; // name, type, value

		for (int i = 0; i < values.length; i++) {
			if (i != 0)
				printWriter.print(", ");

			String value = values[i];

			// Check if the value should be escaped
			boolean escapingNeeded = value != null
				&& (value.contains(",")
				|| value.contains("\"")
				|| value.contains("#")
				|| CONTAINS_WHITESPACE.matcher(value).find());
			if (escapingNeeded) {
				value = '"' + value.replace("\"", "\"\"") + '"';
			}

			printWriter.print(value);
		}

		printWriter.println();
	}

	/**
	 * Change the required amount of variable changes until variables are saved.
	 * Cannot be zero or less.
	 */
	public static void setRequiredChangesForResave(int value) {
		if (value <= 0) {
			Skript.warning("Variable changes until save cannot be zero or less. Using default of 1000.");
			value = 1000;
		}
		defaultRequiredChangesForResave = value;
	}

}
