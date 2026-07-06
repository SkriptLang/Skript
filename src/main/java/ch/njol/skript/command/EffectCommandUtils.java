package ch.njol.skript.command;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.config.Config;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.log.RetainingLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.variables.Variables;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.skriptlang.skript.bukkit.text.TextComponentParser;
import org.skriptlang.skript.lang.script.Script;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Handles the execution of an effect command.
 */
public class EffectCommandUtils {

	private static final Script DUMMY_SCRIPT = ScriptLoader.createDummyScript("effect_command", null);

	/**
	 * Whether effect commands can be used and if {@code message} is an effect command.
	 * @param message The {@link String} to check if it's an effect command.
	 * @return {@code true} if can be used, otherwise {@code false}.
	 */
	static boolean canUse(String message) {
		if (!Skript.testing() && !SkriptConfig.enableEffectCommands.value())
			return false;
		return isPrefixed(message);
	}

	/**
	 * Whether the {@code message} is any type of effect command.
	 * @param message The {@link String} to check.
	 * @return {@code true} if it is, otherwise {@code false}.
	 * @see #isSingularPrefixed(String)
	 * @see #isMultiPrefixed(String)
	 */
	static boolean isPrefixed(String message) {
		return isMultiPrefixed(message) || isSingularPrefixed(message);
	}

	/**
	 * Whether the {@code message} is a singular effect command
	 * Indicated by starting with {@link SkriptConfig#effectCommandToken}.
	 * @param message The {@link String} to check.
	 * @return {@code true} if it is, otherwise {@code false}.
	 */
	static boolean isSingularPrefixed(String message) {
		return message.startsWith(SkriptConfig.effectCommandToken.value());
	}

	/**
	 * Whether the {@code message} is a multi effect command
	 * Indicated by starting with {@link SkriptConfig#multilineEffectCommandToken}.
	 * @param message The {@link String} to check.
	 * @return {@code true} if it is, otherwise {@code false}.
	 */
	static boolean isMultiPrefixed(String message) {
		return message.startsWith(SkriptConfig.multilineEffectCommandToken.value());
	}

	/**
	 * Handles the effect command and redirects to the correlating handler.
	 * @param sender The {@link CommandSender} executing the effect command.
	 * @param command The command.
	 * @return {@code true} if able to use effect commands, otherwise {@code false}.
	 */
	static boolean handleEffectCommand(CommandSender sender, String command) {
		if (!(Skript.testing() || sender instanceof ConsoleCommandSender || sender.hasPermission("skript.effectcommands") || SkriptConfig.allowOpsToUseEffectCommands.value() && sender.isOp()))
			return false;
		if (isMultiPrefixed(command)) {
			handleMultiEffectCommand(sender, command);
		} else {
			handleSingularEffectCommand(sender, command);
		}
		return true;
	}

	/**
	 * Handles singular effect commands, indicated by {@link SkriptConfig#effectCommandToken}.
	 * @param sender The {@link CommandSender} executing the effect command.
	 * @param command The command to execute.
	 */
	static void handleSingularEffectCommand(CommandSender sender, String command) {
		try {
			command = command.substring(SkriptConfig.effectCommandToken.value().length()).trim();
			RetainingLogHandler log = SkriptLogger.startRetainingLog();
			try {
				// Call the event on the Bukkit API for addon developers.
				EffectCommandEvent effectCommand = new EffectCommandEvent(sender, command);
				Bukkit.getPluginManager().callEvent(effectCommand);
				command = effectCommand.getCommand();
				ParserInstance parser = ParserInstance.get();
				parser.setActive(DUMMY_SCRIPT);
				parser.setCurrentEvent("effect command", EffectCommandEvent.class);
				Effect effect = Effect.parse(command, null);
				parser.deleteCurrentEvent();

				TextComponentParser textParser = TextComponentParser.instance();
				if (effect != null) {
					log.clear(); // ignore warnings and stuff
					log.printLog();
					if (!effectCommand.isCancelled()) {
						sender.sendMessage(textParser.parse("<gray>Executing '" + textParser.escape(command) + "'"));
						if (SkriptConfig.logEffectCommands.value() && !(sender instanceof ConsoleCommandSender))
							Skript.info(sender.getName() + " issued effect command: " + textParser.escape(command));
						TriggerItem.walk(effect, effectCommand);
						Variables.removeLocals(effectCommand);
					} else {
						sender.sendMessage(textParser.parse("<red>Your effect command '" + textParser.escape(command) + "' was cancelled."));
					}
				} else {
					if (sender == Bukkit.getConsoleSender()) // log as SEVERE instead of INFO like printErrors below
						// No need to escape command here, logger will do it
						SkriptLogger.LOGGER.severe("Error in: " + command);
					else
						sender.sendMessage(textParser.parse("<red>Error in: <gray>" + textParser.escape(command)));
					// TODO errors likely need to be escaped too
					log.printErrors(sender, "(No specific information is available)");
				}
				parser.setInactive();
			} finally {
				log.stop();
			}
		} catch (Exception e) {
			Skript.exception(e, "Unexpected error while executing effect command '" + TextComponentParser.instance().escape(command) + "' by '" + sender.getName() + "'");
			sender.sendRichMessage("<red>An internal error occurred while executing this effect. Please refer to the server log for details.");
		}
	}

