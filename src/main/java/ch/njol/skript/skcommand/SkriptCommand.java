package ch.njol.skript.skcommand;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Documentation;
import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.PluralizingArgsMessage;
import ch.njol.skript.test.runner.TestMode;
import ch.njol.util.StringUtils;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

/**
 * The {@code /skript} command and its subcommands.
 */
@ApiStatus.Internal
public class SkriptCommand {

	public static final String CONFIG_NODE = "skript command";
	public static final String PERMISSION = "skript.admin";

	private static @Nullable LiteralCommandNode<CommandSourceStack> rootNode;

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

		HelpCommand.register(root);
		ReloadCommand.register(root);
		EnableCommand.register(root);
		DisableCommand.register(root);
		ListCommand.register(root);
		InfoCommand.register(root);
		UpdateCommand.register(root);
		RestoreCommand.register(root);

		if (TestMode.GEN_DOCS || Documentation.isDocsTemplateFound())
			GenDocsCommand.register(root);

		if (TestMode.DEV_MODE)
			TestCommand.register(root);

		rootNode = root.build();
		return rootNode;
	}

	/**
	 * @return The last built {@code /skript} command node.
	 */
	static @NotNull LiteralCommandNode<CommandSourceStack> rootNode() {
		LiteralCommandNode<CommandSourceStack> node = rootNode;
		if (node == null)
			node = build();
		return node;
	}

	/**
	 * Registers an alias that redirects to a subcommand's primary node.
	 *
	 * @param root The root command builder.
	 * @param alias The alias to register.
	 * @param primary The primary node to redirect to.
	 */
	static void registerAlias(LiteralArgumentBuilder<CommandSourceStack> root, String alias,
							  LiteralCommandNode<CommandSourceStack> primary) {
		LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal(alias).redirect(primary);
		if (primary.getCommand() != null)
			builder.executes(primary.getCommand());
		root.then(builder.build());
	}

	/**
	 * Sends a message showing all arguments of the subcommand being executed.
	 *
	 * @param context The command context.
	 */
	protected static void sendHelp(CommandContext<CommandSourceStack> context) {
		CommandSender sender = context.getSource().getSender();
		CommandNode<CommandSourceStack> node = context.getNodes().getLast().getNode();
		String command = node.getName();
		String usage = Language.get(CONFIG_NODE + ".usage");
		Skript.message(sender, usage + " <gold>/skript <yellow>" + command + " <red>...");

		for (CommandNode<CommandSourceStack> child : node.getChildren()) {
			String arg = child instanceof LiteralCommandNode<?>
				? child.getName()
				: "<" + child.getName() + ">";
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
