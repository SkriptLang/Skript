package org.skriptlang.skript.variables.storage;

import org.junit.Before;
import org.junit.Test;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.variables.VariableMap;

import java.util.List;
import java.util.function.BiFunction;

import static org.junit.Assert.*;

/**
 * Tests registering storage types with {@link VariableStorageManager}.
 */
public class VariableStorageManagerRegistrationTest {

	/**
	 * A subclass of {@link RecordingStorage}, to check that related classes can't both be registered.
	 */
	static class SubRecordingStorage extends RecordingStorage {
	}

	private VariableStorageManager manager;

	@Before
	public void setUp() {
		manager = new VariableStorageManager(VariableMap.concurrent(), value -> null, (type, data) -> null, Runnable::run);
	}

	private static <T extends VariableStorage> BiFunction<SkriptAddon, String, T> unused() {
		return (addon, type) -> {
			throw new AssertionError("no storage should be created");
		};
	}

	@Test
	public void registeredTypesAreFoundByAnyNameIgnoringCase() {
		assertTrue(manager.register(null, RecordingStorage.class, unused(), "csv", "file"));
		assertEquals(RecordingStorage.class, manager.typeFor("CSV").storageClass());
		assertEquals(RecordingStorage.class, manager.typeFor("File").storageClass());
		assertNull(manager.typeFor("sqlite"));
	}

	@Test
	public void typesAreKeptInRegistrationOrder() {
		manager.register(null, SubRecordingStorage.class, unused(), "a");
		manager.register(null, OtherStorageUnrelated.class, unused(), "b");
		List<StorageType<?>> types = manager.types();
		assertEquals(2, types.size());
		assertEquals(SubRecordingStorage.class, types.get(0).storageClass());
		assertEquals(OtherStorageUnrelated.class, types.get(1).storageClass());
	}

	@Test(expected = IllegalArgumentException.class)
	public void theSameClassTwiceThrows() {
		manager.register(null, RecordingStorage.class, unused(), "csv");
		manager.register(null, RecordingStorage.class, unused(), "other");
	}

	@Test(expected = IllegalArgumentException.class)
	public void aSubclassOfARegisteredClassThrows() {
		manager.register(null, RecordingStorage.class, unused(), "csv");
		manager.register(null, SubRecordingStorage.class, unused(), "other");
	}

	@Test(expected = IllegalArgumentException.class)
	public void aSuperclassOfARegisteredClassThrows() {
		manager.register(null, SubRecordingStorage.class, unused(), "csv");
		manager.register(null, RecordingStorage.class, unused(), "other");
	}

	@Test
	public void aSharedNameIsRefusedAndChangesNothing() {
		assertTrue(manager.register(null, SubRecordingStorage.class, unused(), "csv", "file"));
		assertFalse(manager.register(null, OtherStorageUnrelated.class, unused(), "sqlite", "FILE"));
		assertEquals(1, manager.types().size());
		assertNull(manager.typeFor("sqlite"));
		assertEquals(SubRecordingStorage.class, manager.typeFor("file").storageClass());
	}

	@Test(expected = IllegalArgumentException.class)
	public void aTypeWithoutNamesThrows() {
		manager.register(null, RecordingStorage.class, unused());
	}

	@Test
	public void registeringAfterStoragesAreSetThrows() {
		manager.setStorages(List.of());
		try {
			manager.register(null, RecordingStorage.class, unused(), "csv");
			fail("registering after loading should throw");
		} catch (IllegalStateException expected) {
			assertTrue(expected.getMessage(), expected.getMessage().contains(RecordingStorage.class.getName()));
		}
		assertTrue(manager.types().isEmpty());
	}

	@Test
	public void storagesAreKeptInConfigOrderAndCanOnlyBeSetOnce() {
		assertTrue(manager.storages().isEmpty());
		RecordingStorage first = new RecordingStorage(), second = new RecordingStorage();
		manager.setStorages(List.of(first, second));
		assertEquals(List.of(first, second), manager.storages());
		assertThrows(UnsupportedOperationException.class, () -> manager.storages().add(first));
		assertThrows(IllegalStateException.class, () -> manager.setStorages(List.of()));
	}

	@Test
	public void typesCantBeChangedFromOutside() {
		manager.register(null, RecordingStorage.class, unused(), "csv");
		assertThrows(UnsupportedOperationException.class, () -> manager.types().clear());
		assertEquals(1, manager.types().size());
	}

	/**
	 * A storage class unrelated to {@link RecordingStorage} by inheritance from either side,
	 * other than both being storages.
	 */
	static class OtherStorageUnrelated extends VariableStorage {

		OtherStorageUnrelated() {
			super(null, "other");
		}

		@Override
		protected boolean configure(ch.njol.skript.config.SectionNode node) {
			return true;
		}

		@Override
		protected void load(LoadListener listener) {
		}

		@Override
		protected void save(Batch batch) {
		}

		@Override
		protected boolean requiresFile() {
			return false;
		}

		@Override
		public void close() {
		}

	}

}
