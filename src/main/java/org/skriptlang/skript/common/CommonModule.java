package org.skriptlang.skript.common;

import ch.njol.skript.Skript;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.common.properties.PropertiesModule;

import java.io.IOException;

public class CommonModule implements AddonModule {

	@Override
	public void load(SkriptAddon addon) {
		try {
			Skript.getAddonInstance().loadClasses("org.skriptlang.skript.common", "expressions");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		addon.loadModules(new PropertiesModule());
	}

	@Override
	public String name() {
		return "common";
	}

}
