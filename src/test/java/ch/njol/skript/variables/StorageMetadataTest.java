package ch.njol.skript.variables;

import ch.njol.skript.Skript;
import ch.njol.skript.registrations.Classes;
import org.junit.Test;
import org.skriptlang.skript.variables.storage.InMemoryVariableStorage;
import java.util.Set;
import static org.junit.Assert.*;

public class StorageMetadataTest {
	@Test
	public void registrationAliasesAreDefensiveCopies() {
		String[] names = {"memory"};
		var registration = new UnloadedStorage<>(Skript.instance(), InMemoryVariableStorage.class,
			InMemoryVariableStorage::new, names);
		names[0] = "changed";
		registration.names()[0] = "also changed";
		assertTrue(registration.matches("MEMORY"));
		assertFalse(registration.matches("changed"));
		assertNotNull(registration.create(Skript.instance(), "memory"));
	}

	@Test
	public void unknownTypesAndDeletionMarkersAreSkipped() {
		var result = Classes.deserialize(Set.of(new SerializedVariable("unknown", "missing-type", new byte[0]),
			new SerializedVariable("deleted", null)));
		assertNotNull(result);
		assertTrue(result.isEmpty());
	}

	@Test
	public void inMemoryStorageReportsLoadedValues() {
		var storage = new InMemoryVariableStorage(Skript.instance(), "memory");
		storage.setVariable("single", "value");
		storage.setVariable("list::1", 1L);
		assertEquals(2, storage.loadedVariables());
		storage.setVariable("list::*", null);
		assertEquals(1, storage.loadedVariables());
		assertEquals("value", storage.getVariable("single"));
	}
}
