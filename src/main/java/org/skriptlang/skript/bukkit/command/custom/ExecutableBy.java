package org.skriptlang.skript.bukkit.command.custom;

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
	 * This command is executable by any {@link CommandSender}.
	 */
	ALL(ignored -> true),

	/**
	 * This command is not executable.
	 */
	NONE(ignored -> false);

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

	/**
	 * Combines (ORs) this ExecutableBy with another.
	 * For example, {@link #PLAYERS}.with({@link #CONSOLE}) is {@link #ALL}.
	 *
	 * @param other Other to OR with.
	 * @return The resulting ExecutableBy.
	 */
	public ExecutableBy with(ExecutableBy other) {
		if (this == NONE) {
			return other;
		} else if (other == NONE) {
			return this;
		}
		return this == other ? this : ALL;
	}

	/**
	 * @param other Subset ExecutableBy to consider.
	 * @return Whether this ExecutableBy is the same as or covers a superset of {@code other}.
	 */
	public boolean includes(ExecutableBy other) {
		return this == ALL || this == other;
	}

	@Override
	public String toString() {
		return switch (this) {
			case PLAYERS -> "players";
			case CONSOLE -> "the console";
			case ALL -> "the console and players";
			case NONE -> "none";
		};
	}

}
