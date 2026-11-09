package ch.njol.skript.skcommand;

import ch.njol.skript.localization.Language;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;

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
public class HelpCommand extends SubCommand {

	HelpCommand() {
		super("help");
	}

	@Override
	public LiteralCommandNode<CommandSourceStack> build(String literal) {
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

		for (SubCommand subCommand : SkriptCommand.getSubCommands()) {
			String command = String.join("/", subCommand.getAliases());
			String description = getDescription(subCommand.getAliases()[0]);
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
