package org.skriptlang.skript.variables.storage;

import ch.njol.skript.Skript;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Collects the problems a storage finds while loading, such as rows it can't read,
 * so that they can be printed as one summary instead of one line each.
 * <p>
 * Each problem has a kind, such as {@link #INVALID}, and a description of where it was found, such as the
 * variable's name or a line number. The summary shows how many problems of each kind were found,
 * with a few examples of each. Not thread safe: use it from the thread that loads.
 */
public final class LoadReport {

	/**
	 * Rows that can't be read at all.
	 */
	public static final String INVALID = "invalid rows";

	/**
	 * Rows that give a list, such as {@code a::*}, a value, which only single variables can have.
	 */
	public static final String LIST_VALUES = "lists holding a value";

	/**
	 * Rows whose value can't be decoded.
	 */
	public static final String UNREADABLE = "unreadable values";

	// how many examples to show for each kind of problem
	private static final int EXAMPLES = 5;

	private final Map<String, Integer> counts = new LinkedHashMap<>(); // in the order the kinds were first found
	private final Map<String, List<Supplier<String>>> examples = new HashMap<>();

	/**
	 * Records a problem found in a row with the given name.
	 *
	 * @param problem The kind of problem, such as {@link #INVALID}
	 * @param name    The row's name, or null if it couldn't be read
	 */
	public void add(String problem, @Nullable String name) {
		String where = name == null ? "an unnamed row" : name;
		add(problem, () -> where);
	}

	/**
	 * Records a problem whose description is only known once the summary is printed,
	 * such as a line number that depends on rows read later.
	 *
	 * @param problem The kind of problem, such as {@link #INVALID}
	 * @param where   Describes where the problem was found, called only when printing
	 */
	public void add(String problem, Supplier<String> where) {
		counts.merge(problem, 1, Integer::sum);
		List<Supplier<String>> shown = examples.computeIfAbsent(problem, key -> new ArrayList<>(EXAMPLES));
		if (shown.size() < EXAMPLES)
			shown.add(where);
	}

	/**
	 * @return Whether no problems were found
	 */
	public boolean isEmpty() {
		return counts.isEmpty();
	}

	/**
	 * @return How many problems were found, of every kind
	 */
	public int count() {
		return counts.values().stream().mapToInt(Integer::intValue).sum();
	}

	/**
	 * Logs the problems as errors, with a few examples of each kind. Does nothing if there are none.
	 *
	 * @param source      What was loaded, for the messages, such as a file's or table's name
	 * @param consequence What happens to the problematic rows, such as that they were skipped
	 */
	public void print(String source, String consequence) {
		if (isEmpty())
			return;
		int total = count();
		Skript.error("Found " + total + " problem" + (total == 1 ? "" : "s") + " while loading " + source
			+ " (" + consequence + "):");
		counts.forEach((problem, count) -> {
			List<String> shown = examples.get(problem).stream().map(Supplier::get).toList();
			String more = count > shown.size() ? ", and " + (count - shown.size()) + " more" : "";
			Skript.error("  " + count + " " + problem + ": " + String.join(", ", shown) + more);
		});
	}

}
