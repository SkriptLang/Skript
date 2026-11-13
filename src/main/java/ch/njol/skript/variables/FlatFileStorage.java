package ch.njol.skript.variables;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.FileUtils;
import ch.njol.skript.util.Version;
import ch.njol.skript.variables.SerializedVariable.Value;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.variables.storage.LoadListener;
import org.skriptlang.skript.variables.storage.VariableStorage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Saves variables to a CSV file, one variable per line as {@code name, type, value in hex}.
 * <p>
 * Changes are appended to the end of the file, and a later line for a name replaces any earlier one.
 * A line with the type {@code null} means the variable was deleted. Once the file holds as many replaced lines
 * as current ones, it is rewritten with only the current ones.
 */
public class FlatFileStorage extends VariableStorage {

	/**
	 * The {@link Charset} used in the CSV storage file.
	 */
	public static final Charset FILE_CHARSET = StandardCharsets.UTF_8;

	// files older than this store values as text rather than hex
	private static final Version V2_1 = new Version(2, 1);
	// the file is rewritten once it has at least this many replaced lines, and at least as many as current ones
	private static final int MIN_REPLACED_LINES_TO_REWRITE = 1000;

	// what the file holds: the last value of each name. Sorted, so a list's variables are next to each other.
	private final TreeMap<String, Value> stored = new TreeMap<>();
	// how many variable lines the file has, including replaced and deleted ones
	private int lines;
	// set when a write may have left the file incomplete, so the next save rewrites it
	private boolean rewriteNeeded;
	private @Nullable Writer writer;

	FlatFileStorage(SkriptAddon addon, String type) {
		super(addon, type);
	}

	@Override
	protected boolean configure(SectionNode node) {
		return true;
	}

	@Override
	protected boolean requiresFile() {
		return true;
	}

