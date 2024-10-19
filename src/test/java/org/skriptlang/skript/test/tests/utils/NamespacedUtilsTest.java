package org.skriptlang.skript.test.tests.utils;

import ch.njol.skript.bukkitutil.NamespacedUtils;
import org.bukkit.NamespacedKey;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class NamespacedUtilsTest {

	@Test
	public void testCoder() {
		char[] chars = ",<>?;'\"[]{}\\|=+)(*&^%%$#@!".toCharArray();
		for (char c : chars) {
			String hex = Integer.toHexString(c);
			NamespacedKey encodedKey = NamespacedUtils.createNamespacedKey("skript:test" + c);
			assertEquals(encodedKey.toString(), "skript:test.x"+hex);
			String decodedKey = NamespacedUtils.decodeNamespacedKey(encodedKey).toString();
			assertEquals(decodedKey, "skript:test" + c);
		}
		String dotHex = Integer.toHexString('.');
		String xHex = Integer.toHexString('x');
		NamespacedKey encodedKey = NamespacedUtils.createNamespacedKey("skript:test.x");
		assertEquals(encodedKey.toString(), "skript:test.x" + dotHex + ".x" + xHex);
		String decodedKey = NamespacedUtils.decodeNamespacedKey(encodedKey).toString();
		assertEquals(decodedKey, "skript:test.x");
	}

}
