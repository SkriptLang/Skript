/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript;

import ch.njol.skript.aliases.Aliases;
import ch.njol.skript.command.CommandHelp;
import ch.njol.skript.doc.Documentation;
import ch.njol.skript.doc.DocumentationIdProvider;
import ch.njol.skript.doc.HTMLGenerator;
import ch.njol.skript.doc.JSONGenerator;
import ch.njol.skript.localization.ArgsMessage;
import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.PluralizingArgsMessage;
import ch.njol.skript.log.LogEntry;
import ch.njol.skript.log.RedirectingLogHandler;
import ch.njol.skript.log.TimingLogHandler;
import ch.njol.skript.test.runner.SkriptTestEvent;
import ch.njol.skript.test.runner.TestMode;
import ch.njol.skript.test.runner.TestTracker;
import ch.njol.skript.util.ExceptionUtils;
import ch.njol.skript.util.FileUtils;
import ch.njol.skript.util.SkriptColor;
import ch.njol.util.OpenCloseable;
import ch.njol.util.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.script.Script;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;


public class SkriptCommand implements CommandExecutor {

	private static final String CONFIG_NODE = "skript command";
	private static final ArgsMessage m_reloading = new ArgsMessage(CONFIG_NODE + ".reload.reloading");

	// TODO /skript scripts show/list - lists all enabled and/or disabled scripts in the scripts folder and/or subfolders (maybe add a pattern [using * and **])
	// TODO document this command on the website
	private static final CommandHelp SKRIPT_COMMAND_HELP = new CommandHelp("<gray>/<gold>skript", SkriptColor.LIGHT_CYAN, CONFIG_NODE + ".help")
		.add(new CommandHelp("reload", SkriptColor.DARK_CYAN)
			.add("all")
			.add("config")
			.add("aliases")
			.add("scripts")
			.add("<script>")
		).add(new CommandHelp("enable", SkriptColor.DARK_CYAN)
			.add("all")
			.add("<script>")
		).add(new CommandHelp("disable", SkriptColor.DARK_CYAN)
			.add("all")
			.add("<script>")
		).add(new CommandHelp("update", SkriptColor.DARK_CYAN)
			.add("check")
			.add("changes")
			.add("download")
		).add("info"
		).add("help");

	static {
		// Add command to generate documentation
		if (TestMode.GEN_DOCS || Documentation.isDocsTemplateFound())
			SKRIPT_COMMAND_HELP.add("gen-docs");

		// Add command to run individual tests
		if (TestMode.DEV_MODE)
			SKRIPT_COMMAND_HELP.add("test");
	}

	private static void reloading(CommandSender sender, String what, RedirectingLogHandler logHandler, Object... args) {
		what = args.length == 0 ? Language.get(CONFIG_NODE + ".reload." + what) : Language.format(CONFIG_NODE + ".reload." + what, args);
		String message = StringUtils.fixCapitalization(m_reloading.toString(what));
		Skript.info(sender, message);

		// Log reloading message
		logHandler.log(new LogEntry(Level.INFO, Language.format(CONFIG_NODE + ".reload." + "player reload", sender.getName(), what)));
	}


	private static final ArgsMessage m_reloaded = new ArgsMessage(CONFIG_NODE + ".reload.reloaded");
	private static final ArgsMessage m_reload_error = new ArgsMessage(CONFIG_NODE + ".reload.error");

	private static void reloaded(CommandSender sender, RedirectingLogHandler logHandler, TimingLogHandler timingLogHandler, String what, Object... args) {
		what = args.length == 0 ? Language.get(CONFIG_NODE + ".reload." + what) : PluralizingArgsMessage.format(Language.format(CONFIG_NODE + ".reload." + what, args));
		String timeTaken = String.valueOf(timingLogHandler.getTimeTaken());

		String message;
		if (logHandler.numErrors() == 0) {
			message = StringUtils.fixCapitalization(PluralizingArgsMessage.format(m_reloaded.toString(what, timeTaken)));
			logHandler.log(new LogEntry(Level.INFO, message));
		} else {
			message = StringUtils.fixCapitalization(PluralizingArgsMessage.format(m_reload_error.toString(what, logHandler.numErrors(), timeTaken)));
			logHandler.log(new LogEntry(Level.SEVERE, message));
		}
	}

