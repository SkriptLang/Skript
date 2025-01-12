package org.skriptlang.skript;

import ch.njol.skript.SkriptAPIException;
import org.junit.Test;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.addon.BaseSkriptAddonTests;

import static org.junit.Assert.*;

public class SkriptTest extends BaseSkriptAddonTests {

	@Override
	public Skript addon() {
		return Skript.of(source(), name());
	}

	@Override
	public Class<?> source() {
		return SkriptTest.class;
	}

	@Override
	public String name() {
		return "TestSkript";
	}

	@Test
	public void testAddonRegistration() {
		Skript skript = addon();
		Skript unmodifiable = skript.unmodifiableView();

		// should have no addons by default
		assertTrue(skript.addons().isEmpty());
		assertTrue(unmodifiable.addons().isEmpty());

		SkriptAddon addon = skript.registerAddon(SkriptTest.class, "TestAddon");
		assertThrows(UnsupportedOperationException.class, () -> unmodifiable.registerAddon(SkriptTest.class, "TestAddon"));
		assertThrows(SkriptAPIException.class, () -> skript.registerAddon(SkriptAddon.class, "TestAddon"));

		assertTrue(skript.addons().contains(addon));
		// unmodifiable addons list would contain an unmodifiable addon
		assertEquals(1, skript.addons().size());
		assertEquals(1, unmodifiable.addons().size());
	}

}
