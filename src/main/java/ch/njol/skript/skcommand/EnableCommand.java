package ch.njol.skript.skcommand;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.log.RedirectingLogHandler;
import ch.njol.skript.util.ExceptionUtils;
import ch.njol.util.StringUtils;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

/**
 * Enables a script, or a folder of scripts<p>
 * Enabling a script will load it and remove the disabled file prefix from it<p>
 * Enabling a folder enables each script inside the folder, the folder name remains the same<p>
 * Usage:
 * <pre>
 * /sk enable file
 * /sk enable file.sk
 * /sk enable /folder/
 * /sk enable all
 * </pre>
 */
class EnableCommand extends SubCommand {

	EnableCommand() {
		super("enable");
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
					CommandSender sender = context.getSource().getSender();
					try (RedirectingLogHandler redirectingLogHandler = new RedirectingLogHandler(sender, "").start()) {
						enableAll(sender, redirectingLogHandler);
					}
					return SINGLE_SUCCESS;
				}))
			.then(Commands.argument("script", StringArgumentType.greedyString())
				.suggests(ScriptCommandUtils.suggestions(false, true))
				.executes(context -> {
					CommandSender sender = context.getSource().getSender();
					try (RedirectingLogHandler redirectingLogHandler = new RedirectingLogHandler(sender, "").start()) {
						String scriptName = StringArgumentType.getString(context, "script").trim();
						File scriptFile = ScriptCommandUtils.getScriptFileFromName(sender, scriptName);
						if (scriptFile == null)
							return SINGLE_SUCCESS;

						if (scriptFile.isDirectory()) {
							enableFolder(sender, redirectingLogHandler, scriptFile);
						} else {
							enableSpecificScript(sender, redirectingLogHandler, scriptFile, new String[]{"enable", scriptName});
						}
					}
					return SINGLE_SUCCESS;
				}))
			.build();
	}

	/**
	 * For handling the {@code /sk enable all} command
	 */
	private static void enableAll(CommandSender sender, RedirectingLogHandler redirectingLogHandler) {
		try {
			SkriptCommand.info(sender, "enable.all.enabling");
			ScriptLoader.loadScripts(ScriptCommandUtils.toggleFiles(Skript.getInstance().getScriptsFolder(), true), redirectingLogHandler)
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

	/**
	 * For handling the {@code /sk enable script.sk} command
	 */
	private static void enableSpecificScript(CommandSender sender, RedirectingLogHandler redirectingLogHandler,
											 File scriptFile, String[] args) {
		if (ScriptLoader.getLoadedScriptsFilter().accept(scriptFile)) {
			SkriptCommand.info(sender, "enable.single.already enabled", scriptFile.getName(), StringUtils.join(args, " ", 1, args.length));
			return;
		}

		try {
			scriptFile = ScriptCommandUtils.toggleFile(scriptFile, true);
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

	/**
	 * For handling the {@code /sk enable /folder/} command
	 */
	private static void enableFolder(CommandSender sender, RedirectingLogHandler redirectingLogHandler, File scriptFolder) {
		Set<File> scriptFiles;
		try {
			scriptFiles = ScriptCommandUtils.toggleFiles(scriptFolder, true);
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
