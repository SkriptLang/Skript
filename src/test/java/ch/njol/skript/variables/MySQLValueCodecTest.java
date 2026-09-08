package ch.njol.skript.variables;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.ConfigurationSerializer;
import ch.njol.skript.util.ColorRGB;
import ch.njol.skript.util.SkriptColor;
import ch.njol.yggdrasil.Yggdrasil;
import org.bukkit.Color;
import org.bukkit.util.Vector;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class MySQLValueCodecTest {

	private Yggdrasil registry() {
		Yggdrasil registered = new Yggdrasil();
		registered.registerClassResolver(new ConfigurationSerializer<Color>() {
			{ info = new ClassInfo<>(Color.class, "bukkitcolor"); }
		});
		registered.registerClassResolver(new ConfigurationSerializer<Vector>() {
			{ info = new ClassInfo<>(Vector.class, "vector"); }
		});
		return registered;
	}

	@Test
	public void readableLabelsAndOldEnvelopeRemainCompatible() throws Exception {
		MySQLValueCodec codec = new MySQLValueCodec(registry());
		assertEquals("vector", codec.encode(new Vector(1, 2, 3)).type);
		assertEquals("color", codec.encode(Color.BLUE).type);
		assertEquals("color", codec.encode(ColorRGB.fromRGB(1, 2, 3)).type);
		assertEquals("list", codec.encode(List.of("a")).type);
		assertEquals("map", codec.encode(Map.of("a", 1)).type);
		SerializedVariable.Value current = codec.encode("old value");
		assertEquals("string", current.type);
		byte[] oldPayload = Arrays.copyOfRange(current.data, 8, current.data.length);
		assertEquals("old value", codec.decode(new SerializedVariable.Value(MySQLValueCodec.FORMAT, oldPayload)));
		assertThrows(IOException.class, () -> codec.decode(new SerializedVariable.Value("vector", current.data)));
	}

	@Test
	public void failedMutationIsNotDeletionAndUnreadableRowsDoNotBlockGoodRows() throws Exception {
		List<String> diagnostics = new ArrayList<>();
		try (var handler = new ch.njol.skript.log.LogHandler() {
			@Override
			public LogResult log(ch.njol.skript.log.LogEntry entry) {
				if (entry.getMessage().startsWith("Cannot persist MySQL variable")
						|| entry.getMessage().startsWith("Cannot restore MySQL variable"))
					diagnostics.add(entry.getMessage());
				return LogResult.DO_NOT_LOG;
			}
		}.start()) {
			MySQLValueCodec codec = new MySQLValueCodec(registry());
			PooledMySQLStorage storage = new PooledMySQLStorage(codec);
			assertNull(storage.serializeChange("unsupported", new Object()));
			assertNull(storage.serializeChange("unsupported", new Object()));
			assertNull(storage.serializeChange("unsupported", null).value);
			assertNotNull(storage.serializeChange("unsupported", Color.BLUE).value);
			Map<String, SerializedVariable> rows = new java.util.LinkedHashMap<>();
			rows.put("bad", new SerializedVariable("bad", new SerializedVariable.Value("mysql:yggdrasil:999", new byte[0])));
			rows.put("good::color", new SerializedVariable("good::color", codec.encode(Color.BLUE)));
			assertEquals(Map.of("good::color", Color.BLUE), storage.decodeValues(rows));
			assertEquals(2, rows.size()); // raw unreadable row remains available for recovery
			assertEquals(diagnostics.toString(), 2, diagnostics.size()); // repeated failed mutations are reported once
		}
	}

	@Test
	public void colorsPreserveArgbAndRuntimeTypesAcrossFreshCodec() throws Exception {
		MySQLValueCodec writer = new MySQLValueCodec(registry());
		MySQLValueCodec reader = new MySQLValueCodec(registry());
		for (int argb : new int[]{0, -1, 0x12345678, 0xFF000001}) {
			ColorRGB original = ColorRGB.fromBukkitColor(Color.fromARGB(argb));
			Object restored = reader.decode(writer.encode(original));
			assertEquals(ColorRGB.class, restored.getClass());
			assertEquals(argb, ((ColorRGB) restored).asARGB());
			assertEquals(Color.fromARGB(argb), reader.decode(writer.encode(Color.fromARGB(argb))));
		}
		assertSame(SkriptColor.DARK_RED, reader.decode(writer.encode(SkriptColor.DARK_RED)));
		ColorRGB[] array = {ColorRGB.fromRGB(0, 1, 2), ColorRGB.fromRGB(3, 4, 5)};
		ColorRGB[] restoredArray = (ColorRGB[]) reader.decode(writer.encode(array));
		assertEquals(array[0].asARGB(), restoredArray[0].asARGB());
		assertEquals(array[1].asARGB(), restoredArray[1].asARGB());
	}

	@Test
	public void mixedNestedContainersReuseBukkitSerializers() throws Exception {
		MySQLValueCodec codec = new MySQLValueCodec(registry());
		Object input = Map.of("玩家::nested", List.of("😀", 42, ColorRGB.fromRGB(1, 2, 3),
				List.of(new Vector(1, 2, 3), Color.BLUE, SkriptColor.DARK_RED)));
		Map<?, ?> result = (Map<?, ?>) codec.decode(codec.encode(input));
		List<?> values = (List<?>) result.get("玩家::nested");
		assertEquals("😀", values.get(0));
		assertEquals(42, values.get(1));
		assertEquals(0xFF010203, ((ColorRGB) values.get(2)).asARGB());
		assertEquals(List.of(new Vector(1, 2, 3), Color.BLUE, SkriptColor.DARK_RED), values.get(3));
		assertEquals(Arrays.asList(null, List.of(), Map.of()),
				codec.decode(codec.encode(Arrays.asList(null, List.of(), Map.of()))));
	}

	@Test
	public void journalRestartAndMutationPreserveNestedVariables() throws Exception {
		var path = Files.createTempDirectory("mysql-codec-test").resolve("journal.bin");
		try {
			MySQLValueCodec codec = new MySQLValueCodec(registry());
			MySQLJournal journal = new MySQLJournal(path, "test/database/table");
			for (Object value : new Object[]{ColorRGB.fromRGB(1, 2, 3), Color.BLUE,
					List.of(Color.RED, new Vector(4, 5, 6)), null, SkriptColor.DARK_RED}) {
				journal.write(Map.of("data::nested::color", new SerializedVariable("data::nested::color",
						value == null ? null : codec.encode(value))));
				var loaded = new MySQLJournal(path, "test/database/table").read().get("data::nested::color");
				if (value == null) {
					assertNull(loaded.value);
				} else {
					Object result = new MySQLValueCodec(registry()).decode(loaded.value);
					assertEquals(value.getClass() == ColorRGB.class ? ColorRGB.class : value instanceof List ? ArrayList.class : value.getClass(), result.getClass());
				}
			}
		} finally {
			Files.deleteIfExists(path);
			Files.delete(path.getParent());
		}
	}

	@Test
	public void rejectsUnsupportedMalformedAndFutureDataWithoutChangingOtherValues() throws Exception {
		MySQLValueCodec codec = new MySQLValueCodec(registry());
		assertThrows(IOException.class, () -> codec.encode(new Object()));
		assertThrows(IOException.class, () -> codec.encode(List.of(new Object())));
		List<Object> cycle = new ArrayList<>();
		cycle.add(cycle);
		assertThrows(IOException.class, () -> codec.encode(cycle));
		var good = codec.encode(ColorRGB.fromRGB(1, 2, 3));
		assertThrows(IOException.class, () -> codec.decode(new SerializedVariable.Value("mysql:yggdrasil:999", good.data)));
		assertThrows(IOException.class, () -> codec.decode(new SerializedVariable.Value("unknown-skript-type", good.data)));
		assertThrows(IOException.class, () -> codec.decode(new SerializedVariable.Value(MySQLValueCodec.FORMAT,
				Arrays.copyOf(good.data, good.data.length - 1))));
		byte[] corrupt = good.data.clone();
		corrupt[0] ^= 1;
		assertThrows(IOException.class, () -> codec.decode(new SerializedVariable.Value(MySQLValueCodec.FORMAT, corrupt)));
		byte[] future = good.data.clone();
		future[5] = 127;
		assertThrows(IOException.class, () -> codec.decode(new SerializedVariable.Value(MySQLValueCodec.FORMAT, future)));
		assertThrows(IOException.class, () -> codec.decode(new SerializedVariable.Value(MySQLValueCodec.FORMAT,
				Arrays.copyOf(good.data, good.data.length + 1))));
		assertEquals(0xFF010203, ((ColorRGB) codec.decode(good)).asARGB());
	}

	@Test
	public void largeUnicodeAndRegistryIsolation() throws Exception {
		Yggdrasil registered = registry();
		MySQLValueCodec codec = new MySQLValueCodec(registered);
		String large = "玩家😀\u0000".repeat(100_000);
		assertEquals(large, codec.decode(codec.encode(large)));
		assertFalse(registered.isSerializable(ColorRGB.class));
		assertThrows(IOException.class, () -> registered.getClass("mysql:rgb-color:1"));
	}
}
