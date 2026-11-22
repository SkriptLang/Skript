package org.skriptlang.skript.variables.storage.flatfile;

import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.variables.storage.LoadReport;

/**
 * A {@link LoadReport} for a file, which describes problems by their line and adds the problems only files have.
 * <p>
 * The rows after the compacted rows are read first, before we know how many lines come before them.
 * So their problems are added with lines counted from the end of the compacted rows,
 * and get their real line numbers once {@link #compactedLines(int)} is known.
 */
final class FileLoadReport {

	// the kinds of problems only a file can have; the others are in LoadReport
	static final String INCOMPLETE = "incomplete last rows (discarded)";
	static final String MISPLACED = "compacted rows out of order or deleting something (discarded)";

	private final LoadReport report = new LoadReport();
	// how many lines the file has up to the end of the compacted rows
	private int compactedLines;

	/**
	 * Records a problem in the compacted rows, or anywhere in a file without any.
	 *
	 * @param problem The kind of problem
	 * @param line    The row's line
	 * @param name    The row's name, if it could be read
	 */
	void add(String problem, int line, @Nullable String name) {
		report.add(problem, () -> where(problem, line, name));
	}

	/**
	 * Records a problem in the rows after the compacted rows.
	 *
	 * @param problem The kind of problem
	 * @param line    The row's line, counted from the end of the compacted rows
	 * @param name    The row's name, if it could be read
	 */
	void addAfterCompacted(String problem, int line, @Nullable String name) {
		// read compactedLines when printing, since it isn't known yet
		report.add(problem, () -> where(problem, compactedLines + line, name));
	}

	/**
	 * Sets how many lines come before the rows after the compacted rows, so their problems get real line numbers.
	 *
	 * @param lines How many lines the file has up to the end of the compacted rows
	 */
	void compactedLines(int lines) {
		compactedLines = lines;
	}

	/**
	 * @return Whether no problems were found
	 */
	boolean isEmpty() {
		return report.isEmpty();
	}

	/**
	 * Logs the problems as errors, with a few examples of each.
	 *
	 * @param fileName The name of the file, for the messages
	 */
	void print(String fileName) {
		report.print(fileName, "the file will be backed up, then rewritten without the problematic rows");
	}

	/**
	 * Describes where a problem was found: its name if there's one, otherwise its line.
	 * Unreadable values get both, since the name alone doesn't say which row it was.
	 */
	private static String where(String problem, int line, @Nullable String name) {
		if (name == null)
			return "line " + line;
		if (problem.equals(LoadReport.UNREADABLE))
			return name + " (line " + line + ")";
		return name;
	}

}
