package org.skriptlang.skript.variables.storage.jdbc;

import ch.njol.skript.variables.SerializedVariable;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.variables.storage.CsvFormat;
import org.skriptlang.skript.variables.storage.VariableStorage.Batch;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.BiConsumer;

/**
 * Remembers the names a storage's polls reported, so a save can tell which of its names a report may have raced.
 * <p>
 * The race: a poll reads another server's value for a name, and this server changes and saves the name before the main
 * thread applies the report. The save holds the newer value, but if this server's change was drained for saving before
 * the report was applied, nothing was pending to stop the report, so memory ends up with the older value. The fix is to
 * report the saved value again, after the older report, which {@link #forEachRaced} finds the names for.
 * <p>
 * A name is first waiting, from when it's {@link #reported} until the main thread has {@link #applied} it, and then
 * applied during a save epoch: the time between two ends of save cycles. A report applied in an earlier epoch can't
 * have raced the batch being saved, since that batch was drained after the epoch ended, so {@link #saveCycleEnded}
 * forgets it.
 * <p>
 * Thread-safe: the monitor thread reports, the main thread applies, and the saving thread saves.
 */
final class ReportTracker {

	private static final long WAITING = -1;
	private static final int CHUNK = 1000; // how many names forEachRaced checks per hold of the lock

	/**
	 * @param poll         the poll that reported the name, since a later poll can report it again
	 * @param appliedEpoch the epoch the main thread applied the report in, or {@link #WAITING}
	 */
	private record Flight(long poll, long appliedEpoch) {}

	// sorted, so a cleared list's names are one range, not a search of every name: during a catch-up it holds many
	private final NavigableMap<String, Flight> flights = new TreeMap<>();
	private long saveEpoch; // counts ended save cycles

	/**
	 * Notes that a poll reported the name, so it waits on the main thread until {@link #applied}.
	 */
	synchronized void reported(String name, long poll) {
		flights.put(name, new Flight(poll, WAITING));
	}

	/**
	 * Notes that the main thread applied what a poll reported, right after the reports. A name a newer poll reported
	 * again is left waiting, since its newer report hasn't been applied yet.
	 */
	synchronized void applied(List<String> names, long poll) {
		for (String name : names) {
			Flight flight = flights.get(name);
			if (flight != null && flight.poll() == poll)
				flights.put(name, new Flight(poll, saveEpoch));
		}
	}

	/**
	 * Finds the names of a just-committed batch that a report may have raced: those still waiting, which will be
	 * applied after the save, and those applied since the previous save cycle ended, which may be after this batch was
	 * drained. That second rule catches a little more than it must, which only reports a value memory already has.
	 * The caller must hold the lock that polls read under, so no poll reports anything in between.
	 * <p>
	 * The tracker's lock is taken for {@value #CHUNK} names at a time, not the whole batch, since the main thread's
	 * {@link #applied} waits for it, and a batch of 200,000 names held it for about 35 ms. In between, only
	 * {@link #applied} can run, since polls are held off and the cycle ends on the calling thread. It only turns a waiting
	 * name into one applied in this epoch, and both count as raced, so the answer is the same.
	 *
	 * @param batch    the batch just committed
	 * @param reReport given each raced name, with its saved value, or null if a clear in the batch deleted it
	 */
	void forEachRaced(Batch batch, BiConsumer<String, SerializedVariable.@Nullable Value> reReport) {
		synchronized (this) {
			if (flights.isEmpty())
				return;
		}
		List<SerializedVariable> racedChanges = new ArrayList<>();
		Iterator<SerializedVariable> changes = batch.changes().iterator();
		while (changes.hasNext()) {
			synchronized (this) {
				for (int i = 0; i < CHUNK && changes.hasNext(); i++) {
					SerializedVariable change = changes.next();
					Flight flight = flights.get(change.name);
					if (flight != null && raced(flight))
						racedChanges.add(change);
				}
			}
			for (SerializedVariable change : racedChanges)
				reReport.accept(change.name, change.value);
			racedChanges.clear();
		}
		if (batch.clears().isEmpty())
			return;
		// a name the batch also changed was reported with its value above
		Set<String> changed = new HashSet<>();
		for (SerializedVariable change : batch.changes())
			changed.add(change.name);
		List<String> racedNames = new ArrayList<>();
		for (String prefix : batch.clears()) {
			String end = CsvFormat.prefixEnd(prefix);
			String from = prefix;
			boolean inclusive = true;
			while (from != null) {
				synchronized (this) {
					Iterator<Map.Entry<String, Flight>> entries = flights.subMap(from, inclusive, end, false).entrySet().iterator();
					String last = null;
					for (int i = 0; i < CHUNK && entries.hasNext(); i++) {
						Map.Entry<String, Flight> entry = entries.next();
						last = entry.getKey();
						if (!changed.contains(last) && raced(entry.getValue()))
							racedNames.add(last);
					}
					// the next chunk starts after the last name, so it doesn't depend on this iterator
					from = entries.hasNext() ? last : null;
					inclusive = false;
				}
				for (String name : racedNames)
					reReport.accept(name, null);
				racedNames.clear();
			}
		}
	}

	// the caller holds the lock
	private boolean raced(Flight flight) {
		return flight.appliedEpoch() == WAITING || flight.appliedEpoch() == saveEpoch;
	}

	/**
	 * Starts a new save epoch, and forgets every applied report. Called when a save cycle ends with nothing left unsaved,
	 * so the next batch is drained after now, and a report applied before now can't have raced it: the local change was
	 * still pending when the report was applied, so the report was ignored. Reports still waiting are kept. Called every
	 * cycle, saved or not, so a server that never saves still forgets what it was sent.
	 */
	synchronized void saveCycleEnded() {
		saveEpoch++;
		if (!flights.isEmpty())
			flights.values().removeIf(flight -> flight.appliedEpoch() != WAITING);
	}

	/**
	 * @return How many names it remembers, waiting or applied in this epoch
	 */
	synchronized int size() {
		return flights.size();
	}

}
