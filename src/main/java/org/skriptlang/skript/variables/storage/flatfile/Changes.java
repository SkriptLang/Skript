package org.skriptlang.skript.variables.storage.flatfile;

import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.variables.storage.CsvFormat;
import org.skriptlang.skript.variables.storage.LoadReport;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * This class represents non-compacted changes in a flat file storage. The file is organized into two regions,
 * the compacted base and the non-compacted changes, appended over time. These include changes to a single
 * variable, `rows`, or list clears, `cleared`. We make sure to only keep the last change to a variable and any
 * clears remove previously changed rows, so the order of the changes is important.
 * <p>
 * Both loading and compacting work the same way: read the changes into this, then stream the compacted rows
 * through {@link #applyTo} to get the current variables in order. Only the changes are ever held in memory,
 * and only while doing either.
 */
final class Changes {

	// the last row for each name, or null if it was deleted last. Sorted, to be merged with the compacted rows.
	// only the row's line is kept, since that's a lot smaller than a split row. it's split again when passed on
	private final TreeMap<String, @Nullable String> rows = new TreeMap<>();
	// the prefixes of deleted lists, only keeping the superset of lists (a::* clear means a::b::* clear doesn't need tracking)
	private final TreeSet<String> cleared = new TreeSet<>();
	// how many valid rows were read
	private long count;

	/**
	 * Reads rows in the order they were written, and applies them.
	 * Rows with problems are skipped and reported, with their lines counted from where the reader starts.
	 *
	 * @param reader   The rows
	 * @param tornTail Whether the last row was cut off by a crash, so it should be skipped
	 * @param report   Where to report problems
	 * @throws IOException If the rows can't be read
	 */
	void read(BufferedReader reader, boolean tornTail, FileLoadReport report) throws IOException {
		String line;
		String next = reader.readLine(); // read one line ahead, to know which line is the last
		int lineNumber = 0;
		while ((line = next) != null) {
			next = reader.readLine();
			lineNumber++;
			// torn last line
			if (next == null && tornTail) {
				report.addAfterCompacted(FileLoadReport.INCOMPLETE, lineNumber, null);
				break;
			}
			// skip non-var rows
			line = line.trim();
			if (!Row.isRow(line))
				continue;
			// parse row
			Row row = Row.parse(line);
			if (row == null) {
				report.addAfterCompacted(LoadReport.INVALID, lineNumber, null);
				continue;
			}
			count++;
			String problem = row.problem();
			if (problem != null) {
				report.addAfterCompacted(problem, lineNumber, row.name());
			} else if (row.cleared()) {
				// clear the prev recorded list values
				clear(CsvFormat.listPrefix(row.name()));
			} else if (row.deleted()) {
				// just delete the var if it exists
				rows.put(row.name(), null);
			} else {
				// normal variable set
				rows.put(row.name(), row.line());
			}
		}
	}

	/**
	 * Deletes a list, and the values set in it so far.
	 *
	 * @param prefix The list's prefix, such as {@code a::}
	 */
	private void clear(String prefix) {
		String end = CsvFormat.prefixEnd(prefix);
		rows.subMap(prefix, end).clear();
		if (isCleared(prefix))
			return; // a list it's in was already deleted
		cleared.subSet(prefix, end).clear(); // lists in it
		cleared.add(prefix);
	}

	/**
	 * Checks whether a deleted list holds a name.
	 *
	 * @param name A variable's name
	 * @return Whether a deleted list holds it
	 */
	private boolean isCleared(String name) {
		String prefix = cleared.floor(name);
		return prefix != null && name.startsWith(prefix);
	}

	/**
	 * @return How many valid rows were read
	 */
	long count() {
		return count;
	}

	/**
	 * Streams the compacted rows, merges these changes into them, and gives each resulting row to the sink
	 * in the order of their names.
	 * <p>
	 * We wrote the compacted rows ourselves, so they should be sorted and never delete anything. A row that's out
	 * of order or deletes something means the file was damaged since, so it's dropped and reported, since the rows
	 * before it were already passed on.
	 *
	 * @param compacted The compacted rows, from the start of the file
	 * @param report    Where to report problems with the compacted rows. It's also told how many lines they took
	 * @param sink      Where to give the rows
	 * @return How many rows were read from the compacted rows
	 * @throws IOException If the compacted rows can't be read, or the sink fails
	 */
	long applyTo(BufferedReader compacted, FileLoadReport report, Row.Sink sink) throws IOException {
		Iterator<Map.Entry<String, @Nullable String>> appendedChanges = rows.entrySet().iterator();
		Map.Entry<String, @Nullable String> nextChange = advance(appendedChanges);
		long compactedRows = 0;
		int lineNumber = 0;
		String lastName = null;
		String line;
		while ((line = compacted.readLine()) != null) {
			lineNumber++;
			// skip non-var rows
			line = line.trim();
			if (!Row.isRow(line))
				continue;
			// parse row
			Row row = Row.parse(line);
			if (row == null) {
				report.add(LoadReport.INVALID, lineNumber, null);
				continue;
			}
			// ensure there's no deletes/out-of-order rows in here, it should be sorted with valid values only
			String name = row.name();
			if (row.deleted() || lastName != null && name.compareTo(lastName) <= 0) {
				report.add(FileLoadReport.MISPLACED, lineNumber, null);
				continue;
			}
			lastName = name;
			compactedRows++;
			// first the changed names that sort before this one
			while (nextChange != null && nextChange.getKey().compareTo(name) < 0) {
				pass(nextChange, sink); // accept the change
				nextChange = advance(appendedChanges);
			}
			// now the next change is either the same or after this row
			if (nextChange != null && nextChange.getKey().equals(name)) {
				// if same, then the change wins
				pass(nextChange, sink); // replaced, or deleted if null
				nextChange = advance(appendedChanges);
			} else if (isCleared(name)) {
				// if this variable is caught up in a clear, drop it
				continue;
			} else {
				// nextChange is after this, and nothing cleared this, so it's a valid row we should save, if it's readable
				String problem = row.problem();
				if (problem != null) {
					report.add(problem, lineNumber, name);
				} else {
					sink.accept(row);
				}
			}
		}
		// apply any remaining changes
		while (nextChange != null) {
			pass(nextChange, sink);
			nextChange = advance(appendedChanges);
		}
		report.compactedLines(lineNumber);
		return compactedRows;
	}

	/**
	 * @param changes The changes left to merge
	 * @return The next one, or null if there are none left
	 */
	private static Map.@Nullable Entry<String, @Nullable String> advance(Iterator<Map.Entry<String, @Nullable String>> changes) {
		return changes.hasNext() ? changes.next() : null;
	}

	/**
	 * Gives a change's row to the sink, unless the change deleted the name.
	 *
	 * @param change The change: a name, and its row's line or null if it was deleted
	 * @param sink   Where to give the row
	 * @throws IOException If the sink fails
	 */
	private static void pass(Map.Entry<String, @Nullable String> change, Row.Sink sink) throws IOException {
		String line = change.getValue();
		if (line == null)
			return;
		Row row = Row.parse(line);
		assert row != null : line; // it was split once already, when it was read
		sink.accept(row);
	}

}
