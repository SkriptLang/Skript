package org.skriptlang.skript.bukkit.command;

import ch.njol.skript.Skript;
import ch.njol.skript.command.Commands;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.HierarchicalAddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.command.custom.ScriptCommandRegistrar;
import org.skriptlang.skript.bukkit.command.elements.conditions.*;
import org.skriptlang.skript.bukkit.command.elements.effects.*;
import org.skriptlang.skript.bukkit.command.elements.expressions.*;
import org.skriptlang.skript.bukkit.command.elements.structures.*;

public class CommandModule extends HierarchicalAddonModule {

	public CommandModule(AddonModule parentModule) {
		super(parentModule);
	}

	@Override
	protected void initSelf(SkriptAddon addon) {
		Commands.registerListeners();
	}

	@Override
	protected void loadSelf(SkriptAddon addon) {
		ScriptCommandRegistrar.init(Skript.getInstance());

		register(addon,
			CondIsScriptCommand::register,
			EffCancelCooldown::register,
			EffCommand::register,
			ExprAllCommands::register,
			ExprArgument::register,
			ExprCmdCooldownInfo::register,
			ExprCommand::register,
			ExprCommandInfo::register,
			syntaxRegistry -> StructCommand.register(addon, syntaxRegistry)
		);
	}

	@Override
	public String name() {
		return "command";
	}

}
