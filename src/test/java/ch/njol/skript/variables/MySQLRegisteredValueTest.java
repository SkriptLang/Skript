package ch.njol.skript.variables;

import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.ColorRGB;
import ch.njol.skript.util.SkriptColor;
import ch.njol.skript.util.Task;
import org.bukkit.Bukkit;
import org.junit.Assume;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
	public void colorsUseTheNormalSerializerAndNestedYggdrasilCollections() {
		Assume.assumeNotNull(Bukkit.getServer());
		assertEquals(Boolean.TRUE, Task.callSync(() -> {
			for (int argb : new int[]{0, -1, 0x12345678, 0xff010203}) {
				ColorRGB color = ColorRGB.fromBukkitColor(org.bukkit.Color.fromARGB(argb));
				var serialized = Classes.serialize(color);
				assertNotNull(serialized);
				assertEquals(color, Classes.deserialize(serialized.type, serialized.data));
				assertEquals(argb, ((ColorRGB) Classes.deserialize(serialized.type, serialized.data)).asARGB());
			}
			var named = Classes.serialize(SkriptColor.DARK_RED);
			assertNotNull(named);
			assertSame(SkriptColor.DARK_RED, Classes.deserialize(named.type, named.data));
			// Existing Yggdrasil collection serializers remain usable inside registered values.
			var nested = new ArrayList<>(Arrays.asList("玩家😀", null, ColorRGB.fromRGB(1, 2, 3),
					new HashMap<>(Map.of("named", SkriptColor.DARK_RED))));
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			try (var out = Variables.yggdrasil.newOutputStream(bytes)) {
				out.writeObject(nested);
			}
			try (var in = Variables.yggdrasil.newInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
				assertEquals(nested, in.readObject());
			}
			// Yggdrasil collection support alone does not register a top-level Skript variable type.
			assertNull(Classes.serialize(nested));
			return true;
		}));
	}

	@Test
	public void unreadableRowsDoNotPreventOtherValuesFromLoading() {
		Assume.assumeNotNull(Bukkit.getServer());
		assertEquals(Boolean.TRUE, Task.callSync(() -> {
			var bad = new SerializedVariable("bad", new SerializedVariable.Value("missing-addon-type", new byte[0]));
			var good = Variables.serializeChange("good", "still readable");
			var rows = Map.of("bad", bad, "good", good);
			assertEquals(Map.of("good", "still readable"), new PooledMySQLStorage().decodeValues(rows));
			assertSame(bad, rows.get("bad"));
			return true;
		}));
	}
}
