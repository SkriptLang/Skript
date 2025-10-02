package org.skriptlang.skript.common.properties;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptConfig;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.SkriptAddon;

import java.io.IOException;

public class PropertiesModule implements AddonModule {

	@Override
	public boolean canLoad(SkriptAddon addon) {
		return SkriptConfig.useTypeProperties.value();
	}

	@Override
	public void load(SkriptAddon addon) {
		try {
			Skript.getAddonInstance().loadClasses("org.skriptlang.skript.common.properties", "expressions", "conditions");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
