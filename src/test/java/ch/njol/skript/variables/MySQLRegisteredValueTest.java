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
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

import static org.junit.Assert.*;

/** Tests the production type registry, rather than a backend-specific supported-class list. */
public class MySQLRegisteredValueTest {

	@Test
	public void declaredTypeWarningsRecognizeRegisteredImplementations() {
		Assume.assumeNotNull(Bukkit.getServer());
		assertTrue(Classes.mayBeSerializable(ch.njol.skript.util.Color.class));
		assertTrue(Classes.mayBeSerializable(ColorRGB.class));
		assertTrue(Classes.mayBeSerializable(SkriptColor.class));
		assertTrue(Classes.mayBeSerializable(String.class));
		assertFalse(Classes.mayBeSerializable(org.bukkit.inventory.Inventory.class));
		assertFalse(Classes.mayBeSerializable(Thread.class));
	}

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
				assertEquals(large, LegacyMySQLValueReader.decode(loaded.get("list::1").value));
				assertEquals(42L, LegacyMySQLValueReader.decode(loaded.get("list::2").value));
				assertNull(loaded.get("list::3").value);
				assertNull(Variables.serializeChange("unsupported-test", new Object()));
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
				assertEquals(argb, ((ColorRGB) LegacyMySQLValueReader.decode(serialized)).asARGB());
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
	public void actualOldMysqlEnvelopesRemainReadableWithoutAnOldWriter() {
		Assume.assumeNotNull(Bukkit.getServer());
		assertEquals(Boolean.TRUE, Task.callSync(() -> {
			Properties fixtures = new Properties();
			try (var in = getClass().getResourceAsStream("legacy-mysql.properties")) {
				assertNotNull(in);
				fixtures.load(in);
			}
			Map<String, Object> expected = Map.of("text", "legacy 玩家😀", "rgb", ColorRGB.fromHexString("12345678"),
					"named", SkriptColor.DARK_RED, "nested", List.of("玩家😀", 42L, ColorRGB.fromHexString("12345678")));
			for (var entry : expected.entrySet()) {
				byte[] data = Base64.getDecoder().decode(fixtures.getProperty(entry.getKey() + ".data"));
				var value = new SerializedVariable.Value(fixtures.getProperty(entry.getKey() + ".type"), data);
				assertEquals(entry.getValue(), LegacyMySQLValueReader.decode(value));
				assertEquals(entry.getValue(), LegacyMySQLValueReader.decode(new SerializedVariable.Value(
						"mysql:yggdrasil:1", Arrays.copyOfRange(data, 8, data.length))));
				byte[] broken = data.clone();
				broken[broken.length - 1] ^= 1;
				assertThrows(IOException.class, () -> LegacyMySQLValueReader.decode(new SerializedVariable.Value(value.type, broken)));
			}
			var bad = new SerializedVariable("bad", new SerializedVariable.Value("missing-addon-type", new byte[0]));
			var good = Variables.serializeChange("good", "still readable");
			var rows = Map.of("bad", bad, "good", good);
			assertEquals(Map.of("good", "still readable"), new PooledMySQLStorage().decodeValues(rows));
			assertSame(bad, rows.get("bad"));
			return true;
		}));
	}
}
