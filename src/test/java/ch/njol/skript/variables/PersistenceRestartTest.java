package ch.njol.skript.variables;

import ch.njol.skript.Skript;
import ch.njol.skript.test.runner.SkriptAsyncJUnitTest;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.junit.Assume;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeSet;

import static org.junit.Assert.*;

/** Run by persistence_test.py in successive JVMs, using the configured production storage. */
public class PersistenceRestartTest extends SkriptAsyncJUnitTest {

	private static final String PREFIX = "persistprobe::";

	@Test
	public void persistsAcrossServerRestarts() throws Exception {
		String phase = System.getenv("SKRIPT_PERSISTENCE_PHASE");
		Assume.assumeNotNull(phase);
		String backend = System.getenv("SKRIPT_PERSISTENCE_BACKEND");
		assertEquals(Boolean.TRUE, Bukkit.getScheduler().callSyncMethod(Skript.getInstance(), () -> {
			Class<?> expectedStorage = switch (backend) {
				case "CSV" -> FlatFileStorage.class;
				case "SQLite" -> SQLiteStorage.class;
				case "MySQL" -> MySQLStorage.class;
				default -> throw new AssertionError("Unknown backend: " + backend);
			};
			VariablesStorage storage = Variables.STORAGES.stream()
					.filter(candidate -> candidate.accept(PREFIX + "stage")).findFirst().orElseThrow();
			assertEquals("Must not silently fall back to another backend", expectedStorage, storage.getClass());
			if (backend.equals("MySQL") && !phase.equals("recover")) {
				int storedStage = switch (phase) {
					case "seed", "empty" -> 0;
					case "update" -> 1;
					case "journal" -> 2;
					case "delete" -> 3;
					default -> throw new AssertionError("Unknown phase: " + phase);
				};
				try (var pool = new MySQLConnectionPool("127.0.0.1",
						Integer.parseInt(System.getenv("SKRIPT_MYSQL_PORT")), "skript_test", "skript",
						System.getenv("SKRIPT_MYSQL_PASSWORD"), "REQUIRED");
					var connection = pool.acquire();
					var statement = connection.prepareStatement("SELECT name FROM persistence_custom_vars");
					var rows = statement.executeQuery()) {
					var actual = new TreeSet<String>();
					while (rows.next())
						actual.add(rows.getString(1));
					var expected = new TreeSet<String>();
					if (storedStage != 0)
						fixtures(storedStage).keySet().forEach(name -> expected.add(PREFIX + name));
					assertEquals("Rows must be stored in the configured custom table", expected, actual);
				}
			}
			switch (phase) {
				case "seed" -> {
					assertNull(Variables.getVariable(PREFIX + "*", null, false));
					fixtures(1).forEach(PersistenceRestartTest::set);
				}
				case "update" -> {
					check(1);
					fixtures(2).forEach(PersistenceRestartTest::set);
					Variables.deleteVariable(PREFIX + "remove-me", null, false);
					Variables.deleteVariable(PREFIX + "remove-list::*", null, false);
				}
				case "journal" -> {
					assertEquals("MySQL", backend);
					check(2);
					// Model a durable journal left by an interrupted process. The lifecycle
					// tests separately verify that a failed writer produces this journal.
					Map<String, SerializedVariable> changes = new LinkedHashMap<>();
					fixtures(3).forEach((name, value) -> changes.put(PREFIX + name,
							Variables.serializeChange(PREFIX + name, value)));
					changes.put(PREFIX + "list::2", Variables.serializeChange(PREFIX + "list::2", null));
					Path journal = Skript.getInstance().getDataFolder().toPath().resolve("persistence-recovery.bin");
					String target = "127.0.0.1:" + System.getenv("SKRIPT_MYSQL_PORT")
							+ "/skript_test/persistence_custom_vars";
					new MySQLJournal(journal, target).write(changes);
				}
				case "recover" -> check(3);
				case "delete" -> {
					check(backend.equals("MySQL") ? 3 : 2);
					Variables.deleteVariable(PREFIX + "*", null, false);
				}
				case "empty" -> assertNull(Variables.getVariable(PREFIX + "*", null, false));
				default -> throw new AssertionError("Unknown phase: " + phase);
			}
			return true;
		}).get());
		Files.writeString(Path.of(System.getenv("SKRIPT_PERSISTENCE_ACK")), phase);
	}

	private static void set(String name, Object value) {
		Variables.setVariable(PREFIX + name, value, null, false);
	}

	private static Map<String, Object> fixtures(int stage) {
		Map<String, Object> values = new LinkedHashMap<>();
		values.put("stage", (long) stage);
		values.put("text", "stage-" + stage + " 玩家😀");
		values.put("number", 42L * stage);
		values.put("decimal", 12.5);
		values.put("boolean", stage == 1);
		values.put("vector", new Vector(1, 2, 3));
		var world = Bukkit.getWorlds().getFirst();
		values.put("world", world);
		values.put("location", new Location(world, 12.5, 80, -34.5, 90, 15));
		ItemStack item = new ItemStack(Material.DIAMOND, 3);
		var meta = item.getItemMeta();
		meta.setDisplayName("Saved item marker");
		item.setItemMeta(meta);
		values.put("item", item);
		values.put("list::1", stage == 1 ? "first" : "updated");
		if (stage != 3)
			values.put("list::2", "second");
		values.put("list::3", new Vector(1, 2, 3));
		values.put("remove-list", "parent scalar survives list deletion");
		if (stage == 1) {
			values.put("remove-me", "delete me");
			values.put("remove-list::1", "one");
			values.put("remove-list::2", "two");
			values.put("remove-list::nested", "nested scalar");
			values.put("remove-list::nested::1", "nested leaf");
		}
		return values;
	}

	private static void check(int stage) {
		Map<String, Object> expected = fixtures(stage);
		expected.forEach((name, value) -> assertEquals(name, value, Variables.getVariable(PREFIX + name, null, false)));
		if (stage > 1) {
			assertNull(Variables.getVariable(PREFIX + "remove-me", null, false));
			assertNull(Variables.getVariable(PREFIX + "remove-list::*", null, false));
		}
		if (stage == 3)
			assertNull(Variables.getVariable(PREFIX + "list::2", null, false));
	}
}
