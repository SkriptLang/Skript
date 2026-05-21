package org.skriptlang.skript.bukkit.text;

import org.junit.Test;

import static org.junit.Assert.*;

import static org.skriptlang.skript.bukkit.text.TextComponentUtils.replaceLegacyFormattingCodes;

public class TextComponentUtilsTest {

	@Test
	public void testReplaceLegacyFormattingCodes() {
		// validate code escaping
		assertEquals("<red>Hello!", replaceLegacyFormattingCodes("&cHello!"));
		assertEquals("&cHello!", replaceLegacyFormattingCodes("\\&cHello!"));
		assertEquals("\\<red>Hello!", replaceLegacyFormattingCodes("\\\\&cHello!"));
		assertEquals("\\\\&cHello!", replaceLegacyFormattingCodes("\\\\\\&cHello!"));
		assertEquals("<#123456>Hello!", replaceLegacyFormattingCodes("&x&1&2&3&4&5&6Hello!"));
		assertEquals("<#123456><red>Hello!", replaceLegacyFormattingCodes("&x&1&2&3&4&5&6&cHello!"));
		// validate internal metacharacter escaping
		assertEquals("<red>You have $10", replaceLegacyFormattingCodes("&cYou have $10"));
	}

}
