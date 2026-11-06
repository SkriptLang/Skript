package ch.njol.skript.command;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.config.Config;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.localization.Language;
import ch.njol.skript.log.RetainingLogHandler;
import ch.njol.skript.log.SkriptLogger;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
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
	 * Whether the {@code sender} can use effect commands or not.
	 * @param sender The {@link CommandSender} to check.
	 * @return {@code true} if the sender can use effect commands, otherwise {@code false}.
	 */
	static boolean canUse(CommandSender sender) {
		return (Skript.testing() || sender instanceof ConsoleCommandSender || sender.hasPermission("skript.effectcommands") || SkriptConfig.allowOpsToUseEffectCommands.value() && sender.isOp());
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
		if (!canUse(sender))
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
		command = command.substring(SkriptConfig.effectCommandToken.value().length()).trim();
		finalizeEffectCommand(sender, command, false);
	}

	/**
	 * Handles multi effect commands, indicated by {@link SkriptConfig#multilineEffectCommandToken}.
	 * @param sender The {@link CommandSender} executing the effect command.
	 * @param command The command to add to the {@link MultiEffectBuilder} until finalized.
	 */
	static void handleMultiEffectCommand(CommandSender sender, String command) {
		command = command.substring(SkriptConfig.multilineEffectCommandToken.value().length()).trim();
		if (command.isBlank())
			return;
		// Process command
		MultiEffectBuilder builder = MultiEffectBuilder.getBuilder(sender);
		if (command.equalsIgnoreCase("end")) {
			if (builder.getLines().isEmpty())
				return;
			finalizeEffectCommand(sender, builder.build(), true);
			return;
		}
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
	 * Handles the finalization of effect commands for parsing and executing.
	 * @param sender The {@link CommandSender} executing the effect command.
	 * @param code The code to be executed.
	 * @param isMultiline Whether the code being executed is multilined or not.
	 */
	static void finalizeEffectCommand(CommandSender sender, String code, boolean isMultiline) {
		EffectCommandEvent event;
		Class<? extends Event> eventClass;
		if (isMultiline) {
			event = new MultiEffectCommandEvent(sender, code);
			eventClass = MultiEffectCommandEvent.class;
		} else {
			event = new EffectCommandEvent(sender, code);
			eventClass = EffectCommandEvent.class;
		}
		Bukkit.getPluginManager().callEvent(event);
		String tabbedCode = "  " + code.replace("\t", "  ").replace("\n", "\n  ");
		ParserInstance parser = ParserInstance.get();
		RetainingLogHandler log = SkriptLogger.startRetainingLog();
		try {
			Config config = new Config(
				new ByteArrayInputStream(code.getBytes(StandardCharsets.UTF_8)),
				"effect_command",
				true,
				false,
				":"
			);
			if (log.hasErrors()) {
				log.printErrors(sender, "(No specific information is available)");
				return;
			}
			SectionNode sectionNode = config.getMainNode();
			parser.setActive(DUMMY_SCRIPT);
			parser.setCurrentEvent("effect command", eventClass);
			List<TriggerItem> items = ScriptLoader.loadItems(sectionNode);

			TextComponentParser textParser = TextComponentParser.instance();
			if (log.hasErrors()) {
				log.printErrors(sender, "(No specific information is available)");
			} else {
				if (!event.isCancelled()) {
					log.clear();
					log.printLog();
					sender.sendMessage(Language.format("effect commands.executing", textParser.escape(tabbedCode)));
					if (SkriptConfig.logEffectCommands.value() && !(sender instanceof ConsoleCommandSender))
						Skript.info(Language.format("effect commands.log", sender.getName(), textParser.escape(tabbedCode)));
					Trigger trigger = new Trigger(DUMMY_SCRIPT, sectionNode.getKey(), null, items);
					trigger.execute(event);
					parser.deleteCurrentEvent();
					parser.setInactive();
				} else {
					sender.sendMessage(Language.format("effect commands.cancelled", textParser.escape(tabbedCode)));
				}
			}
		} catch (Exception e) {
			Skript.exception(e, "Unexpected error while executing effect command by '" + sender.getName()
				+ "':\n" + TextComponentParser.instance().escape(tabbedCode));
			sender.sendRichMessage(Language.get("effect commands.exception"));
		} finally {
			log.stop();
		}
	}

}
