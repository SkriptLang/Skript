package org.skriptlang.skript.test.tests.utils;

import ch.njol.skript.Skript;
import ch.njol.skript.bukkitutil.NamespacedUtils;
import ch.njol.util.Pair;
import org.bukkit.NamespacedKey;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class NamespacedUtilsTest {

	@Test
	public void testCoder() {
		char[] chars = ",<>?;'\"[]{}\\|=+)(*&^%%$#@!".toCharArray();
		for (char c : chars) {
			String hex = Integer.toHexString(c);
			NamespacedKey encodedKey = NamespacedUtils.createNamespacedKey("test" + c);
			assertEquals(encodedKey.toString(), "skript:test.x"+hex);
			Pair<String, String> decodedKey = NamespacedUtils.decodeNamespacedKey(encodedKey);
			String combinedDecode = decodedKey.getKey() + ":" + decodedKey.getValue();
			assertEquals(combinedDecode, "skript:test" + c);
		}
		String dotHex = Integer.toHexString('.');
		String xHex = Integer.toHexString('x');
		NamespacedKey encodedKey = NamespacedUtils.createNamespacedKey("test.x");
		assertEquals(encodedKey.toString(), "skript:test.x" + dotHex + ".x" + xHex);
		Pair<String, String> decodedKey = NamespacedUtils.decodeNamespacedKey(encodedKey);
		String combinedDecode = decodedKey.getKey() + ":" + decodedKey.getValue();
		assertEquals(combinedDecode, "skript:test.x");
	}

}