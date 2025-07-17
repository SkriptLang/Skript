package ch.njol.skript.skcommand;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Documentation;
import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.PluralizingArgsMessage;
import ch.njol.skript.test.runner.TestMode;
import ch.njol.util.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class SkriptCommand implements TabExecutor {

	public static final String CONFIG_NODE = "skript command";

	private static final Set<SubCommand> SUB_COMMANDS;
	private static final List<String> ALIASES;

	static {
		SUB_COMMANDS = new HashSet<>(Arrays.asList(
			new HelpCommand(),
			new ReloadCommand(),
			new EnableCommand(),
			new DisableCommand(),
			new ListCommand()
		));

		// add a command to generate documentation
		if (TestMode.GEN_DOCS || Documentation.isDocsTemplateFound()) {
			SUB_COMMANDS.add(new GenDocsCommand());
		}

		// add a command to run individual tests
		if (TestMode.DEV_MODE) {
			SUB_COMMANDS.add(new TestCommand());
		}

		// cache subcommand aliases for tab completions
		ALIASES = new ArrayList<>();

		for (SubCommand subCommand : SUB_COMMANDS) {
			ALIASES.addAll(Arrays.asList(subCommand.getAliases()));
		}
	}

	/**
	 * Gets a SubCommand by its alias
	 * @param alias The alias
	 * @return The SubCommand or null if no such alias exists
	 */
	private SubCommand findSubCommand(String alias) {
		for (SubCommand subCommand : SUB_COMMANDS) {
			for (String subCommandAlias : subCommand.getAliases()) {
				if (subCommandAlias.equalsIgnoreCase(alias)) {
					return subCommand;
				}
			}
		}
		return null;
	}

	/**
	 * @return All aliases of all subcommands.
	 */
	public static List<String> getAllAliases() {
		return ALIASES;
	}

	public static void info(CommandSender sender, String what, Object... args) {
		what = args.length == 0
			? Language.get(CONFIG_NODE + "." + what)
			: PluralizingArgsMessage.format(Language.format(CONFIG_NODE + "." + what, args));
		Skript.info(sender, StringUtils.fixCapitalization(what));
	}

	public static void error(CommandSender sender, String what, Object... args) {
		what = args.length == 0
			? Language.get(CONFIG_NODE + "." + what)
			: PluralizingArgsMessage.format(Language.format(CONFIG_NODE + "." + what, args));
		Skript.error(sender, StringUtils.fixCapitalization(what));
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
							 @NotNull String @NotNull [] args) {
		SubCommand subCommand = null;

		if (args.length > 0) {
			subCommand = findSubCommand(args[0]);
		}

		if (subCommand == null) {
			// TODO - send unknown command text
			sender.sendMessage("unknown command");
			return true;
		}

		subCommand.execute(sender, args);
		return true;
	}

	@Override
	public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
												@NotNull String @NotNull [] args) {
		if (args.length == 0 || args[0].isEmpty()) {
			return getAllAliases();
		}

		SubCommand subCommand = findSubCommand(args[0]);
		if (subCommand == null) {
			return List.of();
		}

		return subCommand.getTabCompletions(sender, args);
	}

	/**
	 * Represents a subcommand of the /sk command, like /sk reload
	 */
	public static abstract class SubCommand {

		private final String[] aliases;

		public SubCommand(String... aliases) {
			this.aliases = aliases;
		}

		public String[] getAliases() {
			return aliases;
		}

		/**
		 * Executes this SubCommand
		 * @param sender Source of the command
		 * @param args The arguments
		 */
		public abstract void execute(@NotNull CommandSender sender, @NotNull String @NotNull [] args);

		/**
		 * Returns the tab completions for this SubCommand
		 * @param sender Source of the command
		 * @param args The arguments
		 * @return The tab completions
		 */
		public abstract List<String> getTabCompletions(@NotNull CommandSender sender, @NotNull String @NotNull [] args);

	}

}
