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

	@Test
	public void testLegacy() {
		TextComponentParser parser = new TextComponentParser();
		assertEquals(parser.parse("<red>hello"), parser.parse("&chello"));
		assertEquals(parser.parse("<#123456>hello"), parser.parse("&x&1&2&3&4&5&6hello"));
		assertEquals(Component.text("&chello"), parser.parse("\\&chello"));
		assertEquals(Component.text("&x&1&2&3&4&5&6hello"), parser.parse("&x\\&1\\&2\\&3\\&4\\&5\\&6hello"));
	}

	@Test
	public void testLegacyEscaping() {
		TextComponentParser parser = new TextComponentParser();
		assertEquals("\\&chello", parser.escape("&chello"));
		assertEquals("&x\\&1\\&2\\&3\\&4\\&5\\&6hello &x", parser.escape("&x&1&2&3&4&5&6hello &x"));
	}

	@Test
	public void testLegacyDoubleHashtag() {
		TextComponentParser parser = new TextComponentParser();
		assertEquals(parser.parse("<#123456>hello"), parser.parse("<##123456>hello"));
		assertEquals("\\<##123456>hello", parser.escape("<##123456>hello"));
	}

}
