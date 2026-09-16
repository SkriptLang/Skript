package ch.njol.skript.classes;

import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Color;
import ch.njol.skript.util.ColorRGB;
import ch.njol.skript.util.SkriptColor;
import ch.njol.skript.util.Task;
import ch.njol.skript.variables.Variables;
import ch.njol.yggdrasil.Fields;
import ch.njol.yggdrasil.Yggdrasil;
import org.bukkit.Bukkit;
import org.junit.Assume;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.NotSerializableException;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class PolymorphicSerializationTest {

	public static class CustomColor extends ColorRGB {
		int marker;
		CustomColor link;

		public CustomColor() {
			super(org.bukkit.Color.fromRGB(0));
		}

		@Override
		public Fields serialize() {
			Fields fields = super.serialize();
			fields.putPrimitive("marker", marker);
			fields.putObject("link", link);
			return fields;
		}

		@Override
		public void deserialize(Fields fields) throws StreamCorruptedException {
			super.deserialize(fields);
			marker = fields.getPrimitive("marker", int.class);
			link = fields.getObject("link", CustomColor.class);
		}
	}

	public static class UnregisteredColor extends CustomColor {}

	@Test
	public void registeredColorImplementationsKeepTheirIdentity() {
		Assume.assumeNotNull(Bukkit.getServer());
		assertEquals(Boolean.TRUE, Task.callSync(() -> {
			Variables.yggdrasil.registerSingleClass(CustomColor.class, "test:custom-color");
			CustomColor custom = new CustomColor();
			custom.marker = 123;
			custom.link = custom;
			List<Color> colors = new ArrayList<>(List.of(SkriptColor.values()));
			for (int argb : new int[]{0, -1, 0x12345678, 0xff010203})
				colors.add(ColorRGB.fromBukkitColor(org.bukkit.Color.fromARGB(argb)));
			colors.add(custom);

			// A new registry models a restart: no writer-side class or object caches.
			Yggdrasil reader = new Yggdrasil(Variables.YGGDRASIL_VERSION);
			reader.registerSingleClass(SkriptColor.class, "SkriptColor");
			reader.registerSingleClass(ColorRGB.class, "ColorRGB");
			reader.registerSingleClass(CustomColor.class, "test:custom-color");
			reader.registerSingleClass(Color.class, "color");
			for (Color original : colors) {
				var saved = Classes.serialize(original);
				assertNotNull(saved);
				assertEquals("color", saved.type);
				Object restored = Classes.deserialize(saved.type, saved.data);
				assertEquals(original.getClass(), restored.getClass());
				assertEquals(original, restored);
				if (original instanceof SkriptColor)
					assertSame(original, restored);
				if (restored instanceof CustomColor value) {
					assertEquals(123, value.marker);
					assertSame(value, value.link);
				}
				assertEquals(original, read(reader, saved.data));
			}

			Color[] array = colors.toArray(Color[]::new);
			var nested = new ArrayList<>(List.of(colors, array));
			List<?> restored = (List<?>) read(reader, write(Variables.yggdrasil, nested));
			assertEquals(colors, restored.getFirst());
			assertArrayEquals(array, (Color[]) restored.getLast());
			assertThrows(NotSerializableException.class,
					() -> write(Variables.yggdrasil, new UnregisteredColor()));
			assertNull(Classes.deserialize("color", write(Variables.yggdrasil, "wrong type")));

			// Types using the old abbreviated header still round-trip normally.
			for (Object value : List.of("legacy", 42L, true)) {
				var saved = Classes.serialize(value);
				assertNotNull(saved);
				assertEquals(value, Classes.deserialize(saved.type, saved.data));
			}
			return true;
		}));
	}

	@Test
	public void classIdsDoNotRequireInstantiableClasses() throws Exception {
		Yggdrasil registry = new Yggdrasil();
		registry.registerSingleClass(Runnable.class, "runnable");
		assertEquals("runnable", registry.getID(Runnable.class));
		assertFalse(registry.isSerializable(Runnable.class));
		assertSame(Runnable.class, read(registry, write(registry, Runnable.class)));
		assertEquals(Runnable[].class, read(registry, write(registry, new Runnable[0])).getClass());
	}

	private static byte[] write(Yggdrasil registry, Object value) throws Exception {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		try (var out = registry.newOutputStream(bytes)) {
			out.writeObject(value);
		}
		return bytes.toByteArray();
	}

	private static Object read(Yggdrasil registry, byte[] bytes) throws Exception {
		try (var in = registry.newInputStream(new ByteArrayInputStream(bytes))) {
			return in.readObject();
		}
	}
}
