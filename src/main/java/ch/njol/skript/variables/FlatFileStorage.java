package ch.njol.skript.variables;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.ExceptionUtils;
import ch.njol.skript.util.FileUtils;
import ch.njol.skript.util.Task;
import ch.njol.skript.util.Utils;
import ch.njol.skript.util.Version;
import ch.njol.util.NotifyingReference;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A variable storage that stores its content in a
 * comma-separated value file (CSV file).
 */
/*
 * TODO use a database (SQLite) instead and only load a limited amount of variables into RAM - e.g. 2 GB (configurable).
 *  If more variables are available they will be loaded when
 *  accessed. (rem: print a warning when Skript starts)
 *  rem: store null variables (in memory) to prevent looking up the same variables over and over again
 */
public class FlatFileStorage extends VariablesStorage {

	/**
	 * The {@link Charset} used in the CSV storage file.
	 */
	public static final Charset FILE_CHARSET = StandardCharsets.UTF_8;

	/**
	 * The delay for the save task.
	 */
	private static final long SAVE_TASK_DELAY = 5 * 60 * 20;

	/**
	 * The period for the save task, how long (in ticks) between each save.
	 */
	private static final long SAVE_TASK_PERIOD = 5 * 60 * 20;

	/**
	 * A reference to the {@link PrintWriter} that is used to write
	 * to the {@link #file}.
	 * <p>
	 * A Lock on this object must be acquired after connectionLock
	 * if that lock is used
	 * (and thus also after {@link Variables#getReadLock()}).
	 */
	private final NotifyingReference<PrintWriter> changesWriter = new NotifyingReference<>();

	/**
	 * Whether the storage has been loaded.
	 */
	private volatile boolean loaded = false;

	/**
	 * The amount of {@link #changes} needed
	 * for a new {@link #saveVariables(boolean) save}.
	 */
	private static int REQUIRED_CHANGES_FOR_RESAVE = 1000;

	/**
	 * The amount of variable changes written since the last full save.
	 *
	 * @see #REQUIRED_CHANGES_FOR_RESAVE
	 */
	private final AtomicInteger changes = new AtomicInteger(0);

	/**
	 * The save task.
	 *
	 * @see #changes
	 * @see #saveVariables(boolean)
	 * @see #REQUIRED_CHANGES_FOR_RESAVE
	 * @see #SAVE_TASK_DELAY
	 * @see #SAVE_TASK_PERIOD
	 */
	@Nullable
	private Task saveTask;

	/**
	 * Whether there was an error while loading variables.
	 * <p>
	 * Set back to {@code false} when a backup has been made
	 * of the variable file that caused the error.
	 */
	private boolean loadError = false;

	/**
	 * Create a new CSV storage of the given name.
	 *
	 * @param type the databse type i.e. CSV.
	 */
	FlatFileStorage(String type) {
		super(type);
	}

