package ch.njol.skript.skcommand;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.skcommand.SkriptCommand.SubCommand;
import ch.njol.skript.util.ExceptionUtils;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript.lang.script.Script;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Disables a script
 */
class DisableCommand extends SubCommand {

	public DisableCommand() {
		super("disable");
	}

	@Override
	public void execute(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
		if (args[1].equalsIgnoreCase("all")) {
			disableEverything(sender);
		} else {
			File scriptFile = ScriptCommand.getScriptFromArgs(sender, args);
			if (scriptFile == null) // TODO allow disabling deleted/renamed scripts
				return;

			if (scriptFile.isDirectory()) {
				disableFolder(sender, scriptFile);
			} else {
				disableSpecificScript(sender, scriptFile);
			}

		}

	}

	@Override
	public List<String> getTabCompletions(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
		return ScriptCommand.getScriptCommandTabCompletions(args);
	}

	// sk disable all
	private static void disableEverything(CommandSender sender) {
		ScriptLoader.unloadScripts(ScriptLoader.getLoadedScripts());
		try {
			ScriptCommand.toggleFiles(Skript.getInstance().getScriptsFolder(), false);
			SkriptCommand.info(sender, "disable.all.disabled");
		} catch (IOException e) {
			SkriptCommand.error(sender, "disable.all.io error", ExceptionUtils.toString(e));
		}
	}

	// sk disable script_name
	private static void disableSpecificScript(CommandSender sender, File scriptFile) {
		if (ScriptLoader.getDisabledScriptsFilter().accept(scriptFile)) {
			String scriptName = scriptFile.getName().substring(ScriptLoader.DISABLED_SCRIPT_PREFIX_LENGTH);
			SkriptCommand.info(sender, "disable.single.already disabled", scriptName);
			return;
		}

		Script script = ScriptLoader.getScript(scriptFile);
		if (script != null) {
			ScriptLoader.unloadScript(script);
		}

		String fileName = scriptFile.getName();

		try {
			ScriptCommand.toggleFile(scriptFile, false);
		} catch (IOException e) {
			SkriptCommand.error(sender, "disable.single.io error", scriptFile.getName(), ExceptionUtils.toString(e));
			return;
		}
		SkriptCommand.info(sender, "disable.single.disabled", fileName);
	}

	// sk disable folder_name
	private static void disableFolder(CommandSender sender, File scriptFolder) {
		ScriptLoader.unloadScripts(ScriptLoader.getScripts(scriptFolder));

		Set<File> scripts;
		try {
			scripts = ScriptCommand.toggleFiles(scriptFolder, false);
		} catch (IOException e) {
			SkriptCommand.error(sender, "disable.folder.io error", scriptFolder.getName(), ExceptionUtils.toString(e));
			return;
		}

		if (scripts.isEmpty()) {
			SkriptCommand.info(sender, "disable.folder.empty", scriptFolder.getName());
			return;
		}

		SkriptCommand.info(sender, "disable.folder.disabled", scriptFolder.getName(), scripts.size());
	}

}
