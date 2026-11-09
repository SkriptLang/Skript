package ch.njol.skript.skcommand;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Documentation;
import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.PluralizingArgsMessage;
import ch.njol.skript.test.runner.TestMode;
import ch.njol.util.StringUtils;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashSet;
import java.util.Set;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

/**
 * The {@code /skript} command and its {@link SubCommand}s.
 */
@ApiStatus.Internal
public class SkriptCommand {

	public static final String CONFIG_NODE = "skript command";
	public static final String PERMISSION = "skript.admin";

	private static final Set<SubCommand> SUB_COMMANDS = new LinkedHashSet<>();

	static {
		SUB_COMMANDS.add(new HelpCommand());
		SUB_COMMANDS.add(new ReloadCommand());
		SUB_COMMANDS.add(new EnableCommand());
		SUB_COMMANDS.add(new DisableCommand());
		SUB_COMMANDS.add(new ListCommand());
		SUB_COMMANDS.add(new InfoCommand());
		SUB_COMMANDS.add(new UpdateCommand());
		SUB_COMMANDS.add(new RestoreCommand());

		if (TestMode.GEN_DOCS || Documentation.isDocsTemplateFound()) {
			SUB_COMMANDS.add(new GenDocsCommand());
		}

		if (TestMode.DEV_MODE) {
			SUB_COMMANDS.add(new TestCommand());
		}
	}

	/**
	 * Builds the {@code /skript} command node.
	 *
	 * @return The built command node.
	 */
	public static @NotNull LiteralCommandNode<CommandSourceStack> build() {
		LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("skript")
			.requires(source -> source.getSender().hasPermission(PERMISSION))
			.executes(context -> {
				HelpCommand.send(context.getSource().getSender());
				return SINGLE_SUCCESS;
			});

		for (SubCommand subCommand : SUB_COMMANDS) {
			for (String alias : subCommand.getAliases()) {
				root.then(subCommand.build(alias));
			}
		}

		return root.build();
	}

	/**
	 * @return All subcommands of {@code /skript}.
	 */
	protected static Set<SubCommand> getSubCommands() {
		return SUB_COMMANDS;
	}

	/**
	 * Sends a message showing all arguments of a subcommand.
	 *
	 * @param sender Who to send it to.
	 * @param subCommand The subcommand to show the arguments of.
	 */
	protected static void sendHelp(CommandSender sender, SubCommand subCommand) {
		String command = subCommand.getAliases()[0];
		String usage = Language.get(CONFIG_NODE + ".usage");
		Skript.message(sender, usage + " <gold>/skript <yellow>" + command + " <red>...");

		for (String arg : subCommand.args()) {
			String description = Language.get(CONFIG_NODE + ".help." + command + "." + arg);
			Skript.message(sender, "   <yellow>" + arg + " <gray>- <white>" + description);
		}
	}

	protected static void info(CommandSender sender, String what, Object... args) {
		what = args.length == 0
			? Language.get(CONFIG_NODE + "." + what)
			: PluralizingArgsMessage.format(Language.format(CONFIG_NODE + "." + what, args));
		Skript.info(sender, StringUtils.fixCapitalization(what));
	}

	protected static void error(CommandSender sender, String what, Object... args) {
		what = args.length == 0
			? Language.get(CONFIG_NODE + "." + what)
			: PluralizingArgsMessage.format(Language.format(CONFIG_NODE + "." + what, args));
		Skript.error(sender, StringUtils.fixCapitalization(what));
	}

}
