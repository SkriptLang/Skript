package org.skriptlang.skript.variables.storage.jdbc;

import ch.njol.skript.variables.SerializedVariable;
import org.junit.Test;
import org.skriptlang.skript.variables.storage.VariableStorage.Batch;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ReportTrackerTest {

	private final ReportTracker tracker = new ReportTracker();

	private static final SerializedVariable.Value SAVED = new SerializedVariable.Value("string", new byte[] {1});

	private static Batch saved(String... names) {
		List<SerializedVariable> changes = new ArrayList<>();
		for (String name : names)
			changes.add(new SerializedVariable(name, SAVED));
		return new Batch(List.of(), changes);
	}

	private static Batch cleared(String prefix) {
		return new Batch(List.of(prefix), List.of());
	}

	/**
	 * @return The names the tracker says to report again after saving the batch, with the values to report
	 */
	private Map<String, SerializedVariable.Value> raced(Batch batch) {
		Map<String, SerializedVariable.Value> raced = new LinkedHashMap<>();
		tracker.forEachRaced(batch, raced::put);
		return raced;
	}

	@Test
	public void aNameNeverReportedDidntRace() {
		assertTrue(raced(saved("a")).isEmpty());
	}

	@Test
	public void aReportStillWaitingRacesTheSave() {
		tracker.reported("a", 1);
		assertEquals(Map.of("a", SAVED), raced(saved("a", "b")));
	}

	@Test
	public void aReportAppliedInThisEpochRacesTheSave() {
		tracker.reported("a", 1);
		tracker.applied(List.of("a"), 1);
		assertEquals(Map.of("a", SAVED), raced(saved("a")));
	}

	@Test
	public void aReportAppliedBeforeTheCycleEndedIsForgotten() {
		tracker.reported("a", 1);
		tracker.applied(List.of("a"), 1);
		tracker.saveCycleEnded();
		assertEquals(0, tracker.size());
		assertTrue(raced(saved("a")).isEmpty());
	}

	@Test
	public void aReportStillWaitingIsKeptAcrossCycles() {
		tracker.reported("a", 1);
		tracker.saveCycleEnded();
		tracker.saveCycleEnded();
		assertEquals(1, tracker.size());
		assertEquals(Map.of("a", SAVED), raced(saved("a")));
	}

	@Test
	public void applyingAnOlderPollLeavesANewerReportWaiting() {
		tracker.reported("a", 1);
		tracker.reported("a", 2); // reported again before the first was applied
		tracker.applied(List.of("a"), 1);
		tracker.saveCycleEnded();
		assertEquals(Map.of("a", SAVED), raced(saved("a")));

		tracker.applied(List.of("a"), 2);
		tracker.saveCycleEnded();
		assertEquals(0, tracker.size());
	}

	@Test
	public void aClearReReportsRacedNamesUnderItAsDeleted() {
		tracker.reported("list::1", 1);
		tracker.reported("list::2", 1);
		tracker.reported("other", 1);
		Map<String, SerializedVariable.Value> raced = raced(cleared("list::"));
		assertEquals(2, raced.size());
		assertTrue(raced.containsKey("list::1") && raced.get("list::1") == null);
		assertTrue(raced.containsKey("list::2") && raced.get("list::2") == null);
	}

	@Test
	public void aNameClearedAndSetInOneBatchIsReReportedWithItsValue() {
		tracker.reported("list::1", 1);
		Batch batch = new Batch(List.of("list::"), List.of(new SerializedVariable("list::1", SAVED)));
		assertEquals(Map.of("list::1", SAVED), raced(batch));
	}

}
