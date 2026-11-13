package org.skriptlang.skript.variables.storage;

import ch.njol.skript.variables.SerializedVariable;
import org.jetbrains.annotations.Nullable;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests how a {@link VariableStorage} reports changes made from outside the server:
 * {@link VariableStorage#reportUpdated} and {@link VariableStorage#reportCleared}.
 */
public class VariableStorageReportTest {

	/**
	 * Records every change passed to it, as text.
	 */
	private static final class RecordingHandler implements VariableStorage.ChangeHandler {

		final List<String> received = new ArrayList<>();

		@Override
		public void updated(VariableStorage source, String name, SerializedVariable.@Nullable Value value) {
			received.add(source.getName() + " updated " + name + (value == null ? " (deleted)" : " = " + value.type));
		}

		@Override
		public void cleared(VariableStorage source, String listName) {
			received.add(source.getName() + " cleared " + listName);
		}

	}

	private RecordingStorage storage;
	private RecordingHandler handler;

	@Before
	public void setUp() {
		storage = new RecordingStorage();
		storage.setNameForTest("shared");
		handler = new RecordingHandler();
	}

	@Test
	public void reportsBeforeTheStorageRunsThrow() {
		assertThrows(IllegalStateException.class,
			() -> storage.reportUpdatedForTest("a", new SerializedVariable.Value("string", new byte[0])));
		assertThrows(IllegalStateException.class, () -> storage.reportClearedForTest("a::*"));
	}

	@Test
	public void reportsArePassedOnWithTheirSource() {
		storage.startReceiving(handler);
		storage.reportUpdatedForTest("a", new SerializedVariable.Value("string", new byte[0]));
		storage.reportUpdatedForTest("b", null);
		storage.reportClearedForTest("l::*");
		assertEquals(List.of("shared updated a = string", "shared updated b (deleted)", "shared cleared l::*"),
			handler.received);
	}

	@Test
	public void reportsAfterStoppingAreIgnored() {
		storage.startReceiving(handler);
		storage.stopSaving(); // never started saving, but stops receiving all the same
		storage.reportUpdatedForTest("a", null);
		storage.reportClearedForTest("l::*");
		assertTrue(handler.received.isEmpty());
	}

	@Test
	public void reportsAfterAFailureAreIgnored() {
		storage.stopReceiving(); // what the manager does to a storage that failed to load
		storage.reportUpdatedForTest("a", null);
		assertTrue(handler.received.isEmpty());
	}

	@Test
	public void listsCantBeUpdatedAsVariables() {
		storage.startReceiving(handler);
		assertThrows(IllegalArgumentException.class, () -> storage.reportUpdatedForTest("a::*", null));
		assertTrue(handler.received.isEmpty());
	}

	@Test
	public void onlyListsCanBeCleared() {
		storage.startReceiving(handler);
		assertThrows(IllegalArgumentException.class, () -> storage.reportClearedForTest("a"));
		assertThrows(IllegalArgumentException.class, () -> storage.reportClearedForTest("a::"));
		assertTrue(handler.received.isEmpty());
	}

	@Test
	public void receivingStartsOnlyOnce() {
		storage.startReceiving(handler);
		assertThrows(IllegalStateException.class, () -> storage.startReceiving(new RecordingHandler()));
	}

}
