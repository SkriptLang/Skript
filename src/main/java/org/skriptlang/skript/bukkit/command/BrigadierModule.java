package org.skriptlang.skript.bukkit.command;

import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.command.elements.DefaultArgumentTypes;
import org.skriptlang.skript.bukkit.command.elements.StructBrigadierCommand;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

/**
 * Module for Brigadier commands.
 */
public class BrigadierModule implements AddonModule {

	@Override
	public void load(SkriptAddon addon) {
		addon.syntaxRegistry().register(SyntaxRegistry.STRUCTURE,
			DefaultSyntaxInfos.Structure.builder(StructBrigadierCommand.class)
				.supplier(StructBrigadierCommand::new)
				.build());
		DefaultArgumentTypes.String.load(addon);
		DefaultArgumentTypes.Integer.load(addon);
	}

}
