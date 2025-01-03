package org.skriptlang.skript;

import ch.njol.skript.SkriptAPIException;
import org.junit.Test;
import org.skriptlang.skript.addon.SkriptAddon;

import static org.junit.Assert.*;

public class SkriptTest {

	static Skript createSkript() {
		return Skript.of(SkriptTest.class, "TestSkript");
	}

	@Test
	public void testCreation() {
		Skript skript = createSkript();
		assertEquals(skript.name(), "TestSkript");
		assertEquals(skript.source(), SkriptTest.class);
	}

	@Test
	public void testAddonRegistration() {
		Skript skript = createSkript();
		assertTrue(skript.addons().isEmpty());
		SkriptAddon addon = skript.registerAddon(SkriptTest.class, "TestAddon");
		assertTrue(skript.addons().contains(addon));
		// Same name should result in an error
		assertThrows(SkriptAPIException.class, () -> skript.registerAddon(SkriptAddon.class, "TestAddon"));
		assertEquals(1, skript.addons().size());
	}

	@Test
	public void testUnmodifiableView() {
		Skript skript = createSkript().unmodifiableView();
		assertThrows(UnsupportedOperationException.class,
				() -> skript.registerAddon(SkriptAddon.class, "TestAddon"));
	}

}
