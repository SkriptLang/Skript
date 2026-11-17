package org.skriptlang.skript.variables.storage;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.variables.SerializedVariable;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.regex.Pattern;

/**
 * Reads and writes Skript's CSV variable format, used by the CSV storage and by backups.
 * <p>
 * Each row is one variable, written as {@code name, type, value in hex}. A row with the type {@code null}
 * means the variable was deleted, and a deleted list name such as {@code a::*} means everything in that list
 * was deleted, including nested lists. When a file holds several rows for a name, the last one wins.
 */
public final class CsvFormat {

	/**
	 * The charset of CSV variable files.
	 */
	public static final Charset CHARSET = StandardCharsets.UTF_8;

	/**
	 * The type written in rows that delete a variable or a list.
	 */
	public static final String DELETED = "null";

	/**
	 * The start of the header line recording the Skript version that wrote the file.
	 */
	public static final String VERSION_HEADER = "# version:";

	/**
	 * The start of the header line recording where the compacted rows end, as a byte offset of {@link #OFFSET_DIGITS}
	 * digits: the end of the {@link #COMPACTED_END} line. It's only written by storages that keep their file
	 * as compacted rows followed by changes.
	 */
	public static final String COMPACTED_HEADER = "# compacted:";

	/**
	 * The line after the compacted rows. Rows after it are changes made since, applied in order.
	 */
	public static final String COMPACTED_END = "# end of compacted rows";

	/**
	 * How many digits the offset in the {@link #COMPACTED_HEADER} line has, so it can be filled in
	 * once the rows are written without moving anything.
	 */
	public static final int OFFSET_DIGITS = 16;

	/**
	 * The start of a quoted value with escapes in it, which is how values with line breaks are written,
	 * like {@code E"a\nb"}. Inside it, {@code \n}, {@code \r} and {@code \\} stand for a line feed,
	 * a carriage return and a backslash, and quotes are doubled as in any quoted value.
	 */
	public static final String ESCAPED_START = "E\"";

	// writes uppercase, and reads either case
	private static final HexFormat HEX = HexFormat.of().withUpperCase();

	/**
	 * A regex pattern to check if a string contains whitespace.
	 */
	private static final Pattern CONTAINS_WHITESPACE = Pattern.compile("\\s");