	private static void info(CommandSender sender, String what, Object... args) {
		what = args.length == 0 ? Language.get(CONFIG_NODE + "." + what) : PluralizingArgsMessage.format(Language.format(CONFIG_NODE + "." + what, args));
		Skript.info(sender, StringUtils.fixCapitalization(what));
	}

	private static void error(CommandSender sender, String what, Object... args) {
		what = args.length == 0 ? Language.get(CONFIG_NODE + "." + what) : PluralizingArgsMessage.format(Language.format(CONFIG_NODE + "." + what, args));
		Skript.error(sender, StringUtils.fixCapitalization(what));
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!SKRIPT_COMMAND_HELP.test(sender, args))
			return true;

		Set<CommandSender> recipients = new HashSet<>();
		recipients.add(sender);

		if (args[0].equalsIgnoreCase("reload")) {
			recipients.addAll(Bukkit.getOnlinePlayers().stream()
				.filter(player -> player.hasPermission("skript.reloadnotify"))
				.collect(Collectors.toSet()));
		}

		try (
			RedirectingLogHandler logHandler = new RedirectingLogHandler(recipients, "").start();
			TimingLogHandler timingLogHandler = new TimingLogHandler().start()
		) {

			if (args[0].equalsIgnoreCase("reload")) {

				if (args[1].equalsIgnoreCase("all")) {
					reloading(sender, "config, aliases and scripts", logHandler);
					SkriptConfig.load();
					Aliases.clear();
					Aliases.load();

					ScriptLoader.unloadScripts(ScriptLoader.getLoadedScripts());
					ScriptLoader.loadScripts(Skript.getInstance().getScriptsFolder(), OpenCloseable.combine(logHandler, timingLogHandler))
						.thenAccept(info -> {
							if (info.files == 0)
								Skript.warning(Skript.m_no_scripts.toString());
							reloaded(sender, logHandler, timingLogHandler, "config, aliases and scripts");
						});
				} else if (args[1].equalsIgnoreCase("scripts")) {
					reloading(sender, "scripts", logHandler);

					ScriptLoader.unloadScripts(ScriptLoader.getLoadedScripts());
					ScriptLoader.loadScripts(Skript.getInstance().getScriptsFolder(), OpenCloseable.combine(logHandler, timingLogHandler))
						.thenAccept(info -> {
							if (info.files == 0)
								Skript.warning(Skript.m_no_scripts.toString());
							reloaded(sender, logHandler, timingLogHandler, "scripts");
						});
				} else if (args[1].equalsIgnoreCase("config")) {
					reloading(sender, "main config", logHandler);
					SkriptConfig.load();
					reloaded(sender, logHandler, timingLogHandler, "main config");
				} else if (args[1].equalsIgnoreCase("aliases")) {
					reloading(sender, "aliases", logHandler);
					Aliases.clear();
					Aliases.load();
					reloaded(sender, logHandler, timingLogHandler, "aliases");
				} else { // Reloading an individual Script or folder
					File scriptFile = getScriptFromArgs(sender, args);
					if (scriptFile == null)
						return true;

					if (!scriptFile.isDirectory()) {
						if (ScriptLoader.getDisabledScriptsFilter().accept(scriptFile)) {
							info(sender, "reload.script disabled", scriptFile.getName().substring(ScriptLoader.DISABLED_SCRIPT_PREFIX_LENGTH), StringUtils.join(args, " ", 1, args.length));
							return true;
						}

						reloading(sender, "script", logHandler, scriptFile.getName());

						Script script = ScriptLoader.getScript(scriptFile);
						if (script != null)
							ScriptLoader.unloadScript(script);
						ScriptLoader.loadScripts(scriptFile, OpenCloseable.combine(logHandler, timingLogHandler))
							.thenAccept(scriptInfo ->
								reloaded(sender, logHandler, timingLogHandler, "script", scriptFile.getName())
							);
					} else {
						final String fileName = scriptFile.getName();
						reloading(sender, "scripts in folder", logHandler, fileName);
						ScriptLoader.unloadScripts(ScriptLoader.getScripts(scriptFile));
						ScriptLoader.loadScripts(scriptFile, OpenCloseable.combine(logHandler, timingLogHandler))
							.thenAccept(scriptInfo -> {
								if (scriptInfo.files == 0) {
									info(sender, "reload.empty folder", fileName);
								} else {
									reloaded(sender, logHandler, timingLogHandler, "x scripts in folder", fileName, scriptInfo.files);
								}
							});
					}
				}

			} else if (args[0].equalsIgnoreCase("enable")) {

				if (args[1].equalsIgnoreCase("all")) {
					try {
						info(sender, "enable.all.enabling");
						ScriptLoader.loadScripts(toggleFiles(Skript.getInstance().getScriptsFolder(), true), logHandler)
							.thenAccept(scriptInfo -> {
								if (logHandler.numErrors() == 0) {
									info(sender, "enable.all.enabled");
								} else {
									error(sender, "enable.all.error", logHandler.numErrors());
								}
							});
					} catch (IOException e) {
						error(sender, "enable.all.io error", ExceptionUtils.toString(e));
					}
				} else {
					File scriptFile = getScriptFromArgs(sender, args);
					if (scriptFile == null)
						return true;

					if (!scriptFile.isDirectory()) {
						if (ScriptLoader.getLoadedScriptsFilter().accept(scriptFile)) {
							info(sender, "enable.single.already enabled", scriptFile.getName(), StringUtils.join(args, " ", 1, args.length));
							return true;
						}

						try {
							scriptFile = toggleFile(scriptFile, true);
						} catch (IOException e) {
							error(sender, "enable.single.io error", scriptFile.getName().substring(ScriptLoader.DISABLED_SCRIPT_PREFIX_LENGTH), ExceptionUtils.toString(e));
							return true;
						}

						final String fileName = scriptFile.getName();
						info(sender, "enable.single.enabling", fileName);
						ScriptLoader.loadScripts(scriptFile, logHandler)
							.thenAccept(scriptInfo -> {
								if (logHandler.numErrors() == 0) {
									info(sender, "enable.single.enabled", fileName);
								} else {
									error(sender, "enable.single.error", fileName, logHandler.numErrors());
								}
							});
					} else {
						Set<File> scriptFiles;
						try {
							scriptFiles = toggleFiles(scriptFile, true);
						} catch (IOException e) {
							error(sender, "enable.folder.io error", scriptFile.getName(), ExceptionUtils.toString(e));
							return true;
						}

						if (scriptFiles.isEmpty()) {
							info(sender, "enable.folder.empty", scriptFile.getName());
							return true;
						}

						final String fileName = scriptFile.getName();
						info(sender, "enable.folder.enabling", fileName, scriptFiles.size());
						ScriptLoader.loadScripts(scriptFiles, logHandler)
							.thenAccept(scriptInfo -> {
								if (logHandler.numErrors() == 0) {
									info(sender, "enable.folder.enabled", fileName, scriptInfo.files);
								} else {
									error(sender, "enable.folder.error", fileName, logHandler.numErrors());
								}
							});
					}
				}

			} else if (args[0].equalsIgnoreCase("disable")) {

				if (args[1].equalsIgnoreCase("all")) {
					ScriptLoader.unloadScripts(ScriptLoader.getLoadedScripts());
					try {
						toggleFiles(Skript.getInstance().getScriptsFolder(), false);
						info(sender, "disable.all.disabled");
					} catch (IOException e) {
						error(sender, "disable.all.io error", ExceptionUtils.toString(e));
					}
				} else {
					File scriptFile = getScriptFromArgs(sender, args);
					if (scriptFile == null) // TODO allow disabling deleted/renamed scripts
						return true;

					if (!scriptFile.isDirectory()) {
						if (ScriptLoader.getDisabledScriptsFilter().accept(scriptFile)) {
							info(sender, "disable.single.already disabled", scriptFile.getName().substring(ScriptLoader.DISABLED_SCRIPT_PREFIX_LENGTH));
							return true;
						}

						Script script = ScriptLoader.getScript(scriptFile);
						if (script != null)
							ScriptLoader.unloadScript(script);

						String fileName = scriptFile.getName();

						try {
							toggleFile(scriptFile, false);
						} catch (IOException e) {
							error(sender, "disable.single.io error", scriptFile.getName(), ExceptionUtils.toString(e));
							return true;
						}
						info(sender, "disable.single.disabled", fileName);
					} else {
						ScriptLoader.unloadScripts(ScriptLoader.getScripts(scriptFile));

						Set<File> scripts;
						try {
							scripts = toggleFiles(scriptFile, false);
						} catch (IOException e) {
							error(sender, "disable.folder.io error", scriptFile.getName(), ExceptionUtils.toString(e));
							return true;
						}

						if (scripts.isEmpty()) {
							info(sender, "disable.folder.empty", scriptFile.getName());
							return true;
						}

						info(sender, "disable.folder.disabled", scriptFile.getName(), scripts.size());
					}
				}

			} else if (args[0].equalsIgnoreCase("update")) {
				SkriptUpdater updater = Skript.getInstance().getUpdater();
				if (updater == null) { // Oh. That is bad
					Skript.info(sender, "" + SkriptUpdater.m_internal_error);
					return true;
				}
				if (args[1].equalsIgnoreCase("check")) {
					updater.updateCheck(sender);
				} else if (args[1].equalsIgnoreCase("changes")) {
					updater.changesCheck(sender);
				} else if (args[1].equalsIgnoreCase("download")) {
					updater.updateCheck(sender);
				}
			} else if (args[0].equalsIgnoreCase("info")) {
				info(sender, "info.aliases");
				info(sender, "info.documentation");
				info(sender, "info.tutorials");
				info(sender, "info.server", Bukkit.getVersion());

				SkriptUpdater updater = Skript.getInstance().getUpdater();
				if (updater != null) {
					info(sender, "info.version", Skript.getVersion() + " (" + updater.getCurrentRelease().flavor + ")");
				} else {
					info(sender, "info.version", Skript.getVersion());
				}

				Collection<SkriptAddon> addons = Skript.getAddons();
				info(sender, "info.addons", addons.isEmpty() ? "None" : "");
				for (SkriptAddon addon : addons) {
					PluginDescriptionFile desc = addon.plugin.getDescription();
					String web = desc.getWebsite();
					Skript.info(sender, " - " + desc.getFullName() + (web != null ? " (" + web + ")" : ""));
				}

				List<String> dependencies = Skript.getInstance().getDescription().getSoftDepend();
				boolean dependenciesFound = false;
				for (String dep : dependencies) { // Check if any dependency is found in the server plugins
					Plugin plugin = Bukkit.getPluginManager().getPlugin(dep);
					if (plugin != null) {
						if (!dependenciesFound) {
							dependenciesFound = true;
							info(sender, "info.dependencies", "");
						}
						String ver = plugin.getDescription().getVersion();
						Skript.info(sender, " - " + plugin.getName() + " v" + ver);
					}
				}
				if (!dependenciesFound)
					info(sender, "info.dependencies", "None");

			} else if (args[0].equalsIgnoreCase("gen-docs")) {
				File templateDir = Documentation.getDocsTemplateDirectory();
				if (!templateDir.exists()) {
					Skript.error(sender, "Cannot generate docs! Documentation templates not found at '" + Documentation.getDocsTemplateDirectory().getPath() + "'");
					TestMode.docsFailed = true;
					return true;
				}
				File outputDir = Documentation.getDocsOutputDirectory();
				outputDir.mkdirs();
				HTMLGenerator htmlGenerator = new HTMLGenerator(templateDir, outputDir);
				JSONGenerator jsonGenerator = new JSONGenerator(templateDir, outputDir);
				Skript.info(sender, "Generating docs...");
				htmlGenerator.generate(); // Try to generate docs... hopefully
				jsonGenerator.generate();
				Skript.info(sender, "Documentation generated!");
			} else if (args[0].equalsIgnoreCase("test") && TestMode.DEV_MODE) {
				File scriptFile;
				if (args.length == 1) {
					scriptFile = TestMode.lastTestFile;
					if (scriptFile == null) {
						Skript.error(sender, "No test script has been run yet!");
						return true;
					}
				} else {
					scriptFile = TestMode.TEST_DIR.resolve(
						Arrays.stream(args).skip(1).collect(Collectors.joining(" ")) + ".sk"
					).toFile();
					TestMode.lastTestFile = scriptFile;
				}

				if (!scriptFile.exists()) {
					Skript.error(sender, "Test script doesn't exist!");
					return true;
				}

				ScriptLoader.loadScripts(scriptFile, logHandler)
					.thenAccept(scriptInfo ->
						// Code should run on server thread
						Bukkit.getScheduler().scheduleSyncDelayedTask(Skript.getInstance(), () -> {
							Bukkit.getPluginManager().callEvent(new SkriptTestEvent()); // Run it
							ScriptLoader.unloadScripts(ScriptLoader.getLoadedScripts());

							// Get results and show them
							String[] lines = TestTracker.collectResults().createReport().split("\n");
							for (String line : lines) {
								Skript.info(sender, line);
							}
						})
					);
			} else if (args[0].equalsIgnoreCase("help")) {
				SKRIPT_COMMAND_HELP.showHelp(sender);
			}

		} catch (Exception e) {
			//noinspection ThrowableNotThrown
			Skript.exception(e, "Exception occurred in Skript's main command", "Used command: /" + label + " " + StringUtils.join(args, " "));
		}

