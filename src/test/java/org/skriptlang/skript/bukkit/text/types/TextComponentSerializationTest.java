package org.skriptlang.skript.bukkit.text.types;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.junit.Test;
import org.skriptlang.skript.bukkit.text.types.TextComponentClassInfo.TextComponentSerializer;

import java.io.StreamCorruptedException;
import java.util.Set;

import static org.junit.Assert.*;

public class TextComponentSerializationTest {

	@Test
	public void test() throws StreamCorruptedException {
		TextComponentSerializer serializer = new TextComponentSerializer();
		Component expected = Component.text("Hello ", Style.style(NamedTextColor.RED)
				.decorations(Set.of(TextDecoration.values()), false).decoration(TextDecoration.BOLD, true))
			.append(Component.text("world!", Style.style(NamedTextColor.BLUE, TextDecoration.BOLD.withState(false))));
		Component deserialized = serializer.deserialize(serializer.serialize(expected));
		assertEquals(expected, deserialized);
	}

}
