package ch.njol.skript.skcommand;

import ch.njol.skript.localization.Language;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

/**
 * Lists all Skript commands
 * <p>
 * Usage:
 * <pre>
 * /sk
 * /sk help
 * </pre>
 */
class HelpCommand {

	static void register(LiteralArgumentBuilder<CommandSourceStack> root) {
		root.then(build("help"));
	}

	static LiteralCommandNode<CommandSourceStack> build(String literal) {
		return Commands.literal(literal)
			.executes(context -> {
				send(context.getSource().getSender());
				return SINGLE_SUCCESS;
			})
			.build();
	}

	/**
	 * Sends the command list to the given sender.
	 *
	 * @param sender Who to send the list to.
	 */
	static void send(CommandSender sender) {
		String usage = Language.get(SkriptCommand.CONFIG_NODE + ".usage");
		sender.sendRichMessage(usage + " <gold>/skript <red>...");

		LiteralCommandNode<CommandSourceStack> root = SkriptCommand.rootNode();
		for (CommandNode<CommandSourceStack> child : root.getChildren()) {
			if (child.getRedirect() != null)
				continue;

			List<String> aliases = new ArrayList<>();
			aliases.add(child.getName());
			for (CommandNode<CommandSourceStack> sibling : root.getChildren()) {
				if (sibling.getRedirect() == child)
					aliases.add(sibling.getName());
			}

			String command = String.join("/", aliases);
			String description = getDescription(child.getName());
			sender.sendRichMessage("   <yellow>" + command + " <dark_gray>- <white>" + description);
		}
	}

	private static String getDescription(String command) {
		String description = Language.get_(SkriptCommand.CONFIG_NODE + ".help." + command + ".description");
		if (description == null)
			return Language.get(SkriptCommand.CONFIG_NODE + ".help." + command);
		return description;
	}

}
