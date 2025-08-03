package ch.njol.skript.command.brigadier;

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
			Skript.getAddonInstance().loadClasses("ch.njol.skript.command.brigadier");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
