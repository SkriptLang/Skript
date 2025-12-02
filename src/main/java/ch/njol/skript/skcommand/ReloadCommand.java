package ch.njol.skript.skcommand;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.aliases.Aliases;
import ch.njol.skript.localization.ArgsMessage;
import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.PluralizingArgsMessage;
import ch.njol.skript.log.LogEntry;
import ch.njol.skript.log.RedirectingLogHandler;
import ch.njol.skript.log.TimingLogHandler;
import ch.njol.skript.skcommand.SkriptCommand.SubCommand;
import ch.njol.skript.util.Utils;
import ch.njol.util.OpenCloseable;
import ch.njol.util.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript.lang.script.Script;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static ch.njol.skript.skcommand.SkriptCommand.info;

class ReloadCommand extends SubCommand {

	public ReloadCommand() {
		super("reload");
		args("all", "scripts", "config", "aliases", "<script>");
	}

	@Override
	public void execute(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
		Set<CommandSender> recipients = Bukkit.getOnlinePlayers().stream()
			.filter(player -> player.hasPermission("skript.reloadnotify"))
			.collect(Collectors.toSet());
		recipients.add(sender);

		try (
			RedirectingLogHandler redirectingLogHandler = new RedirectingLogHandler(recipients, "").start();
			TimingLogHandler timingLogHandler = new TimingLogHandler().start()
		) {
			switch (args[1].toLowerCase(Locale.ENGLISH)) {
				case "all" -> reloadEverything(sender, redirectingLogHandler, timingLogHandler);
				case "scripts" -> reloadScripts(sender, redirectingLogHandler, timingLogHandler);
				case "config" -> reloadConfig(sender, redirectingLogHandler, timingLogHandler);
				case "aliases" -> reloadAliases(sender, redirectingLogHandler, timingLogHandler);
				// Reloading an individual script or folder
				default -> reloadSpecific(sender, args, redirectingLogHandler, timingLogHandler);
			}
		}
	}

	@Override
	public List<String> getTabCompletions(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
		return ScriptCommand.getScriptCommandTabCompletions(args);
	}

	// sk reload all
	private static void reloadEverything(CommandSender sender, RedirectingLogHandler redirectingLogHandler,
										 TimingLogHandler timingLogHandler) {
		reloading(sender, "config, aliases and scripts", redirectingLogHandler);
		SkriptConfig.load();
		Aliases.clear();
		Aliases.loadAsync().thenRun(() -> {
			ScriptLoader.unloadScripts(ScriptLoader.getLoadedScripts());
			File scriptsFolder = Skript.getInstance().getScriptsFolder();
			ScriptLoader.loadScripts(scriptsFolder, OpenCloseable.combine(redirectingLogHandler, timingLogHandler))
				.thenAccept(info -> {
					if (info.files == 0) {
						Skript.warning(Skript.m_no_scripts.toString());
					}
					reloaded(redirectingLogHandler, timingLogHandler, "config, aliases and scripts");
				});
		});
	}

	// sk reload scripts
	private static void reloadScripts(CommandSender sender, RedirectingLogHandler redirectingLogHandler,
									  TimingLogHandler timingLogHandler) {
		reloading(sender, "scripts", redirectingLogHandler);

		ScriptLoader.unloadScripts(ScriptLoader.getLoadedScripts());
		File scriptsFolder = Skript.getInstance().getScriptsFolder();
		ScriptLoader.loadScripts(scriptsFolder, OpenCloseable.combine(redirectingLogHandler, timingLogHandler))
			.thenAccept(info -> {
				if (info.files == 0)
					Skript.warning(Skript.m_no_scripts.toString());
				reloaded(redirectingLogHandler, timingLogHandler, "scripts");
			});
	}

	// sk reload config
	private static void reloadConfig(CommandSender sender, RedirectingLogHandler redirectingLogHandler,
									 TimingLogHandler timingLogHandler) {
		reloading(sender, "main config", redirectingLogHandler);
		SkriptConfig.load();
		reloaded(redirectingLogHandler, timingLogHandler, "main config");
	}

	// sk reload aliases
	private static void reloadAliases(CommandSender sender, RedirectingLogHandler redirectingLogHandler,
									  TimingLogHandler timingLogHandler) {
		reloading(sender, "aliases", redirectingLogHandler);
		Aliases.clear();
		Aliases.loadAsync().thenRun(() -> reloaded(redirectingLogHandler, timingLogHandler, "aliases"));
	}

