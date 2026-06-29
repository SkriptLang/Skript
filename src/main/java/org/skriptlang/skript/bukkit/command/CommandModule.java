package org.skriptlang.skript.bukkit.command;

import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.HierarchicalAddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.command.elements.expressions.ExprArgument;
import org.skriptlang.skript.bukkit.command.elements.structures.StructCommand;

public class CommandModule extends HierarchicalAddonModule {

	public CommandModule(AddonModule parentModule) {
		super(parentModule);
	}

	@Override
	protected void loadSelf(SkriptAddon addon) {
		register(addon,
			ExprArgument::register,
			syntaxRegistry -> StructCommand.register(addon, syntaxRegistry)
		);
	}

	@Override
	public String name() {
		return "command";
	}

}
