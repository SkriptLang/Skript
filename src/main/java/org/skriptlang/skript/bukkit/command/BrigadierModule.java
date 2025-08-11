package org.skriptlang.skript.bukkit.command;

import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.command.elements.DefaultArgumentTypes;
import org.skriptlang.skript.bukkit.command.elements.StructBrigadierCommand;

/**
 * Module for Brigadier commands.
 */
public class BrigadierModule implements AddonModule {

	@Override
	public void load(SkriptAddon addon) {
		StructBrigadierCommand.load(addon);
		DefaultArgumentTypes.String.load(addon);
		DefaultArgumentTypes.Integer.load(addon);
	}

}
