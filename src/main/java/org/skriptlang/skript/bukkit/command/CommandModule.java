package org.skriptlang.skript.bukkit.command;

import ch.njol.skript.Skript;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.HierarchicalAddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.command.brigadier.RuntimeCommandRegistrar;
import org.skriptlang.skript.bukkit.command.elements.effects.EffCancelCooldown;
import org.skriptlang.skript.bukkit.command.elements.expressions.ExprArgument;
import org.skriptlang.skript.bukkit.command.elements.expressions.ExprCmdCooldownInfo;
import org.skriptlang.skript.bukkit.command.elements.structures.StructCommand;

public class CommandModule extends HierarchicalAddonModule {

	public CommandModule(AddonModule parentModule) {
		super(parentModule);
	}

	@Override
	protected void loadSelf(SkriptAddon addon) {
		RuntimeCommandRegistrar.init(Skript.getInstance());

		register(addon,
			EffCancelCooldown::register,
			ExprArgument::register,
			ExprCmdCooldownInfo::register,
			syntaxRegistry -> StructCommand.register(addon, syntaxRegistry)
		);
	}

	@Override
	public String name() {
		return "command";
	}

}
