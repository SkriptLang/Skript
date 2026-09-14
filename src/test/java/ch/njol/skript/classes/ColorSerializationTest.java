package ch.njol.skript.classes;

import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Color;
import ch.njol.skript.util.ColorRGB;
import ch.njol.skript.util.SkriptColor;
import ch.njol.skript.util.Task;
import ch.njol.skript.variables.Variables;
import ch.njol.yggdrasil.Fields;
import ch.njol.yggdrasil.Tag;
import ch.njol.yggdrasil.Yggdrasil;
import org.bukkit.Bukkit;
import org.junit.Assume;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class ColorSerializationTest {

	@Test
	public void parentRegistrationDelegatesBothImplementations() {
		Assume.assumeNotNull(Bukkit.getServer());
		assertEquals(Boolean.TRUE, Task.callSync(() -> {
			ClassInfo<Color> info = Classes.getExactClassInfo(Color.class);
			assertNotNull(info);
			assertNotNull(info.getSerializer());
			List<Color> colors = new ArrayList<>(List.of(SkriptColor.values()));
			for (int argb : new int[]{0, -1, 0x12345678, 0xff010203})
				colors.add(ColorRGB.fromBukkitColor(org.bukkit.Color.fromARGB(argb)));
			for (Color original : colors) {
				assertSame(info, Classes.getSuperClassInfo(original.getClass()));
				var saved = Classes.serialize(original);
				assertNotNull(saved);
				assertEquals("color", saved.type);
				Object restored = Classes.deserialize(info, saved.data);
				assertEquals(original.getClass(), restored.getClass());
				assertEquals(original, restored);
				if (original instanceof SkriptColor)
					assertSame(original, restored);
				assertEquals(original, info.getSerializer().deserialize(Color.class, info.getSerializer().serialize(original)));
				byte[] stream = write(Variables.yggdrasil, original);
				assertEquals(Tag.T_OBJECT.tag, stream[6]);
				assertEquals(original, read(Variables.yggdrasil, stream));
			}
			var nested = new ArrayList<>(List.of(colors, new Color[]{SkriptColor.DARK_RED, colors.getLast()}));
			List<?> restored = (List<?>) read(Variables.yggdrasil, write(Variables.yggdrasil, nested));
			assertEquals(colors, restored.getFirst());
			assertArrayEquals((Color[]) nested.getLast(), (Color[]) restored.getLast());
			return true;
		}));
	}

	@Test
	public void invalidDiscriminatorsAndEnumNamesAreRejected() {
		ColorSerializer serializer = new ColorSerializer();
		for (String implementation : new String[]{"unknown", "named", "rgb"}) {
			Fields fields = new Fields();
			fields.putObject("implementation", implementation);
			fields.putObject("name", "NOT_A_COLOR");
			assertThrows(StreamCorruptedException.class, () -> serializer.deserialize(fields));
		}
	}

	private enum Example { VALUE }

	@Test
	public void enumObjectEncodingRequiresExplicitOptIn() throws Exception {
		for (boolean objectEncoding : new boolean[]{false, true}) {
			AtomicInteger calls = new AtomicInteger();
			Yggdrasil registry = new Yggdrasil();
			registry.registerClassResolver(new EnumSerializer<Example>(Example.class) {
				@Override public Class<Example> getClass(String id) { return "example".equals(id) ? Example.class : null; }
				@Override public String getID(Class<?> type) { return type == Example.class ? "example" : null; }
				@Override public boolean serializeEnumsAsObjects() { return objectEncoding; }
				@Override public boolean canBeInstantiated(Class<? extends Example> type) { return false; }
				@Override public Fields serialize(Example value) { calls.incrementAndGet(); return super.serialize(value); }
				@Override public <E extends Example> E deserialize(Class<E> type, Fields fields) { return type.cast(super.deserialize(fields)); }
			});
			byte[] bytes = write(registry, Example.VALUE);
			assertEquals(objectEncoding ? Tag.T_OBJECT.tag : Tag.T_ENUM.tag, bytes[6]);
			assertEquals(objectEncoding ? 1 : 0, calls.get());
			assertSame(Example.VALUE, read(registry, bytes));
		}
	}

	private static byte[] write(Yggdrasil registry, Object value) throws Exception {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		try (var out = registry.newOutputStream(bytes)) { out.writeObject(value); }
		return bytes.toByteArray();
	}

	private static Object read(Yggdrasil registry, byte[] bytes) throws Exception {
		try (var in = registry.newInputStream(new ByteArrayInputStream(bytes))) { return in.readObject(); }
	}
}