	private CsvFormat() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Appends the header of a variable file, which says which Skript version wrote it.
	 *
	 * @param out       Where to append it
	 * @param compacted Whether to include a {@link #COMPACTED_HEADER} line. Its offset is all zeros,
	 *                  to be filled in once the rows and the {@link #COMPACTED_END} line are written
	 */
	public static void appendHeader(StringBuilder out, boolean compacted) {
		out.append("# === Skript's variable storage ===\n");
		out.append("# Please do not modify this file manually!\n");
		out.append("#\n");
		out.append(VERSION_HEADER).append(' ').append(Skript.getVersion()).append('\n');
		if (compacted) {
			out.append(COMPACTED_HEADER).append(' ').repeat("0", OFFSET_DIGITS).append('\n');
			out.append("#\n");
			out.append("# The rows down to '").append(COMPACTED_END).append("' hold each variable once, sorted by name.\n");
			out.append("# The rows after it are changes made since, applied in order.\n");
		}
		out.append('\n');
	}

	/**
	 * Appends the {@link #COMPACTED_END} line, including its line break.
	 *
	 * @param out Where to append it
	 */
	public static void appendCompactedEnd(StringBuilder out) {
		out.append(COMPACTED_END).append('\n');
	}

	/**
	 * Appends one row, including its line break.
	 *
	 * @param out   Where to append it
	 * @param name  The variable's name, or a list's name such as {@code a::*} for a deleted list
	 * @param value The serialized value, or null for a deletion
	 */
	public static void appendRow(StringBuilder out, String name, SerializedVariable.@Nullable Value value) {
		appendValue(out, name);
		out.append(", ");
		appendValue(out, value == null ? DELETED : value.type);
		out.append(", ");
		appendValue(out, value == null ? "" : encode(value.data));
		out.append('\n');
	}

	/**
	 * Appends one value of a row. It's quoted if it holds a comma, quote, {@code #} or whitespace,
	 * with any quotes in it doubled. A value with a line break in it is written in the {@link #ESCAPED_START} form
	 * instead, since files are read line by line.
	 *
	 * @param out   Where to append it
	 * @param value The value
	 */
	private static void appendValue(StringBuilder out, String value) {
		if (value.indexOf('\n') >= 0 || value.indexOf('\r') >= 0) {
			appendEscaped(out, value);
			return;
		}
		boolean escapingNeeded = value.contains(",")
			|| value.contains("\"")
			|| value.contains("#")
			|| CONTAINS_WHITESPACE.matcher(value).find();
		if (escapingNeeded) {
			out.append('"').append(value.replace("\"", "\"\"")).append('"');
		} else {
			out.append(value);
		}
	}

	/**
	 * Appends a value in the {@link #ESCAPED_START} form: quoted, with quotes doubled, and line breaks
	 * and backslashes escaped.
	 *
	 * @param out   Where to append it
	 * @param value The value
	 */
	private static void appendEscaped(StringBuilder out, String value) {
		out.append(ESCAPED_START);
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			switch (c) {
				case '\n' -> out.append("\\n");
				case '\r' -> out.append("\\r");
				case '\\' -> out.append("\\\\");
				case '"' -> out.append("\"\"");
				default -> out.append(c);
			}
		}
		out.append('"');
	}

	/**
	 * Undoes the escapes of a value read in the {@link #ESCAPED_START} form, after its quotes were undone.
	 *
	 * @param value The value, between its quotes
	 * @return The value, or null if it has a backslash that isn't one of the escapes
	 */
	private static @Nullable String unescape(String value) {
		int backslash = value.indexOf('\\');
		if (backslash < 0)
			return value;
		StringBuilder out = new StringBuilder(value.length());
		out.append(value, 0, backslash);
		for (int i = backslash; i < value.length(); i++) {
			char c = value.charAt(i);
			if (c != '\\') {
				out.append(c);
				continue;
			}
			if (++i == value.length())
				return null;
			switch (value.charAt(i)) {
				case 'n' -> out.append('\n');
				case 'r' -> out.append('\r');
				case '\\' -> out.append('\\');
				default -> {
					return null;
				}
			}
		}
		return out.toString();
	}

	/**
	 * Checks whether a name is a list's name.
	 *
	 * @param name A variable's name
	 * @return Whether it's a list's name, such as {@code a::*}
	 */
	public static boolean isListName(String name) {
		return name.endsWith(Variable.SEPARATOR + "*");
	}

	/**
	 * Gets the prefix shared by every element of a list.
	 *
	 * @param listName A list's name, such as {@code a::b::*}
	 * @return The prefix, such as {@code a::b::}
	 */
	public static String listPrefix(String listName) {
		assert isListName(listName) : listName;
		return listName.substring(0, listName.length() - 1);
	}

	/**
	 * Gets the exclusive upper bound of all strings starting with {@code prefix},
	 * for use with {@link java.util.NavigableMap#subMap(Object, Object)} on naturally ordered keys.
	 * <p>
	 * We just bump the last character, so {@code "a::"} becomes {@code "a:;"}. Every string starting
	 * with the prefix sorts below that, and nothing else sorts between the prefix and the bound.
	 *
	 * @param prefix A non-empty prefix whose last character isn't {@link Character#MAX_VALUE}
	 * @return The exclusive upper bound
	 */
	public static String prefixEnd(String prefix) {
		int last = prefix.length() - 1;
		assert last >= 0 && prefix.charAt(last) != Character.MAX_VALUE : prefix;
		return prefix.substring(0, last) + (char) (prefix.charAt(last) + 1);
	}

	/**
	 * Encodes bytes as uppercase hex.
	 *
	 * @param data The bytes to encode
	 * @return The hex string
	 */
	public static String encode(byte[] data) {
		return HEX.formatHex(data);
	}

	/**
	 * Decodes a hex string, in either case.
	 *
	 * @param hex The hex string to decode
	 * @return The bytes
	 * @throws IllegalArgumentException If the string isn't valid hex
	 */
	public static byte[] decode(String hex) {
		return HEX.parseHex(hex); // either case, and throws for an odd length or anything but hex digits
	}

	/**
	 * Checks whether a string is valid hex, so {@link #decode} would accept it, without decoding it.
	 *
	 * @param hex The string
	 * @return Whether it's an even number of hex digits, in either case
	 */
	public static boolean isHex(String hex) {
		if ((hex.length() & 1) != 0)
			return false;
		for (int i = 0; i < hex.length(); i++) {
			if (!HexFormat.isHexDigit(hex.charAt(i)))
				return false;
		}
		return true;
	}

	/**
	 * Splits a CSV line into its values, which are separated by commas.
	 * <p>
	 * A value is either unquoted, in which case it can't hold a comma or quote and is trimmed,
	 * or quoted, in which case it's kept exactly, with {@code ""} standing for one quote.
	 * A quoted value in the {@link #ESCAPED_START} form also has its escapes undone.
	 * Whitespace around a quoted value is ignored. We do this by hand rather than with a regex,
	 * since it runs for every row of a variable file while loading.
	 *
	 * @param line The line
	 * @return The values, or null if the line isn't valid CSV
	 */
	public static String @Nullable [] splitCSV(String line) {
		int length = line.length();
		int position = 0;
		ArrayList<String> result = new ArrayList<>(3);
		while (true) {
			int start = position;
			while (position < length && isSpace(line.charAt(position)))
				position++;
			boolean escaped = line.startsWith(ESCAPED_START, position);
			if (escaped)
				position++; // to the quote
			if (position < length && line.charAt(position) == '"') {
				StringBuilder value = null; // only needed if the value has escaped quotes
				int from = ++position;
				while (true) {
					int quote = line.indexOf('"', position);
					if (quote < 0)
						return null; // never closed
					if (quote + 1 < length && line.charAt(quote + 1) == '"') {
						if (value == null)
							value = new StringBuilder();
						value.append(line, from, quote + 1); // one of the two quotes
						position = from = quote + 2;
						continue;
					}
					String last = line.substring(from, quote);
					String quoted = value == null ? last : value.append(last).toString();
					if (escaped && (quoted = unescape(quoted)) == null)
						return null; // a backslash that isn't an escape
					result.add(quoted);
					position = quote + 1;
					break;
				}
				while (position < length && isSpace(line.charAt(position)))
					position++;
			} else {
				position = start; // the whitespace is part of the unquoted value, which is trimmed
				while (position < length) {
					char c = line.charAt(position);
					if (c == ',')
						break;
					if (c == '"')
						return null; // a quote inside an unquoted value
					position++;
				}
				result.add(line.substring(start, position).trim());
			}
			if (position == length)
				return result.toArray(new String[0]);
			if (line.charAt(position) != ',')
				return null; // something after a quoted value
			position++;
		}
	}

	/**
	 * Checks for whitespace the way regexes define {@code \s}, which the format has always used.
	 *
	 * @param c The character
	 * @return Whether it's a space, or anything from a tab to a carriage return
	 */
	private static boolean isSpace(char c) {
		return c == ' ' || (c >= '\t' && c <= '\r');
	}

}
