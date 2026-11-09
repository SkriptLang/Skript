package ch.njol.skript.skcommand;

import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;

/**
 * Represents a subcommand of the {@code /skript} command, such as {@code /skript reload}.
 */
abstract class SubCommand {

	private final String[] aliases;
	private String[] args;

	protected SubCommand(String... aliases) {
		this.aliases = aliases;
	}

	/**
	 * @return All aliases of this subcommand.
	 */
	public String[] getAliases() {
		return aliases;
	}

	/**
	 * Sets the arguments shown when this subcommand is executed without arguments.
	 *
	 * @param args The arguments to show in the help message.
	 */
	protected void args(String... args) {
		this.args = args;
	}

	/**
	 * @return The arguments shown when this subcommand is executed without arguments,
	 *         or {@code null} if this subcommand does not have any.
	 */
	public String[] args() {
		return args;
	}

	/**
	 * Builds this subcommand's node.
	 *
	 * @param literal The name or alias to register this {@link SubCommand} under.
	 * @return The built command node.
	 */
	public abstract LiteralCommandNode<CommandSourceStack> build(String literal);

}
