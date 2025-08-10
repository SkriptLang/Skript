package org.skriptlang.skript.bukkit.command;

import ch.njol.skript.Skript;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.SkriptAddon;

import java.io.IOException;

/**
 * Module for Brigadier commands.
 */
public class BrigadierModule implements AddonModule {

	@Override
	public void load(SkriptAddon addon) {
		try {
			Skript.getAddonInstance().loadClasses(BrigadierModule.class.getPackageName());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
