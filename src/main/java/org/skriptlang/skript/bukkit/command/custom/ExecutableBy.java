package org.skriptlang.skript.bukkit.command.custom;

import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.function.Predicate;

/**
 * Enum describing the types of {@link CommandSender}s that can execute this command.
 */
public enum ExecutableBy {

	/**
	 * This command is only executable by players.
	 */
	PLAYERS(sender -> sender instanceof Player),

	/**
	 * This command is only executable by console.
	 */
	CONSOLE(sender -> sender instanceof ConsoleCommandSender || sender instanceof RemoteConsoleCommandSender),

	/**
	 * This command is only executable by blocks (e.g, command blocks).
	 */
	BLOCKS(sender -> sender instanceof BlockCommandSender);

	private final Predicate<CommandSender> predicate;

	ExecutableBy(Predicate<CommandSender> predicate) {
		this.predicate = predicate;
	}

	/**
	 * @return A predicate to validate the behavior expected by this restriction.
	 */
	public Predicate<CommandSender> predicate() {
		return predicate;
	}

	@Override
	public String toString() {
		return switch (this) {
			case PLAYERS -> "players";
			case CONSOLE -> "the console";
			case BLOCKS -> "blocks";
		};
	}

}