	// sk reload script_name
	// sk reload folder_name
	private static void reloadSpecific(CommandSender sender, String[] args, RedirectingLogHandler redirectingLogHandler,
									   TimingLogHandler timingLogHandler) {
		File scriptFile = ScriptCommand.getScriptFromArgs(sender, args);
		if (scriptFile == null)
			return;

		if (!scriptFile.isDirectory()) {
			if (ScriptLoader.getDisabledScriptsFilter().accept(scriptFile)) {
				String scriptName = scriptFile.getName().substring(ScriptLoader.DISABLED_SCRIPT_PREFIX_LENGTH);
				String command = StringUtils.join(args, " ", 1, args.length);
				info(sender, "reload.script disabled", scriptName, command);
				return;
			}

			reloading(sender, "script", redirectingLogHandler, scriptFile.getName());

			Script script = ScriptLoader.getScript(scriptFile);
			if (script != null) {
				ScriptLoader.unloadScript(script);
			}

			ScriptLoader.loadScripts(scriptFile, OpenCloseable.combine(redirectingLogHandler, timingLogHandler))
				.thenAccept(scriptInfo ->
					reloaded(redirectingLogHandler, timingLogHandler, "script", scriptFile.getName())
				);
		} else {
			String fileName = scriptFile.getName();
			reloading(sender, "scripts in folder", redirectingLogHandler, fileName);
			ScriptLoader.unloadScripts(ScriptLoader.getScripts(scriptFile));
			ScriptLoader.loadScripts(scriptFile, OpenCloseable.combine(redirectingLogHandler, timingLogHandler))
				.thenAccept(scriptInfo -> {
					if (scriptInfo.files == 0) {
						info(sender, "reload.empty folder", fileName);
						return;
					}

					String key = redirectingLogHandler.numErrors() == 0
						? "x scripts in folder success"
						: "x scripts in folder error";

					reloaded(redirectingLogHandler, timingLogHandler, key, fileName, scriptInfo.files);
				});
		}
	}

	private static final ArgsMessage RELOADING_MESSAGE = new ArgsMessage(SkriptCommand.CONFIG_NODE + ".reload.reloading");
	private static final ArgsMessage RELOADED_MESSAGE = new ArgsMessage(SkriptCommand.CONFIG_NODE + ".reload.reloaded");
	private static final ArgsMessage RELOAD_ERROR_MESSAGE = new ArgsMessage(SkriptCommand.CONFIG_NODE + ".reload.error");

	private static void reloading(CommandSender sender, String what, RedirectingLogHandler logHandler, Object... args) {
		what = args.length == 0
			? Language.get(SkriptCommand.CONFIG_NODE + ".reload." + what)
			: Language.format(SkriptCommand.CONFIG_NODE + ".reload." + what, args);

		String message = StringUtils.fixCapitalization(RELOADING_MESSAGE.toString(what));
		Skript.info(sender, message);

		// Log reloading message
		String text = Language.format(SkriptCommand.CONFIG_NODE + ".reload." + "player reload", sender.getName(), what);
		logHandler.log(new LogEntry(Level.INFO, Utils.replaceEnglishChatStyles(text)), sender);
	}

	private static void reloaded(RedirectingLogHandler logHandler, TimingLogHandler timingLogHandler,
								 String what, Object... args) {
		what = args.length == 0
			? Language.get(SkriptCommand.CONFIG_NODE + ".reload." + what)
			: PluralizingArgsMessage.format(Language.format(SkriptCommand.CONFIG_NODE + ".reload." + what, args));

		String timeTaken = String.valueOf(timingLogHandler.getTimeTaken());
		String message;

		if (logHandler.numErrors() == 0) {
			String reloadedMessage = Skript.getSkriptPrefix() + RELOADED_MESSAGE.toString(what, timeTaken);
			message = StringUtils.fixCapitalization(PluralizingArgsMessage.format(reloadedMessage));
		} else {
			String errorMessage = Skript.getSkriptPrefix() + RELOAD_ERROR_MESSAGE.toString(what, logHandler.numErrors(), timeTaken);
			message = StringUtils.fixCapitalization(PluralizingArgsMessage.format(errorMessage));
		}
		logHandler.log(new LogEntry(Level.INFO, Utils.replaceEnglishChatStyles(message)));
	}

}
