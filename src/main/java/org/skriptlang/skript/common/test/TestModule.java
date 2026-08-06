package org.skriptlang.skript.common.test;

import ch.njol.skript.test.runner.TestMode;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.HierarchicalAddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.common.test.elements.EffCauseRuntime;

public class TestModule extends HierarchicalAddonModule {

	public TestModule(AddonModule parent) {
		super(parent);
	}

	@Override
	protected boolean canLoadSelf(SkriptAddon addon) {
		return TestMode.ENABLED || TestMode.DEV_MODE;
	}

	@Override
	protected void loadSelf(SkriptAddon addon) {
		register(addon, EffCauseRuntime::register);
	}

	@Override
	public String name() {
		return "test";
	}

}
