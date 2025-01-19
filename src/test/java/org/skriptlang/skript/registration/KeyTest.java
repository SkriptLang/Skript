package org.skriptlang.skript.registration;

import org.junit.Test;
import org.skriptlang.skript.registration.SyntaxRegistry.ChildKey;
import org.skriptlang.skript.registration.SyntaxRegistry.Key;

import static org.junit.Assert.*;

public class KeyTest {

	@Test
	public void testKey() {
		assertEquals("TestKey", Key.of("TestKey").name());
	}

	@Test
	public void testChildKey() {
		final Key<?> key = Key.of("TestKey");
		final ChildKey<?, ?> child = ChildKey.of(key, "TestChildKey");

		assertEquals(key, child.parent());
		assertEquals("TestChildKey", child.name());
	}

}
