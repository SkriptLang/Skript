package org.skriptlang.skript.variables.storage.flatfile;

import ch.njol.skript.variables.SerializedVariable.Value;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.variables.storage.CsvFormat;
import org.skriptlang.skript.variables.storage.LoadReport;

import java.io.IOException;

/**
 * One row of a variable file, split but not decoded or deserialized yet.
 *
 * @param name The variable's or list's name
 * @param type The value's type, or {@link CsvFormat#DELETED} if the row deletes something
 * @param hex  The value in hex
 * @param line The row as it was written, trimmed, so writing it again is just a copy
 */
record Row(String name, String type, String hex, String line) {

	/**
	 * Checks whether a line holds a row, rather than being blank or a comment.
	 *
	 * @param line The line, trimmed
	 * @return Whether it holds a row, valid or not
	 */
	static boolean isRow(String line) {
		return !line.isEmpty() && !line.startsWith("#");
	}

	/**
	 * Splits a line into a row.
	 *
	 * @param line The line, trimmed
	 * @return The row, or null if it isn't valid CSV with three values
	 */
	static @Nullable Row parse(String line) {
		String[] split = CsvFormat.splitCSV(line);
		if (split == null || split.length != 3)
			return null;
		return new Row(split[0], split[1], split[2], line);
	}

	/**
	 * @return Whether the row's name is a list's name, such as {@code a::*}
	 */
	boolean list() {
		return CsvFormat.isListName(name);
	}

	/**
	 * @return Whether the row deletes a variable or a list
	 */
	boolean deleted() {
		return type.equals(CsvFormat.DELETED);
	}

	/**
	 * @return Whether the row deletes a whole list
	 */
	boolean cleared() {
		return deleted() && list();
	}

	/**
	 * Checks what's wrong with the row, if anything. Deletions are always fine, but a row that sets a value
	 * can't be for a list, and its value has to be valid hex.
	 * <p>
	 * Problem? :troll_face:
	 *
	 * @return The kind of problem, as {@link LoadReport} names them, or null if the row is fine
	 */
	@Nullable String problem() {
		if (deleted())
			return null;
		// bad row, lists can't store single values
		if (list())
			return LoadReport.LIST_VALUES;
		if (!CsvFormat.isHex(hex))
			return LoadReport.UNREADABLE;
		return null;
	}

	/**
	 * Decodes the row's value. Only call this for a row that sets a value and has no {@link #problem()}.
	 *
	 * @return The value
	 */
	Value value() {
		return new Value(type, CsvFormat.decode(hex));
	}

	/**
	 * Something that takes rows in the order of their names, like a load's listener or a rewrite.
	 */
	@FunctionalInterface
	interface Sink {

		/**
		 * @param row The next row
		 * @throws IOException If writing it out fails
		 */
		void accept(Row row) throws IOException;

	}

	/**
	 * Something that hands a {@link Sink} rows in the order of their names.
	 */
	@FunctionalInterface
	interface Source {

		/**
		 * Gives every row to the sink, in order.
		 *
		 * @param sink Where to give them
		 * @throws IOException If reading or writing fails
		 */
		void forEach(Sink sink) throws IOException;

	}

}
