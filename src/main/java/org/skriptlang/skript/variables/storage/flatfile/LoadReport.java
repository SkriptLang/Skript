package org.skriptlang.skript.variables.storage.flatfile;

import ch.njol.skript.Skript;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Collects the problems found while loading a file, so we can print them as one summary instead of one line each.
 * <p>
 * The rows after the compacted rows are read first, before we know how many lines come before them.
 * So their problems are added with lines counted from the end of the compacted rows,
 * and get their real line numbers once {@link #compactedLines(int)} is known.
 */
final class LoadReport {

	// the kinds of problems a load reports
	static final String INCOMPLETE = "incomplete last rows (discarded)";
	static final String INVALID = "invalid rows";
	static final String LIST_VALUES = "lists holding a value";
	static final String UNREADABLE = "unreadable values";
	static final String MISPLACED = "compacted rows out of order or deleting something (discarded)";

	// how many examples to show for each kind of problem
	private static final int EXAMPLES = 5;

	/**
	 * Where one problem was found.
	 *
	 * @param line           The row's line
	 * @param afterCompacted Whether the line is counted from the end of the compacted rows
	 * @param name           The row's name, if it could be read
	 */
	private record Problem(int line, boolean afterCompacted, @Nullable String name) {
	}

	private final Map<String, Integer> counts = new LinkedHashMap<>();
	private final Map<String, List<Problem>> examples = new HashMap<>();
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
		add(problem, new Problem(line, false, name));
	}

	/**
	 * Records a problem in the rows after the compacted rows.
	 *
	 * @param problem The kind of problem
	 * @param line    The row's line, counted from the end of the compacted rows
	 * @param name    The row's name, if it could be read
	 */
	void addAfterCompacted(String problem, int line, @Nullable String name) {
		add(problem, new Problem(line, true, name));
	}

	private void add(String problem, Problem where) {
		counts.merge(problem, 1, Integer::sum);
		List<Problem> shown = examples.computeIfAbsent(problem, key -> new ArrayList<>(EXAMPLES));
		if (shown.size() < EXAMPLES)
			shown.add(where);
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
		return counts.isEmpty();
	}

	/**
	 * Logs the problems as errors, with a few examples of each.
	 *
	 * @param fileName The name of the file, for the messages
	 */
	void print(String fileName) {
		int total = counts.values().stream().mapToInt(Integer::intValue).sum();
		Skript.error("Found " + total + " problem" + (total == 1 ? "" : "s") + " while loading " + fileName
			+ " (the file will be backed up, then rewritten without the problematic rows):");
		counts.forEach((problem, count) -> {
			List<String> shown = examples.get(problem).stream().map(where -> example(problem, where)).toList();
			String more = count > shown.size() ? ", and " + (count - shown.size()) + " more" : "";
			Skript.error("  " + count + " " + problem + ": " + String.join(", ", shown) + more);
		});
	}

	/**
	 * Describes where a problem was found: its name if there's one, otherwise its line.
	 * Unreadable values get both, since the name alone doesn't say which row it was.
	 *
	 * @param problem The kind of problem
	 * @param where   Where it was found
	 * @return The description
	 */
	private String example(String problem, Problem where) {
		int line = where.afterCompacted() ? compactedLines + where.line() : where.line();
		if (where.name() == null)
			return "line " + line;
		if (problem.equals(UNREADABLE))
			return where.name() + " (line " + line + ")";
		return where.name();
	}

}
