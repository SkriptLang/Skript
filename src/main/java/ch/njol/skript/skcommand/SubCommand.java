package ch.njol.skript.skcommand;

import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.jetbrains.annotations.ApiStatus;

/**
 * Represents a subcommand of the {@code /skript} command, such as {@code /skript reload}.
 */
@ApiStatus.Internal
public abstract class SubCommand {

	private final String[] aliases;

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
	 * Builds this subcommand's node.
	 *
	 * @param literal The name or alias to register this {@link SubCommand} under.
	 * @return The built command node.
	 */
	public abstract LiteralCommandNode<CommandSourceStack> build(String literal);

}
