package org.skriptlang.skript.bukkit.log.runtime;

import ch.njol.skript.localization.ArgsMessage;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript.log.runtime.ErrorSource;
import org.skriptlang.skript.log.runtime.ErrorSource.Location;
import org.skriptlang.skript.log.runtime.Frame.FrameOutput;
import org.skriptlang.skript.log.runtime.RuntimeError;
import org.skriptlang.skript.log.runtime.RuntimeErrorConsumer;

import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * An implementation for Bukkit based platforms.
 * Sends errors to console with formatted colours and a notification chat message to
 * users with the see_runtime_errors permission.
 */
public class BukkitRuntimeErrorConsumer implements RuntimeErrorConsumer {

	public static final String ERROR_NOTIF_PERMISSION = "skript.see_runtime_errors";

	public static final String CONFIG_NODE = "log.runtime";
	public static final ArgsMessage WARNING_DETAILS = new ArgsMessage("skript command.reload.warning details");
	public static final ArgsMessage ERROR_DETAILS = new ArgsMessage( "skript command.reload.error details");
	public static final ArgsMessage OTHER_DETAILS = new ArgsMessage("skript command.reload.other details");

	public static final ArgsMessage ERROR_INFO = new ArgsMessage(CONFIG_NODE + ".error");
	public static final ArgsMessage WARNING_INFO = new ArgsMessage(CONFIG_NODE + ".warning");
	public static final ArgsMessage LINE_INFO = new ArgsMessage(CONFIG_NODE + ".line info");

	public static final ArgsMessage ERROR_SKIPPED = new ArgsMessage(CONFIG_NODE + ".errors skipped");
	public static final ArgsMessage ERROR_TIMEOUT = new ArgsMessage(CONFIG_NODE + ".errors timed out");
	public static final ArgsMessage ERROR_NOTIF = new ArgsMessage(CONFIG_NODE + ".error notification");
	public static final ArgsMessage ERROR_NOTIF_PLURAL = new ArgsMessage(CONFIG_NODE + ".error notification plural");

	public static final ArgsMessage WARNING_SKIPPED = new ArgsMessage(CONFIG_NODE + ".warnings skipped");
	public static final ArgsMessage WARNING_TIMEOUT = new ArgsMessage(CONFIG_NODE + ".warnings timed out");
	public static final ArgsMessage WARNING_NOTIF = new ArgsMessage(CONFIG_NODE + ".warning notification");
	public static final ArgsMessage WARNING_NOTIF_PLURAL = new ArgsMessage(CONFIG_NODE + ".warning notification plural");

	@Override
	public void printError(@NotNull RuntimeError error) {
		ArgsMessage details;
		ArgsMessage info;
		if (error.level().intValue() == Level.WARNING.intValue()) { // warnings
			details = WARNING_DETAILS;
			info = WARNING_INFO;
		} else if (error.level().intValue() == Level.SEVERE.intValue()) { // errors
			details = ERROR_DETAILS;
			info = ERROR_INFO;
		} else { // anything else
			details = OTHER_DETAILS;
			info = WARNING_INFO;
		}

		String skriptInfo = replaceNewline(Utils.replaceEnglishChatStyles(info.getValue() == null ? info.key : info.getValue()));
		String errorInfo = replaceNewline(Utils.replaceEnglishChatStyles(details.getValue() == null ? details.key : details.getValue()));
		String lineInfo = replaceNewline(Utils.replaceEnglishChatStyles(
				LINE_INFO.getValue() == null ? LINE_INFO.key : LINE_INFO.getValue()));

		ErrorSource source = error.source();
		String code = source.lineText();
		if (error.toHighlight() != null && !error.toHighlight().isEmpty())
			code = code.replace("§", "&").replace(error.toHighlight(), "§f§n" + error.toHighlight() + "§7");

		SkriptLogger.sendFormatted(Bukkit.getConsoleSender(),
				String.format(skriptInfo, source.script(), source.syntaxName(), source.syntaxType()) +
				String.format(errorInfo, error.error().replace("§", "&")) +
				String.format(lineInfo, source.lineNumber(), code));
	}

	@Override
	public void printFrameOutput(@NotNull FrameOutput output, Level level) {

		if (!output.skippedErrors().isEmpty()) {
			String linesNotDisplayed = output.skippedErrors().entrySet().stream()
				.map(entry -> "'"+ entry.getKey().script() + "' line " + entry.getKey().lineNumber() + " (" + entry.getValue() + ")")
				.collect(Collectors.joining(", "));

			int skipped = output.skippedErrors().values().stream().reduce(0, Integer::sum);

			ArgsMessage message = level == Level.SEVERE ? ERROR_SKIPPED : WARNING_SKIPPED;
			String line = message.getValue() != null ? message.getValue() : message.key;

			SkriptLogger.sendFormatted(Bukkit.getConsoleSender(),
				Utils.replaceEnglishChatStyles(
					String.format(line, skipped, linesNotDisplayed)
				)
			);
		}

		ArgsMessage message = level == Level.SEVERE ? ERROR_TIMEOUT : WARNING_TIMEOUT;
		String timeoutLine = message.getValue() != null ? message.getValue() : message.key;
		for (Location location : output.newTimeouts()) {
			SkriptLogger.sendFormatted(Bukkit.getConsoleSender(),
				Utils.replaceEnglishChatStyles(
					String.format(timeoutLine, location.lineNumber(), location.script(),
							output.frameLimits().lineTimeoutLimit(), output.frameLimits().timeoutDuration())
				)
			);
		}

		if (!output.totalErrors().isEmpty()) {
			ArgsMessage notification;
			// get various scripts from nodes
			Set<String> scripts = output.totalErrors().keySet().stream()
				.map(Location::script)
				.collect(Collectors.toSet());
			notification = level == Level.SEVERE ? ERROR_NOTIF : WARNING_NOTIF;
			if (scripts.size() > 1) {
				notification = level == Level.SEVERE ? ERROR_NOTIF_PLURAL : WARNING_NOTIF_PLURAL;
			}

			String notif = notification.getValue() != null ? notification.getValue() : notification.key;
			for (Player player : Bukkit.getOnlinePlayers()) {
				if (player.hasPermission(ERROR_NOTIF_PERMISSION)) {
					SkriptLogger.sendFormatted(player,
						Utils.replaceEnglishChatStyles(
							String.format(notif, scripts.iterator().next(), scripts.size() - 1)
						)
					);
				}
			}
		}
	}

	private static @NotNull String replaceNewline(@NotNull String s) {
		return s.replaceAll("\\\\n", "\n");
	}

}
