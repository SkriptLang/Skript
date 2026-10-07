package ch.njol.skript.variables;

import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Color;
import ch.njol.skript.util.ColorRGB;
import ch.njol.skript.util.SkriptColor;
import org.junit.Test;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Map;
import static org.junit.Assert.*;

public class ColorSerializationTest {
	@Test
	public void batchPreservesConcreteColorTypesAndAlpha() {
		Map<String, Object> values = Map.of("named", SkriptColor.BLACK,
			"rgba", ColorRGB.fromRGBA(12, 34, 56, 78), "plain", "value");
		var encoded = Classes.serialize(values);
		assertNotNull(encoded);
		var decoded = Classes.deserialize(encoded);
		assertEquals(values, decoded);
		assertSame(SkriptColor.BLACK, decoded.get("named"));
		assertEquals(78, ((Color) decoded.get("rgba")).getAlpha());
	}

	@Test
	public void interfaceArraysRestoreTheirConcreteElements() throws Exception {
		Color[] colors = {SkriptColor.BLACK, ColorRGB.fromRGBA(1, 2, 3, 4)};
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		try (var output = Variables.yggdrasil.newOutputStream(bytes)) {
			output.writeObject(colors);
		}
		try (var input = Variables.yggdrasil.newInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
			assertArrayEquals(colors, input.readObject(Color[].class));
		}
	}
}
