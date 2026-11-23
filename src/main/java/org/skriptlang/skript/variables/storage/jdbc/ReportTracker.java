package org.skriptlang.skript.variables.storage.jdbc;

import ch.njol.skript.variables.SerializedVariable;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.variables.storage.VariableStorage.Batch;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

	/**
	 * @param poll         the poll that reported the name, since a later poll can report it again
	 * @param appliedEpoch the epoch the main thread applied the report in, or {@link #WAITING}
	 */
	private record Flight(long poll, long appliedEpoch) {}

	private final Map<String, Flight> flights = new HashMap<>();
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
	 *
	 * @param batch    the batch just committed
	 * @param reReport given each raced name, with its saved value, or null if a clear in the batch deleted it
	 */
	synchronized void forEachRaced(Batch batch, BiConsumer<String, SerializedVariable.@Nullable Value> reReport) {
		if (flights.isEmpty())
			return;
		Set<String> changed = new HashSet<>();
		for (SerializedVariable change : batch.changes()) {
			changed.add(change.name);
			Flight flight = flights.get(change.name);
			if (flight != null && raced(flight))
				reReport.accept(change.name, change.value);
		}
		for (String prefix : batch.clears()) {
			for (Map.Entry<String, Flight> entry : flights.entrySet()) {
				String name = entry.getKey();
				if (name.startsWith(prefix) && !changed.contains(name) && raced(entry.getValue()))
					reReport.accept(name, null);
			}
		}
	}

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
