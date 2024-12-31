package org.skriptlang.skript;

import ch.njol.skript.SkriptAPIException;
import org.junit.Assert;
import org.junit.Test;
import org.skriptlang.skript.addon.SkriptAddon;

public class SkriptTest {

	static Skript createSkript() {
		return Skript.of(SkriptTest.class, "TestSkript");
	}

	@Test
	public void testCreation() {
		Skript skript = createSkript();
		Assert.assertEquals(skript.name(), "TestSkript");
		Assert.assertEquals(skript.source(), SkriptTest.class);
	}

	@Test
	public void testAddonRegistration() {
		Skript skript = createSkript();
		Assert.assertTrue(skript.addons().isEmpty());
		SkriptAddon addon = skript.registerAddon(SkriptTest.class, "TestAddon");
		Assert.assertTrue(skript.addons().contains(addon));
		// Same name should result in an error
		Assert.assertThrows(SkriptAPIException.class, () -> skript.registerAddon(SkriptAddon.class, "TestAddon"));
		Assert.assertEquals(1, skript.addons().size());
	}

	@Test
	public void testUnmodifiableView() {
		Skript skript = createSkript().unmodifiableView();
		Assert.assertThrows(UnsupportedOperationException.class,
				() -> skript.registerAddon(SkriptAddon.class, "TestAddon"));
	}

}