		return true;
	}

	private static final ArgsMessage m_invalid_script = new ArgsMessage(CONFIG_NODE + ".invalid script");
	private static final ArgsMessage m_invalid_folder = new ArgsMessage(CONFIG_NODE + ".invalid folder");

	@Nullable
	private static File getScriptFromArgs(CommandSender sender, String[] args) {
		String script = StringUtils.join(args, " ", 1, args.length);
		File f = getScriptFromName(script);
		if (f == null) {
			// Always allow '/' and '\' regardless of OS
			boolean directory = script.endsWith("/") || script.endsWith("\\") || script.endsWith(File.separator);
			Skript.error(sender, (directory ? m_invalid_folder : m_invalid_script).toString(script));
			return null;
		}
		return f;
	}

	@Nullable
	public static File getScriptFromName(String script) {
		if (script.endsWith("/") || script.endsWith("\\")) { // Always allow '/' and '\' regardless of OS
			script = script.replace('/', File.separatorChar).replace('\\', File.separatorChar);
		} else if (!StringUtils.endsWithIgnoreCase(script, ".sk")) {
			int dot = script.lastIndexOf('.');
			if (dot > 0 && !script.substring(dot + 1).equals(""))
				return null;
			script = script + ".sk";
		}

		if (script.startsWith(ScriptLoader.DISABLED_SCRIPT_PREFIX))
			script = script.substring(ScriptLoader.DISABLED_SCRIPT_PREFIX_LENGTH);

		File scriptFile = new File(Skript.getInstance().getScriptsFolder(), script);
		if (!scriptFile.exists()) {
			scriptFile = new File(scriptFile.getParentFile(), ScriptLoader.DISABLED_SCRIPT_PREFIX + scriptFile.getName());
			if (!scriptFile.exists()) {
				return null;
			}
		}
		try {
			return scriptFile.getCanonicalFile();
		} catch (IOException e) {
			throw Skript.exception(e, "An exception occurred while trying to get the script file from the string '" + script + "'");
		}
	}

	private static File toggleFile(File file, boolean enable) throws IOException {
		if (enable)
			return FileUtils.move(
				file,
				new File(file.getParentFile(), file.getName().substring(ScriptLoader.DISABLED_SCRIPT_PREFIX_LENGTH)),
				false
			);
		return FileUtils.move(
			file,
			new File(file.getParentFile(), ScriptLoader.DISABLED_SCRIPT_PREFIX + file.getName()),
			false
		);
	}

	private static Set<File> toggleFiles(File folder, boolean enable) throws IOException {
		FileFilter filter = enable ? ScriptLoader.getDisabledScriptsFilter() : ScriptLoader.getLoadedScriptsFilter();

		Set<File> changed = new HashSet<>();
		for (File file : folder.listFiles()) {
			if (file.isDirectory()) {
				changed.addAll(toggleFiles(file, enable));
			} else {
				if (filter.accept(file)) {
					String fileName = file.getName();
					changed.add(FileUtils.move(
						file,
						new File(file.getParentFile(), enable ? fileName.substring(ScriptLoader.DISABLED_SCRIPT_PREFIX_LENGTH) : ScriptLoader.DISABLED_SCRIPT_PREFIX + fileName),
						false
					));
				}
			}
		}

		return changed;
	}

}
