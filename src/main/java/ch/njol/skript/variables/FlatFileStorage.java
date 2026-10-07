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

	/**
	 * Whether the storage is being saved now (written to a file).
	 */
	private final AtomicBoolean isSaving = new AtomicBoolean(false);

	/**
	 * Variables map of variables managed by this storage.
	 */
	private final VariablesMap variablesMap = new VariablesMap();

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
		boolean legacy = false;
		boolean failed = false;

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(Files.newInputStream(file.toPath()), FILE_CHARSET))) {
			String line;
			int lineNum = 0;
			while ((line = reader.readLine()) != null) {
				lineNum++;

				line = line.trim();

				if (line.startsWith("# version:")) {
					try {
						legacy = new ch.njol.skript.util.Version(line.substring(10).trim())
							.isSmallerThan(new ch.njol.skript.util.Version(2, 1));
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
							serializedVariable = new SerializedVariable(key, Classes.serialize(value));
						} else {
							serializedVariable = new SerializedVariable(key, type, decode(split[2]));
						}
					} catch (IllegalArgumentException exception) {
						Skript.error("Cannot load variable '" + key + "' in " + file.getName() + " at line " + lineNum);
						failed = true;
						continue;
					}
				}
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
		if (failed || deserialized.size() != expected || legacy) {
			try {
				FileUtils.backup(file);
			} catch (IOException exception) {
				Skript.exception(exception, "Cannot back up variable file before conversion or recovery");
				return false;
			}
		}
		deserialized.forEach(variablesMap::setVariable);
		if (legacy)
			changes.incrementAndGet();

		saveTask = new Task(Skript.getInstance(), saveTaskDelay, saveTaskPeriod, true) {
			@Override
			public void run() {
				if (changes.get() > 0 || isBackupDue())
					saveAsync();
			}
		};

		return true;
	}

	/**
	 * Calls the save executor to perform the rewrite of the CSV file.
	 */
	private void saveAsync() {
		if (closed.get())
			return;
		if (isSaving.compareAndSet(false, true)) {
			saveExecutor.execute(() -> {
				try {
					int pending = changes.getAndSet(0);
					if (!performSave(variablesMap.getAll()))
						changes.addAndGet(pending);
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

	private boolean performSave(Map<String, Object> snapshot) {
		if (!saveLock.tryLock())
			return false;
		try {
			assert file != null;
			File tempFile = new File(file.getParentFile(), file.getName() + ".temp");

			Set<SerializedVariable> serializedVariables = Classes.serialize(snapshot);
			if (serializedVariables == null || serializedVariables.size() != snapshot.size()) {
				if (Skript.debug()) {
					Skript.warning("Failed to save the variables off main thread, this may happen when Skript gets disabled.");
					Skript.warning("No data is lost, final save will run synchronously on the main thread.");
				}
				return false;
			}

			try (PrintWriter pw = new PrintWriter(tempFile, FILE_CHARSET)) {
				pw.println("# === Skript's variable storage ===");
				pw.println("# Please do not modify this file manually!");
				pw.println("#");
				pw.println("# version: " + Skript.getVersion());
				pw.println();

				serializedVariables.forEach(variable -> {
					if (variable.value() == null)
						return;
					String name = variable.name();
					String type = variable.value().type();
					String encoded = encode(variable.value().data());
					writeCSV(pw, name, type, encoded);
				});

				pw.println();
				pw.flush();
				if (pw.checkError())
					throw new IOException("Failed writing variable snapshot");
				pw.close();
				backupIfDue();
				FileUtils.move(tempFile, file, true);
				return true;
			} catch (IOException e) {
				Skript.error("Unable to make a save of the database '" + getUserConfigurationName() +
					"' (no variables are lost): " + ExceptionUtils.toString(e));
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

	@Override
	public void setVariable(String name, @Nullable Object value) {
		variablesMap.setVariable(name, value);
		int currentChanges = changes.incrementAndGet();
		if (currentChanges >= requiredChangesForResave) {
			saveAsync();
		}
	}

	@Override
	public boolean flush() {
		int pending = changes.getAndSet(0);
		boolean success = performSave(variablesMap.getAll());
		if (!success)
			changes.addAndGet(pending);
		return success;
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
		try {
			if (saveTask != null) {
				saveTask.cancel();
				saveTask = null;
			}
			// it can not finish the save anyway because Skript is disabled and
			// serialization will fail off main thread as it can not schedule
			// tasks to serialize such variables.
			// we can shutdown now as all variables are on heap and will be
			// saved once again on the main thread
			saveExecutor.shutdownNow();

			// wait for the background thread to actually release the file
			try {
				if (!saveExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
					Skript.warning("Variable save thread took too long to shutdown. Final save might fail.");
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}

			// now write to file.temp
			if (changes.get() > 0) {
				Map<String, Object> snapshot = variablesMap.getAll();
				changes.set(0);
				performSave(snapshot);
			}
		} finally {
			releaseFile();
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
		if ((hex.length() & 1) != 0 || !hex.matches("[0-9a-fA-F]*"))
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
