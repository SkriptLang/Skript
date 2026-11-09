package org.skriptlang.skript.variables.storage;

import org.junit.Test;
import org.skriptlang.skript.addon.SkriptAddon;

import java.util.function.BiFunction;

import static org.junit.Assert.*;

/**
 * Tests {@link UnloadedStorage}, the record a registered storage type is kept as.
 */
public class UnloadedStorageTest {

	private static final BiFunction<SkriptAddon, String, RecordingStorage> CONSTRUCTOR = (addon, type) -> new RecordingStorage();

	private static UnloadedStorage<RecordingStorage> type(String... names) {
		return new UnloadedStorage<>(null, RecordingStorage.class, CONSTRUCTOR, names);
	}

	@Test
	public void namesMatchIgnoringCase() {
		UnloadedStorage<RecordingStorage> type = type("csv", "file");
		assertTrue(type.matches("CSV"));
		assertTrue(type.matches("File"));
		assertFalse(type.matches("sqlite"));
		assertFalse(type.matches("cs"));
	}

	@Test
	public void namesAreCopiedOnTheWayIn() {
		String[] names = {"csv"};
		UnloadedStorage<RecordingStorage> type = type(names);
		names[0] = "changed";
		assertTrue(type.matches("csv"));
		assertFalse(type.matches("changed"));
	}

	@Test
	public void namesAreCopiedOnTheWayOut() {
		UnloadedStorage<RecordingStorage> type = type("csv");
		type.names()[0] = "changed";
		assertArrayEquals(new String[] {"csv"}, type.names());
		assertTrue(type.matches("csv"));
	}

	@Test
	public void createPassesTheRegisteringAddonAndTheConfiguredType() {
		Object[] received = new Object[2];
		UnloadedStorage<RecordingStorage> type = new UnloadedStorage<>(null, RecordingStorage.class,
			(addon, name) -> {
				received[0] = addon;
				received[1] = name;
				return new RecordingStorage();
			}, "csv");
		assertNotNull(type.create("CSV"));
		assertNull(received[0]);
		assertEquals("CSV", received[1]);
	}

	@Test
	public void createMakesANewStorageEachTime() {
		UnloadedStorage<RecordingStorage> type = type("csv");
		assertNotSame(type.create("csv"), type.create("csv"));
	}

	@Test
	public void sharedNamesAreFound() {
		assertTrue(type("csv", "file").sharesNameWith(type("sqlite", "FILE")));
		assertFalse(type("csv").sharesNameWith(type("sqlite")));
	}

	@Test(expected = IllegalArgumentException.class)
	public void aTypeNeedsAName() {
		type();
	}

	@Test(expected = IllegalArgumentException.class)
	public void blankNamesAreRejected() {
		type("csv", " ");
	}

	@Test(expected = NullPointerException.class)
	public void theClassIsRequired() {
		new UnloadedStorage<>(null, null, CONSTRUCTOR, "csv");
	}

	@Test
	public void equalTypesAreEqual() {
		assertEquals(type("csv", "file"), type("csv", "file"));
		assertEquals(type("csv", "file").hashCode(), type("csv", "file").hashCode());
		assertNotEquals(type("csv"), type("csv", "file"));
	}

}