	/**
	 * Loads the variables in the CSV file.
	 * <p>
	 * Doesn't lock the connection, as required by
	 * {@link Variables#variableLoaded(String, Object, VariablesStorage)}.
	 */
	@SuppressWarnings("deprecation")
	@Override
	protected boolean load_i(SectionNode sectionNode) {
		SkriptLogger.setNode(null);

		if (file == null) {
			assert false : this;
			return false;
		}

		// Keep track of loading errors
		IOException ioException = null;
		int unsuccessfulVariableCount = 0;
		StringBuilder invalid = new StringBuilder();

		// The Skript version this CSV was created with
		Version csvSkriptVersion;

		// Some variables used to allow legacy CSV files to be loaded
		Version v2_0_beta3 = new Version(2, 0, "beta 3");
		boolean update2_0_beta3 = false;
		Version v2_1 = new Version(2, 1);
		boolean update2_1 = false;

		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(Files.newInputStream(file.toPath()), FILE_CHARSET))) {
			String line;
			int lineNum = 0;
			while ((line = reader.readLine()) != null) {
				lineNum++;

				line = line.trim();

				if (line.isEmpty() || line.startsWith("#")) {
					// Line doesn't contain variable
					if (line.startsWith("# version:")) {
						// Update the version accordingly

						try {
							csvSkriptVersion = new Version(line.substring("# version:".length()).trim());
							update2_0_beta3 = csvSkriptVersion.isSmallerThan(v2_0_beta3);
							update2_1 = csvSkriptVersion.isSmallerThan(v2_1);
						} catch (IllegalArgumentException ignored) {
						}
					}

					continue;
				}

				String[] split = splitCSV(line);
				if (split == null || split.length != 3) {
					// Invalid CSV line

					Skript.error("invalid amount of commas in line " + lineNum + " ('" + line + "')");
					if (invalid.length() != 0)
						invalid.append(", ");

					invalid.append(split == null ? "<unknown>" : split[0]);
					unsuccessfulVariableCount++;
					continue;
				}

				if (split[1].equals("null")) {
					Variables.variableLoaded(split[0], null, this);
				} else {
					Object deserializedValue;
					if (update2_1) {
						// Use old deserialization if variables come from old Skript version
						deserializedValue = Classes.deserialize(split[1], split[2]);
					} else {
						deserializedValue = Classes.deserialize(split[1], decode(split[2]));
					}

					if (deserializedValue == null) {
						// Couldn't deserialize variable
						if (invalid.length() != 0)
							invalid.append(", ");

						invalid.append(split[0]);
						unsuccessfulVariableCount++;
						continue;
					}

					// Legacy
					if (deserializedValue instanceof String && update2_0_beta3) {
						deserializedValue = Utils.replaceChatStyles((String) deserializedValue);
					}

					Variables.variableLoaded(split[0], deserializedValue, this);
				}
			}
		} catch (IOException e) {
			loadError = true;
			ioException = e;
		}

		if (ioException != null || unsuccessfulVariableCount > 0 || update2_1) {
			// Something's wrong (or just an old version)
			if (unsuccessfulVariableCount > 0) {
				Skript.error(unsuccessfulVariableCount + " variable" + (unsuccessfulVariableCount == 1 ? "" : "s") +
						" could not be loaded!");
				Skript.error("Affected variables: " + invalid.toString());
			}

			if (ioException != null) {
				Skript.error("An I/O error occurred while loading the variables: " + ExceptionUtils.toString(ioException));
				Skript.error("This means that some to all variables could not be loaded!");
			}

			try {
				if (update2_1) {
					Skript.info("[2.1] updating " + file.getName() + " to the new format...");
				}

				// Back up CSV file
				File backupFile = FileUtils.backup(file);
				Skript.info("Created a backup of " + file.getName() + " as " + backupFile.getName());

				loadError = false;
			} catch (IOException ex) {
				Skript.error("Could not backup " + file.getName() + ": " + ex.getMessage());
			}
		}

		if (update2_1) {
			// Save variables in new format
			saveVariables(false);
			Skript.info(file.getName() + " successfully updated.");
		}

		connect();

		// Start the save task
		saveTask = new Task(Skript.getInstance(), SAVE_TASK_DELAY, SAVE_TASK_PERIOD, true) {
			@Override
			public void run() {
				// Due to concurrency, the amount of changes may change between the get and set call
				//  but that's not a big issue
				if (changes.get() >= REQUIRED_CHANGES_FOR_RESAVE) {
					saveVariables(false);
					changes.set(0);
				}
			}
		};

		return ioException == null;
	}

	@Override
	protected void allLoaded() {
		// no transaction support
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
	protected final void disconnect() {
		synchronized (connectionLock) {
			clearChangesQueue();
			synchronized (changesWriter) {
				PrintWriter printWriter = changesWriter.get();

				if (printWriter != null) {
					printWriter.close();
					changesWriter.set(null);
				}
			}
		}
	}

	@Override
	protected final boolean connect() {
		synchronized (connectionLock) {
			synchronized (changesWriter) {
				assert file != null; // file should be non-null after load

				if (changesWriter.get() != null)
					return true;

				// Open the file stream, and create the PrintWriter with it
				try (FileOutputStream fos = new FileOutputStream(file, true)) {
					changesWriter.set(new PrintWriter(new OutputStreamWriter(fos, FILE_CHARSET)));
					loaded = true;
					return true;
				} catch (IOException e) { // close() might throw ANY IOException
					//noinspection ThrowableNotThrown
					Skript.exception(e);
					return false;
				}
			}
		}
	}

	@Override
	public void close() {
		clearChangesQueue();
		super.close();
		saveVariables(true); // also closes the writer
	}

	@Override
	protected boolean save(String name, @Nullable String type, @Nullable byte[] value) {
		synchronized (connectionLock) {
			synchronized (changesWriter) {
				if (!loaded && type == null) {
					// deleting variables is not really required for this kind of storage,
					//  as it will be completely rewritten every once in a while,
					//  and at least once when the server stops.
					return true;
				}

				// Get the PrintWriter, waiting for it to be available if needed
				PrintWriter printWriter;
				while ((printWriter = changesWriter.get()) == null) {
					try {
						changesWriter.wait();
					} catch (InterruptedException e) {
						// Re-interrupt thread
						Thread.currentThread().interrupt();
					}
				}

				writeCSV(printWriter, name, type, value == null ? "" : encode(value));
				printWriter.flush();

				changes.incrementAndGet();
			}
		}
		return true;
	}

	/**
	 * Completely rewrites the CSV file.
	 * <p>
	 * The {@code finalSave} argument is used to determine if
	 * the {@link #saveTask save} and {@link #backupTask backup} tasks
	 * should be cancelled, and if the storage should reconnect after saving.
	 *
	 * @param finalSave whether this is the last save in this session or not.
	 */
	public final void saveVariables(boolean finalSave) {
		if (finalSave) {
			// Cancel save and backup tasks, not needed with final save anyway
			if (saveTask != null)
				saveTask.cancel();
			if (backupTask != null)
				backupTask.cancel();
		}

		try {
			// Acquire read lock
			Variables.getReadLock().lock();

			synchronized (connectionLock) {
				try {
					if (file == null) {
						// This storage requires a file, so file should be nonnull
						assert false : this;
						return;
					}

					disconnect();

					if (loadError) {
						// There was an error while loading the CSV file, create a backup of it
						try {
							File backup = FileUtils.backup(file);
							Skript.info("Created a backup of the old " + file.getName() + " as " + backup.getName());
							loadError = false;
						} catch (IOException e) {
							Skript.error("Could not backup the old " + file.getName() + ": " + ExceptionUtils.toString(e));
							Skript.error("No variables are saved!");
							return;
						}
					}

					// Write the variables to a temporary file, giving less problems if saving fails
					//  (if saving fails during writing to the actual file,
					//  the data in the actual file may be partially lost)
					File tempFile = new File(file.getParentFile(), file.getName() + ".temp");

					try (PrintWriter pw = new PrintWriter(tempFile, "UTF-8")) {
						pw.println("# === Skript's variable storage ===");
						pw.println("# Please do not modify this file manually!");
						pw.println("#");
						pw.println("# version: " + Skript.getVersion());
						pw.println();
						save(pw, "", Variables.getVariables());
						pw.println();
						pw.flush();
						pw.close();
						FileUtils.move(tempFile, file, true);
					} catch (IOException e) {
						Skript.error("Unable to make a final save of the database '" + getUserConfigurationName() +
								"' (no variables are lost): " + ExceptionUtils.toString(e));
						// FIXME happens at random - check locks/threads
					}
				} finally {
					// Reconnect if needed
					if (!finalSave) {
						connect();
					}
				}
			}
		} finally {
			Variables.getReadLock().unlock();
			boolean gotWriteLock = Variables.variablesLock.writeLock().tryLock();
			if (gotWriteLock) { // Only process queue now if it doesn't require us to wait
				try {
					Variables.processChangeQueue();
				} finally {
					Variables.variablesLock.writeLock().unlock();
				}
			}
		}
	}

	/**
	 * Saves the variables.
	 * <p>
	 * This method uses the sorted variables map to save the variables in order.
	 *
	 * @param pw the print writer to write the CSV lines too.
	 * @param parent The parent's name with {@link Variable#SEPARATOR} at the end.
	 * @param map the variables map.
	 */
	@SuppressWarnings("unchecked")
	private void save(PrintWriter pw, String parent, TreeMap<String, Object> map) {
		// Iterate over all children
		for (Entry<String, Object> childEntry : map.entrySet()) {
			Object childNode = childEntry.getValue();
			String childKey = childEntry.getKey();

			if (childNode == null)
				continue; // Leaf node

			if (childNode instanceof TreeMap) {
				// TreeMap found, recurse
				save(pw, parent + childKey + Variable.SEPARATOR, (TreeMap<String, Object>) childNode);
			} else {
				// Remove variable separator if needed
				String name = childKey == null ? parent.substring(0, parent.length() - Variable.SEPARATOR.length()) : parent + childKey;

				try {
					// Loop over storages to make sure this variable is ours to store
					for (VariablesStorage storage : Variables.STORAGES) {
						if (storage.accept(name)) {
							if (storage == this) {
								// Serialize the value
								SerializedVariable.Value serializedValue = Classes.serialize(childNode);

								// Write the CSV line
								if (serializedValue != null)
									writeCSV(pw, name, serializedValue.type, encode(serializedValue.data));
							}

							break;
						}
					}
				} catch (Exception ex) {
					//noinspection ThrowableNotThrown
					Skript.exception(ex, "Error saving variable named " + name);
				}
			}
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
		byte[] decoded = new byte[hex.length() / 2];

		for (int i = 0; i < decoded.length; i++) {
			decoded[i] = (byte) ((Character.digit(hex.charAt(2 * i), 16) << 4) + Character.digit(hex.charAt(2 * i + 1), 16));
		}

		return decoded;
	}

	/**
	 * A regex pattern of a line in a CSV file.
	 */
	private static final Pattern CSV_LINE_PATTERN = Pattern.compile("(?<=^|,)\\s*([^\",]*|\"([^\"]|\"\")*\")\\s*(,|$)");

	/**
	 * Splits the given CSV line into its values.
	 *
	 * @param line the CSV line.
	 * @return the array of values.
	 *
	 * @see #CSV_LINE_PATTERN
	 */
	@Nullable
	static String[] splitCSV(String line) {
		Matcher matcher = CSV_LINE_PATTERN.matcher(line);

		int lastEnd = 0;
		ArrayList<String> result = new ArrayList<>();

		while (matcher.find()) {
			if (lastEnd != matcher.start())
				return null; // other stuff inbetween finds

			String value = matcher.group(1);
			if (value.startsWith("\""))
				// Unescape value
				result.add(value.substring(1, value.length() - 1).replace("\"\"", "\""));
			else
				result.add(value.trim());

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
	private static void writeCSV(PrintWriter printWriter, String... values) {
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
	 * @param value
	 */
	public static void setRequiredChangesForResave(int value) {
		if (value <= 0) {
			Skript.warning("Variable changes until save cannot be zero or less. Using default of 1000.");
			value = 1000;
		}
		REQUIRED_CHANGES_FOR_RESAVE = value;
	}

}
