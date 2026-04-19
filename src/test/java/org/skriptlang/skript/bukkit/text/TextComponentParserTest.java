package org.skriptlang.skript.bukkit.text;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.*;

public class TextComponentParserTest {

	@Test
	public void testSafeTags() {
		TextComponentParser parser = new TextComponentParser();

		parser.setSafeTags("");
		Component expected = Component.text("<red>Hello <bold>world!");
		Component parsed = parser.parseSafe("<red>Hello <bold>world!");
		assertEquals(expected, parsed);

		parser.setSafeTags("color");
		expected = Component.text("Hello <bold>world!", NamedTextColor.RED);
		parsed = parser.parseSafe("<red>Hello <bold>world!");
		assertEquals(expected, parsed);

		parser.setSafeTags("color", "bold");
		expected = Component.text("Hello ", NamedTextColor.RED)
			.append(Component.text("world!", Style.style(TextDecoration.BOLD)));
		parsed = parser.parseSafe("<red>Hello <bold>world!");
		assertEquals(expected, parsed);
	}

	@Test
	public void testColorsCauseReset() {
		TextComponentParser parser = new TextComponentParser();

		parser.colorsCauseReset(false);
		Component expected = Component.text("Hello ", NamedTextColor.RED, TextDecoration.BOLD)
			.append(Component.text("world!", NamedTextColor.BLUE));
		Component parsed = parser.parse("<red><bold>Hello <blue>world!");
		assertEquals(expected, parsed);

		parser.colorsCauseReset(true);
		expected = Component.text("Hello ", Style.style(NamedTextColor.RED)
				.decorations(Set.of(TextDecoration.values()), false).decoration(TextDecoration.BOLD, true))
			.append(Component.text("world!", Style.style(NamedTextColor.BLUE, TextDecoration.BOLD.withState(false))));
		parsed = parser.parse("<red><bold>Hello <blue>world!");
		assertEquals(expected, parsed);
	}

}