	/**
	 * Handles multi effect commands, indicated by {@link SkriptConfig#multilineEffectCommandToken}.
	 * @param sender The {@link CommandSender} executing the effect command.
	 * @param command The command to add to the {@link MultiEffectBuilder} until finalized.
	 */
	static void handleMultiEffectCommand(CommandSender sender, String command) {
		command = command.substring(SkriptConfig.multilineEffectCommandToken.value().length()).trim();
		if (command.isBlank()) {
			if (!MultiEffectBuilder.hasBuilder(sender))
				return;
			MultiEffectBuilder builder = MultiEffectBuilder.getBuilder(sender);
			if (builder.getLines().isEmpty())
				return;
			finalizeMultiEffect(sender, builder);
			return;
		}
		MultiEffectBuilder builder = MultiEffectBuilder.getBuilder(sender);
		if (!builder.getLines().isEmpty()) {
			String tabToken = SkriptConfig.multilineEffectTabToken.value();
			int tabs = 0;
			while (command.startsWith(tabToken)) {
				command = command.substring(tabToken.length()).trim();
				tabs++;
			}
			command = "\t".repeat(tabs) + command;
		}
		builder.addLine(command);
		if (sender instanceof Player) {
			TextComponentParser textParser = TextComponentParser.instance();
			sender.sendMessage(textParser.parse("<gray>Current Session:\n" + textParser.escape(builder.joinLines(1, "  "))));
		}
	}

	/**
	 * Finalizes a multi effect command by building the {@code builder} to parse and execute.
	 * @param sender The {@link CommandSender} executing the effect command.
	 * @param builder The {@link MultiEffectBuilder} containing all the effects to parse and execute.
	 */
	private static void finalizeMultiEffect(CommandSender sender, MultiEffectBuilder builder) {
		MultiEffectCommandEvent event = builder.build();
		Bukkit.getPluginManager().callEvent(event);
		String string = builder.joinLines();
		String tabbedString = builder.joinLines(2, "  ");
		ParserInstance parser = ParserInstance.get();
		RetainingLogHandler log = SkriptLogger.startRetainingLog();
		try {
			Config config = new Config(
				new ByteArrayInputStream(string.getBytes(StandardCharsets.UTF_8)),
				"effect_command",
				true,
				false,
				":"
			);
			SectionNode sectionNode = config.getMainNode();
			parser.setActive(DUMMY_SCRIPT);
			parser.setCurrentEvent("effect command", MultiEffectCommandEvent.class);
			List<TriggerItem> items = ScriptLoader.loadItems(sectionNode);

			TextComponentParser textParser = TextComponentParser.instance();
			if (log.hasErrors()) {
				log.printErrors(sender, "(No specific information is available)");
			} else {
				if (!event.isCancelled()) {
					log.clear();
					log.printLog();
					sender.sendMessage(textParser.parse("<gray>Executing:\n" + textParser.escape(tabbedString)));
					if (SkriptConfig.logEffectCommands.value() && !(sender instanceof ConsoleCommandSender))
						Skript.info(sender.getName() + " issued effect command:\n" + textParser.escape(tabbedString));
					Trigger trigger = new Trigger(DUMMY_SCRIPT, sectionNode.getKey(), null, items);
					trigger.execute(event);
					parser.deleteCurrentEvent();
					parser.setInactive();
				} else {
					sender.sendMessage(textParser.parse("<red>Your effect command was cancelled:\n" + textParser.escape(tabbedString)));
				}
			}
		} catch (Exception e) {
			Skript.exception(e, "Unexpected error while executing effect command by '" + sender.getName() + "':\n" + TextComponentParser.instance().escape(tabbedString));
			sender.sendRichMessage("<red>An internal error occurred while executing this effect. Please refer to the server log for details.");
		} finally {
			log.stop();
		}
	}

}
