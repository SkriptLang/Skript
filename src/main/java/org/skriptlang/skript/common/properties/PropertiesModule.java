package org.skriptlang.skript.common.properties;

import ch.njol.skript.Skript;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.SkriptAddon;

import java.io.IOException;

public class PropertiesModule implements AddonModule {

	@Override
	public void load(SkriptAddon addon) {
		try {
			Skript.getAddonInstance().loadClasses(
				"org.skriptlang.skript.common.properties",
				"expressions", "conditions", "effects");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
