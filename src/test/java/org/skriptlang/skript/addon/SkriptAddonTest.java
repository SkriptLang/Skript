package org.skriptlang.skript.addon;

import org.skriptlang.skript.Skript;

public class SkriptAddonTest extends BaseSkriptAddonTests {

	@Override
	public SkriptAddon addon() {
		return Skript.of(source(), name())
				.registerAddon(source(), name());
	}

	@Override
	public Class<?> source() {
		return SkriptAddonTest.class;
	}

	@Override
	public String name() {
		return "TestAddon";
	}

}
