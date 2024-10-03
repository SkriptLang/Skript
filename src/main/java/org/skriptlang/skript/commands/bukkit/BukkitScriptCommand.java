package org.skriptlang.skript.commands.bukkit;

import ch.njol.skript.Skript;
import ch.njol.skript.command.CommandUsage;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.VariableString;
import ch.njol.skript.patterns.SkriptPattern;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.commands.api.Argument;
import org.skriptlang.skript.commands.api.CommandCooldown;
import org.skriptlang.skript.commands.api.ScriptCommand;
import org.skriptlang.skript.commands.api.ScriptCommandSender;
import org.skriptlang.skript.commands.api.ScriptCommandSender.CommandSenderType;

import java.util.List;

/**
 * A Bukkit implementation of {@link ScriptCommand}.
 * Since Skript is currently Bukkit-only, this implementation doesn't do anything special.
 * This structure is mainly to support further decoupling of Skript from Bukkit.
 */
public class BukkitScriptCommand extends ScriptCommand {

	public BukkitScriptCommand(
		String label, @Nullable String namespace, String description, CommandUsage usage, List<String> aliases,
		List<CommandSenderType> executableBy, @Nullable String permission, @Nullable VariableString permissionMessage,
		@Nullable CommandCooldown cooldown, Trigger trigger, List<Argument<?>> arguments, SkriptPattern pattern
	) {
		super(
			label, namespace, description, usage, aliases,
			executableBy, permission, permissionMessage,
			cooldown, trigger, arguments, pattern
		);
	}

	@Override
	public void execute(ScriptCommandSender sender, String label, String[] args) {
		if (!Bukkit.isPrimaryThread()) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(Skript.getInstance(),
				() -> execute_i(sender, label, args)
			);
			return;
		}
		execute_i(sender, label, args);
	}

}
