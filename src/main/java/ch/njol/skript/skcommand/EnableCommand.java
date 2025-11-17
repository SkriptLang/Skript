package ch.njol.skript.skcommand;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.log.RedirectingLogHandler;
import ch.njol.skript.skcommand.SkriptCommand.SubCommand;
import ch.njol.skript.util.ExceptionUtils;
import ch.njol.util.StringUtils;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Enables a script
 */
class EnableCommand extends SubCommand {

	public EnableCommand() {
		super("enable");
	}

	@Override
	public void execute(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
		try (RedirectingLogHandler redirectingLogHandler = new RedirectingLogHandler(sender, "").start()) {
			if (args[1].equalsIgnoreCase("all")) {
				enableEverything(sender, redirectingLogHandler);
			} else {
				File scriptFile = ScriptCommand.getScriptFromArgs(sender, args);
				if (scriptFile == null)
					return;

				if (scriptFile.isDirectory()) {
					enableFolder(sender, redirectingLogHandler, scriptFile);
				} else {
					enableSpecificScript(sender, redirectingLogHandler, scriptFile, args);
				}
			}
		}
	}

	@Override
	public List<String> getTabCompletions(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
		return ScriptCommand.getScriptCommandTabCompletions(args);
	}

	// sk enable all
	private static void enableEverything(CommandSender sender, RedirectingLogHandler redirectingLogHandler) {
		try {
			SkriptCommand.info(sender, "enable.all.enabling");
			ScriptLoader.loadScripts(ScriptCommand.toggleFiles(Skript.getInstance().getScriptsFolder(), true), redirectingLogHandler)
				.thenAccept(scriptInfo -> {
					if (redirectingLogHandler.numErrors() == 0) {
						SkriptCommand.info(sender, "enable.all.enabled");
					} else {
						SkriptCommand.error(sender, "enable.all.error", redirectingLogHandler.numErrors());
					}
				});
		} catch (IOException e) {
			SkriptCommand.error(sender, "enable.all.io error", ExceptionUtils.toString(e));
		}
	}

	// sk enable script_name
	private static void enableSpecificScript(CommandSender sender, RedirectingLogHandler redirectingLogHandler,
											 File scriptFile, String[] args) {
		if (ScriptLoader.getLoadedScriptsFilter().accept(scriptFile)) {
			SkriptCommand.info(sender, "enable.single.already enabled", scriptFile.getName(), StringUtils.join(args, " ", 1, args.length));
			return;
		}

		try {
			scriptFile = ScriptCommand.toggleFile(scriptFile, true);
		} catch (IOException e) {
			String scriptName = scriptFile.getName().substring(ScriptLoader.DISABLED_SCRIPT_PREFIX_LENGTH);
			SkriptCommand.error(sender, "enable.single.io error", scriptName, ExceptionUtils.toString(e));
			return;
		}

		String fileName = scriptFile.getName();
		SkriptCommand.info(sender, "enable.single.enabling", fileName);
		ScriptLoader.loadScripts(scriptFile, redirectingLogHandler)
			.thenAccept(scriptInfo -> {
				if (redirectingLogHandler.numErrors() == 0) {
					SkriptCommand.info(sender, "enable.single.enabled", fileName);
				} else {
					SkriptCommand.error(sender, "enable.single.error", fileName, redirectingLogHandler.numErrors());
				}
			});
	}

	// sk enable folder_name
	private static void enableFolder(CommandSender sender, RedirectingLogHandler redirectingLogHandler, File scriptFolder) {
		Set<File> scriptFiles;
		try {
			scriptFiles = ScriptCommand.toggleFiles(scriptFolder, true);
		} catch (IOException e) {
			SkriptCommand.error(sender, "enable.folder.io error", scriptFolder.getName(), ExceptionUtils.toString(e));
			return;
		}

		if (scriptFiles.isEmpty()) {
			SkriptCommand.info(sender, "enable.folder.empty", scriptFolder.getName());
			return;
		}

		String fileName = scriptFolder.getName();
		SkriptCommand.info(sender, "enable.folder.enabling", fileName, scriptFiles.size());
		ScriptLoader.loadScripts(scriptFiles, redirectingLogHandler)
			.thenAccept(scriptInfo -> {
				if (redirectingLogHandler.numErrors() == 0) {
					SkriptCommand.info(sender, "enable.folder.enabled", fileName, scriptInfo.files);
				} else {
					SkriptCommand.error(sender, "enable.folder.error", fileName, redirectingLogHandler.numErrors());
				}
			});
	}

}
