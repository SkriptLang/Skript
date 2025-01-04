package ch.njol.skript;

import ch.njol.skript.aliases.Aliases;
import ch.njol.skript.command.CommandHelp;
import ch.njol.skript.doc.Documentation;
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
import ch.njol.skript.test.utils.TestResults;
import ch.njol.skript.util.ExceptionUtils;
import ch.njol.skript.util.FileUtils;
import ch.njol.skript.util.SkriptColor;
import ch.njol.skript.util.Utils;
import ch.njol.util.OpenCloseable;
import ch.njol.util.StringUtils;
import com.google.gson.GsonBuilder;
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
import java.nio.file.Files;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;


public class SkriptCommand implements CommandExecutor {

	private static final String CONFIG_NODE = "skript command";
	private static final ArgsMessage m_reloading = new ArgsMessage(CONFIG_NODE + ".reload.reloading");

	// TODO document this command on the website
	private static final CommandHelp SKRIPT_COMMAND_HELP = new CommandHelp("<gray>/<gold>skript", SkriptColor.LIGHT_CYAN, CONFIG_NODE + ".help")
			.add(new CommandHelp("reload", SkriptColor.DARK_RED)
				.add("all")
				.add("config")
				.add("aliases")
				.add("scripts")
				.add("<script>")
			).add(new CommandHelp("enable", SkriptColor.DARK_RED)
				.add("all")
				.add("<script>")
			).add(new CommandHelp("disable", SkriptColor.DARK_RED)
				.add("all")
				.add("<script>")
			).add(new CommandHelp("update", SkriptColor.DARK_RED)
				.add("check")
				.add("changes")
			)
			.add("list")
			.add("show")
			.add("info")
			.add("help");

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
		String text = Language.format(CONFIG_NODE + ".reload." + "player reload", sender.getName(), what);
		logHandler.log(new LogEntry(Level.INFO, Utils.replaceEnglishChatStyles(text)), sender);
	}

	private static final ArgsMessage m_reloaded = new ArgsMessage(CONFIG_NODE + ".reload.reloaded");
	private static final ArgsMessage m_reload_error = new ArgsMessage(CONFIG_NODE + ".reload.error");

	private static void reloaded(CommandSender sender, RedirectingLogHandler logHandler, TimingLogHandler timingLogHandler, String what, Object... args) {
		what = args.length == 0 ? Language.get(CONFIG_NODE + ".reload." + what) : PluralizingArgsMessage.format(Language.format(CONFIG_NODE + ".reload." + what, args));
		String timeTaken = String.valueOf(timingLogHandler.getTimeTaken());

		String message;
		if (logHandler.numErrors() == 0) {
			message = StringUtils.fixCapitalization(PluralizingArgsMessage.format(m_reloaded.toString(what, timeTaken)));
			logHandler.log(new LogEntry(Level.INFO, Utils.replaceEnglishChatStyles(message)));
		} else {
			message = StringUtils.fixCapitalization(PluralizingArgsMessage.format(m_reload_error.toString(what, logHandler.numErrors(), timeTaken)));
			logHandler.log(new LogEntry(Level.SEVERE, Utils.replaceEnglishChatStyles(message)));
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
					Aliases.loadAsync().thenRun(() -> {
						ScriptLoader.unloadScripts(ScriptLoader.getLoadedScripts());
						ScriptLoader.loadScripts(Skript.getInstance().getScriptsFolder(), OpenCloseable.combine(logHandler, timingLogHandler))
							.thenAccept(info -> {
								if (info.files == 0)
									Skript.warning(Skript.m_no_scripts.toString());
								reloaded(sender, logHandler, timingLogHandler, "config, aliases and scripts");
							});
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
					Aliases.loadAsync().thenRun(() -> reloaded(sender, logHandler, timingLogHandler, "aliases"));
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
									if (logHandler.numErrors() == 0) {
										reloaded(sender, logHandler, timingLogHandler, "x scripts in folder success", fileName, scriptInfo.files);
									} else {
										reloaded(sender, logHandler, timingLogHandler, "x scripts in folder error", fileName, scriptInfo.files);
									}
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
					if (args[1].equalsIgnoreCase("all")) {
						scriptFile = TestMode.TEST_DIR.toFile();
					} else {
						scriptFile = getScriptFromArgs(sender, args, TestMode.TEST_DIR.toFile());
						TestMode.lastTestFile = scriptFile;
					}
				}

				if (scriptFile == null || !scriptFile.exists()) {
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
							TestResults testResults = TestTracker.collectResults();
							String[] lines = testResults.createReport().split("\n");
							for (String line : lines) {
								Skript.info(sender, line);
							}

							// Log results to file
							Skript.info(sender, "Collecting results to " + TestMode.RESULTS_FILE);
							String results = new GsonBuilder()
								.setPrettyPrinting() // Easier to read lines
								.disableHtmlEscaping() // Fixes issue with "'" character in test strings going unicode
								.create().toJson(testResults);
							try {
								Files.writeString(TestMode.RESULTS_FILE, results);
							} catch (IOException e) {
								Skript.exception(e, "Failed to write test results.");
							}
						})
					);
			} else if (args[0].equalsIgnoreCase("list") || args[0].equalsIgnoreCase("show")) {
				info(sender, "list.enabled.header");
				ScriptLoader.getLoadedScripts().stream()
						.map(script -> script.getConfig().getFileName())
						.forEach(name -> info(sender, "list.enabled.element", name));
				info(sender, "list.disabled.header");
				ScriptLoader.getDisabledScripts().stream()
						.flatMap(file -> {
							if (file.isDirectory()) {
								return getSubFiles(file).stream();
							}
							return Arrays.stream(new File[]{file});
						})
						.map(File::getPath)
						.map(path -> path.substring(Skript.getInstance().getScriptsFolder().getPath().length() + 1))
						.forEach(path -> info(sender, "list.disabled.element", path));
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

	private static List<File> getSubFiles(File file) {
		List<File> files = new ArrayList<>();
		if (file.isDirectory()) {
			for (File listFile : file.listFiles(f -> !f.isHidden())) {
				if (listFile.isDirectory()) {
					files.addAll(getSubFiles(listFile));
				} else if (listFile.getName().endsWith(".sk")) {
					files.add(listFile);
				}
			}
		}
		return files;
	}

	private static @Nullable File getScriptFromArgs(CommandSender sender, String[] args) {
		return getScriptFromArgs(sender, args, Skript.getInstance().getScriptsFolder());
	}

	private static @Nullable File getScriptFromArgs(CommandSender sender, String[] args, File directoryFile) {
		String script = StringUtils.join(args, " ", 1, args.length);
		File f = ScriptLoader.getScriptFromName(script);
		if (f == null) {
			// Always allow '/' and '\' regardless of OS
			boolean directory = script.endsWith("/") || script.endsWith("\\") || script.endsWith(File.separator);
			Skript.error(sender, (directory ? m_invalid_folder : m_invalid_script).toString(script));
			return null;
		}
		return f;
	}

	/**
	 * Moved to {@link ScriptLoader#getScriptFromName(String)}
	 */
	@Nullable
	@Deprecated(forRemoval = true)
	public static File getScriptFromName(String script) {
		return ScriptLoader.getScriptFromName(script);
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
