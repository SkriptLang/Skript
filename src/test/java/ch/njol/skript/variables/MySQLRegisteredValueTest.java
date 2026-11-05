package ch.njol.skript.variables;

import ch.njol.skript.log.LogEntry;
import ch.njol.skript.log.LogHandler;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Task;
import org.bukkit.Bukkit;
import org.junit.Assume;
import org.junit.Test;

import java.nio.file.Files;
import java.util.*;

import static org.junit.Assert.*;

/** Tests the production type registry, rather than a backend-specific supported-class list. */
public class MySQLRegisteredValueTest {

	@Test
	public void sharedSerializationAndJournalReload() {
		Assume.assumeNotNull(Bukkit.getServer());
		assertEquals(Boolean.TRUE, Task.callSync(() -> {
			var path = Files.createTempDirectory("shared-variable-test").resolve("journal.bin");
			try {
				Map<String, SerializedVariable> changes = new LinkedHashMap<>();
				String large = "玩家😀".repeat(100_000);
				changes.put("list::1", Variables.serializeChange("list::1", large));
				changes.put("list::2", Variables.serializeChange("list::2", 42L));
				changes.put("list::3", Variables.serializeChange("list::3", null));
				var standard = Classes.serialize(large);
				assertNotNull(standard);
				assertEquals(standard.type, changes.get("list::1").value.type);
				assertArrayEquals(standard.data, changes.get("list::1").value.data);
				new MySQLJournal(path, "test").write(changes);
				var loaded = new MySQLJournal(path, "test").read();
				assertEquals(large, Classes.deserialize(loaded.get("list::1").value.type, loaded.get("list::1").value.data));
				assertEquals(42L, Classes.deserialize(loaded.get("list::2").value.type, loaded.get("list::2").value.data));
				assertNull(loaded.get("list::3").value);
				var inventory = Bukkit.createInventory(null, 27);
				try (var handler = new ch.njol.skript.log.LogHandler() {
					@Override
					public LogResult log(ch.njol.skript.log.LogEntry entry) {
						fail("Unsupported values should be skipped silently: " + entry.getMessage());
						return LogResult.DO_NOT_LOG;
					}
				}.start()) {
					assertNull(Variables.serializeChange("unsupported-test", new Object()));
					assertNull(Variables.serializeChange("temporary-inventory", inventory));
				}
				assertNull(Variables.serializeChange("unsupported-test", null).value);
			} finally {
				Files.deleteIfExists(path);
				Files.delete(path.getParent());
			}
			return true;
		}));
	}

	@Test
	public void unreadableRowsDoNotPreventOtherValuesFromLoading() {
		Assume.assumeNotNull(Bukkit.getServer());
		assertEquals(Boolean.TRUE, Task.callSync(() -> {
			var bad = new SerializedVariable("bad", new SerializedVariable.Value("missing-addon-type", new byte[0]));
			var anotherBad = new SerializedVariable("another-bad", bad.value);
			var good = Variables.serializeChange("good", "still readable");
			var rows = Map.of("bad", bad, "another-bad", anotherBad, "good", good);
			List<LogEntry> errors = new ArrayList<>();
			try (var handler = new LogHandler() {
				@Override
				public LogResult log(LogEntry entry) {
					errors.add(entry);
					return LogResult.DO_NOT_LOG;
				}
			}.start()) {
				assertEquals(Map.of("good", "still readable"), new PooledMySQLStorage().decodeValues(rows));
			}
			assertEquals(List.of("2 variables could not be loaded!", "Affected variables: another-bad, bad"),
					errors.stream().map(LogEntry::getMessage).toList());
			errors.forEach(entry -> assertEquals(java.util.logging.Level.SEVERE, entry.getLevel()));
			assertSame(bad, rows.get("bad"));
			assertSame(anotherBad, rows.get("another-bad"));
			return true;
		}));
	}
}
