package org.skriptlang.skript.bukkit.whitelist;

import java.io.IOException;

import ch.njol.skript.Skript;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.SkriptAddon;

public class WhitelistModule implements AddonModule {

	@Override
	public void load(SkriptAddon addon) {
		try {
			Skript.getAddonInstance().loadClasses("org.skriptlang.skript.bukkit.whitelist", "elements");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