	@Override
	protected synchronized void load(LoadListener listener) throws IOException {
		File file = file();
		boolean hasHeader = false, legacy = false;
		int invalid = 0;
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(Files.newInputStream(file.toPath()), FILE_CHARSET))) {
			String line;
			int lineNumber = 0;
			while ((line = reader.readLine()) != null) {
				lineNumber++;
				line = line.trim();
				if (line.startsWith("# version:")) {
					hasHeader = true;
					try {
						legacy = new Version(line.substring("# version:".length()).trim()).isSmallerThan(V2_1);
					} catch (IllegalArgumentException ignored) {
					}
				}
				if (line.isEmpty() || line.startsWith("#"))
					continue;

				String[] split = splitCSV(line);
				if (split == null || split.length != 3) {
					Skript.error("Invalid amount of commas in line " + lineNumber + " of " + file.getName() + " ('" + line + "')");
					invalid++;
					continue;
				}
				lines++;
				String name = split[0], type = split[1];
				if (type.equals("null")) {
					stored.remove(name);
				} else if (!legacy) {
					stored.put(name, new Value(type, decode(split[2])));
				} else {
					// stored as text, so it has to be read and stored again
					@SuppressWarnings("deprecation")
					Object value = Classes.deserialize(type, split[2]);
					Value serialized = value == null ? null : Classes.serialize(value);
					if (serialized == null) {
						Skript.error("Could not read the variable '" + name + "' in line " + lineNumber + " of " + file.getName());
						invalid++;
						stored.remove(name); // an older line for it is outdated
					} else {
						stored.put(name, serialized);
					}
				}
			}
		}

		if (invalid > 0 || legacy) {
			// the rewrite below drops what couldn't be read, so keep the file as it was
			File backup;
			try {
				backup = FileUtils.backup(file);
			} catch (IOException e) {
				throw new IOException("Could not back up " + file.getName() + " before updating it: " + e.getMessage(), e);
			}
			if (invalid > 0)
				Skript.error(invalid + " lines of " + file.getName() + " could not be read. A backup of the file was made as " + backup.getName());
			if (legacy)
				Skript.info("Updating " + file.getName() + " to the new format. A backup of the file was made as " + backup.getName());
		}

		for (Map.Entry<String, Value> entry : stored.entrySet())
			listener.loaded(entry.getKey(), entry.getValue());

		if (invalid > 0 || legacy || !hasHeader || shouldRewrite(0)) {
			rewrite();
		} else {
			writer = openWriter(file, true);
		}
	}

	@Override
	protected synchronized void save(Batch batch) throws IOException {
		// the changes are applied to what the file holds first: applying the same batch again changes nothing,
		// so a retry can rewrite the file from it, no matter how much of the batch was written
		StringBuilder appended = new StringBuilder();
		int appendedLines = 0;
		for (String prefix : batch.clears()) {
			Iterator<String> names = stored.tailMap(prefix, true).keySet().iterator();
			while (names.hasNext()) {
				String name = names.next();
				if (!name.startsWith(prefix))
					break;
				names.remove();
				writeCSV(appended, name, null, "");
				appendedLines++;
			}
		}
		for (SerializedVariable change : batch.changes()) {
			Value value = change.value;
			if (value == null) {
				if (stored.remove(change.name) == null)
					continue; // the file doesn't have it
				writeCSV(appended, change.name, null, "");
			} else {
				stored.put(change.name, value);
				writeCSV(appended, change.name, value.type, encode(value.data));
			}
			appendedLines++;
		}

		if (rewriteNeeded || writer == null || shouldRewrite(appendedLines)) {
			rewrite();
			return;
		}
		try {
			writer.write(appended.toString());
			writer.flush();
		} catch (IOException e) {
			rewriteNeeded = true; // part of the batch may have been written
			throw e;
		}
		lines += appendedLines;
	}

	/**
	 * @param appendedLines how many lines are about to be appended
	 * @return whether the file should be rewritten instead, since it has many lines that were replaced since
	 */
	private boolean shouldRewrite(int appendedLines) {
		int replaced = lines + appendedLines - stored.size();
		return replaced >= Math.max(MIN_REPLACED_LINES_TO_REWRITE, stored.size());
	}

	/**
	 * Rewrites the file with only the current variables, through a temporary file,
	 * so that the old file stays complete if the rewrite fails.
	 */
	private void rewrite() throws IOException {
		rewriteNeeded = true; // until it's done
		closeWriter();
		File file = file();
		File temp = new File(file.getParentFile(), file.getName() + ".temp");
		try (Writer out = openWriter(temp, false)) {
			writeAll(out);
		}
		try {
			Files.move(temp.toPath(), file.toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
		} catch (AtomicMoveNotSupportedException e) {
			Files.move(temp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}
		writer = openWriter(file, true);
		lines = stored.size();
		rewriteNeeded = false;
	}

	@Override
	protected boolean supportsBackups() {
		return true;
	}

	@Override
	protected synchronized void writeBackup(OutputStream out) throws IOException {
		Writer backup = new BufferedWriter(new OutputStreamWriter(out, FILE_CHARSET));
		writeAll(backup);
		backup.flush(); // not closed, since that would close out
	}

	private void writeAll(Writer out) throws IOException {
		out.write("# === Skript's variable storage ===\n");
		out.write("# Please do not modify this file manually!\n");
		out.write("#\n");
		out.write("# version: " + Skript.getVersion() + "\n");
		out.write("\n");
		StringBuilder line = new StringBuilder();
		for (Map.Entry<String, Value> entry : stored.entrySet()) {
			line.setLength(0);
			writeCSV(line, entry.getKey(), entry.getValue().type, encode(entry.getValue().data));
			out.write(line.toString());
		}
	}

	@Override
	public synchronized void close() {
		try {
			closeWriter();
		} catch (IOException e) {
			Skript.warning("Could not close " + file().getName() + ": " + e.getMessage());
		}
	}

	private static Writer openWriter(File file, boolean append) throws IOException {
		return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, append), FILE_CHARSET));
	}

	private void closeWriter() throws IOException {
		Writer writer = this.writer;
		this.writer = null;
		if (writer != null)
			writer.close();
	}

	private File file() {
		File file = getStorageFile();
		if (file == null)
			throw new IllegalStateException("The database '" + getName() + "' has no file");
		return file;
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
	 * Writes a variable as a CSV line.
	 *
	 * @param line  where to write the line, including its line break
	 * @param name  the variable's name
	 * @param type  the value's type, or null for a deleted variable
	 * @param value the value in hex, or empty for a deleted variable
	 */
	private static void writeCSV(StringBuilder line, String name, @Nullable String type, String value) {
		appendCSVValue(line, name);
		line.append(", ");
		appendCSVValue(line, type == null ? "null" : type);
		line.append(", ");
		appendCSVValue(line, value);
		line.append('\n');
	}

	private static void appendCSVValue(StringBuilder line, String value) {
		boolean escapingNeeded = value.contains(",")
			|| value.contains("\"")
			|| value.contains("#")
			|| CONTAINS_WHITESPACE.matcher(value).find();
		if (escapingNeeded) {
			line.append('"').append(value.replace("\"", "\"\"")).append('"');
		} else {
			line.append(value);
		}
	}

}
