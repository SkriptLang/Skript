package org.skriptlang.skript.common;

import ch.njol.skript.Skript;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.SkriptAddon;

import java.io.IOException;

public class CommonModule implements AddonModule {
	@Override
	public void load(SkriptAddon addon) {
		try {
			Skript.getAddonInstance().loadClasses("org.skriptlang.skript.common", "expressions", "conditions");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
