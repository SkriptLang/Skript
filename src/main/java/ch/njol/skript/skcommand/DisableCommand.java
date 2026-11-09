package ch.njol.skript.skcommand;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.util.ExceptionUtils;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;
import org.skriptlang.skript.lang.script.Script;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

/**
 * Disables a script, or a folder of scripts<p>
 * Disabling a script unloads the script and prepends the disabled prefix to the file name ('-' by default)<p>
 * Disabling a folder disables each script inside the folder, the folder name will remain the same<p>
 * Usage:
 * <pre>
 * /sk disable (script/folder name)
 * /sk disable file.sk
 * /sk disable file
 * /sk disable /folder/
 * /sk disable all
 * </pre>
 */
class DisableCommand extends SubCommand {

	DisableCommand() {
		super("disable");
	}

	@Override
	public LiteralCommandNode<CommandSourceStack> build(String literal) {
		return Commands.literal(literal)
			.executes(context -> {
				SkriptCommand.sendHelp(context.getSource().getSender(), this);
				return SINGLE_SUCCESS;
			})
			.then(Commands.literal("all")
				.executes(context -> {
					disableAll(context.getSource().getSender());
					return SINGLE_SUCCESS;
				}))
			.then(Commands.argument("script", StringArgumentType.greedyString())
				.suggests(ScriptCommandUtils.suggestions(false, false))
				.executes(context -> {
					CommandSender sender = context.getSource().getSender();
					String scriptName = StringArgumentType.getString(context, "script").trim();
					File scriptFile = ScriptCommandUtils.getScriptFileFromName(sender, scriptName);
					if (scriptFile == null) // TODO allow disabling deleted/renamed scripts
						return SINGLE_SUCCESS;

					if (scriptFile.isDirectory()) {
						disableFolder(sender, scriptFile);
					} else {
						disableSpecificScript(sender, scriptFile);
					}
					return SINGLE_SUCCESS;
				}))
			.build();
	}

	/**
	 * Handling the {@code /sk disable all} command
	 */
	private static void disableAll(CommandSender sender) {
		ScriptLoader.unloadScripts(ScriptLoader.getLoadedScripts());
		try {
			ScriptCommandUtils.toggleFiles(Skript.getInstance().getScriptsFolder(), false);
			SkriptCommand.info(sender, "disable.all.disabled");
		} catch (IOException e) {
			SkriptCommand.error(sender, "disable.all.io error", ExceptionUtils.toString(e));
		}
	}

	/**
	 * Handling the {@code /sk disable script_name} command
	 */
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
			ScriptCommandUtils.toggleFile(scriptFile, false);
		} catch (IOException e) {
			SkriptCommand.error(sender, "disable.single.io error", scriptFile.getName(), ExceptionUtils.toString(e));
			return;
		}
		SkriptCommand.info(sender, "disable.single.disabled", fileName);
	}

	/**
	 * Handling the {@code /sk disable folder_name} command
	 */
	private static void disableFolder(CommandSender sender, File scriptFolder) {
		ScriptLoader.unloadScripts(ScriptLoader.getScripts(scriptFolder));

		Set<File> scripts;
		try {
			scripts = ScriptCommandUtils.toggleFiles(scriptFolder, false);
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
