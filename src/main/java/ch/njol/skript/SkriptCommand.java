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
import ch.njol.skript.log.TestingLogHandler;
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
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.Map.Entry;
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
	private static String @Nullable [] lastReloaded;

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
					File[] scriptFiles;
					if (args[1].equalsIgnoreCase("lastReloaded")) {
						if (lastReloaded == null || lastReloaded.length == 0) {
							error(sender, "reload.no previous reload");
							return true;
						}
						scriptFiles = getScriptsFromArgs(sender, lastReloaded);
						if (scriptFiles == null || scriptFiles.length == 0)
							return true;
					} else {
						scriptFiles = getScriptsFromArgs(sender, args);
						if (scriptFiles == null || scriptFiles.length == 0)
							return true;
						lastReloaded = args;
					}

					List<File> scripts = new ArrayList<>();

					for (File scriptFile : scriptFiles) {
						if (ScriptLoader.getDisabledScriptsFilter().accept(scriptFile)) {
							info(sender, "reload.script disabled", scriptFile.getName().substring(ScriptLoader.DISABLED_SCRIPT_PREFIX_LENGTH), scriptFile.getName());
							continue;
						}
						if (!scriptFile.isDirectory()) {
							Script script = ScriptLoader.getScript(scriptFile);
							if (!scripts.contains(scriptFile)) {
								if (script != null)
									ScriptLoader.unloadScript(script);
								scripts.add(scriptFile);
							}
						} else {
							ScriptLoader.unloadScripts(ScriptLoader.getScripts(scriptFile));
							scripts.add(scriptFile);
						}
					}

					for (File scriptFile : scriptFiles) {
						if (!scriptFile.isDirectory()) {
							reloading(sender, "script", logHandler, scriptFile.getName());
							ScriptLoader.loadScripts(scriptFile, OpenCloseable.combine(logHandler, timingLogHandler))
								.thenAccept(scriptInfo ->
									reloaded(sender, logHandler, timingLogHandler, "script", scriptFile.getName())
								);
						} else {
							String fileName = scriptFile.getName();
							reloading(sender, "scripts in folder", logHandler, fileName);
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
				}

			} else if (args[0].equalsIgnoreCase("enable")) {

				if (args[1].equalsIgnoreCase("all")) {
					try {
						info(sender, "enable.all.enabling");
						ScriptLoader.loadScripts(toggleFiles(Skript.getInstance().getScriptsFolder(), true, true), logHandler)
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
					File[] scriptFiles = getScriptsFromArgs(sender, args);
					if (scriptFiles == null || scriptFiles.length == 0)
						return true;

					for (File scriptFile : scriptFiles) {
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

							// Since we're enabling a directory, we want to change the name of the directory first, then enable scripts
							// If we do it after we get the files, then reloading will result in "... already exists"
							File enabledDirectory = FileUtils.move(
								scriptFile,
								new File(scriptFile.getParentFile(), scriptFile.getName().substring(ScriptLoader.DISABLED_SCRIPT_PREFIX_LENGTH)),
								false
							);

							Set<File> enabledFiles;
							try {
								enabledFiles = toggleFiles(enabledDirectory, true, false);
							} catch (IOException e) {
								error(sender, "enable.folder.io error", enabledDirectory.getName(), ExceptionUtils.toString(e));
								return true;
							}

							final String fileName = enabledDirectory.getName();

							info(sender, "enable.folder.enabling", fileName, enabledFiles.size());
							ScriptLoader.loadScripts(enabledFiles, logHandler)
								.thenAccept(scriptInfo -> {
									if (logHandler.numErrors() == 0) {
										info(sender, "enable.folder.enabled", fileName, scriptInfo.files);
									} else {
										error(sender, "enable.folder.error", fileName, logHandler.numErrors());
									}
								});
						}
					}
				}

			} else if (args[0].equalsIgnoreCase("disable")) {
				if (args[1].equalsIgnoreCase("all")) {
					ScriptLoader.unloadScripts(ScriptLoader.getLoadedScripts());
					try {
						toggleFiles(Skript.getInstance().getScriptsFolder(), false, true);
						info(sender, "disable.all.disabled");
					} catch (IOException e) {
						error(sender, "disable.all.io error", ExceptionUtils.toString(e));
					}
				} else {
					File[] scriptFiles = getScriptsFromArgs(sender, args);
					if (scriptFiles == null || scriptFiles.length == 0) // TODO allow disabling deleted/renamed scripts
						return true;

					for (File scriptFile : scriptFiles) {
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

							int totalSubFiles = getSubFiles(scriptFile).size();

							// We're disabling a directory, so we only want to change the name of the directory
							// After the files within are unloaded
							FileUtils.move(
								scriptFile,
								new File(scriptFile.getParentFile(), ScriptLoader.DISABLED_SCRIPT_PREFIX + scriptFile.getName()),
								false
							);

							info(sender, "disable.folder.disabled", scriptFile.getName(), totalSubFiles);
						}
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
				File outputDir = Documentation.getDocsOutputDirectory();
				outputDir.mkdirs();

				Skript.info(sender, "Generating docs...");
				JSONGenerator jsonGenerator = new JSONGenerator(templateDir, outputDir);
				jsonGenerator.generate();

				if (!templateDir.exists()) {
					Skript.info(sender, "JSON-only documentation generated!");
					return true;
				}

				HTMLGenerator htmlGenerator = new HTMLGenerator(templateDir, outputDir);
				htmlGenerator.generate(); // Try to generate docs... hopefully
				Skript.info(sender, "All documentation generated!");
			} else if (args[0].equalsIgnoreCase("test") && TestMode.DEV_MODE) {
				File[] scriptFiles;
				if (args.length == 1) {
					scriptFiles = TestMode.lastTestFiles;
					if (scriptFiles == null || scriptFiles.length == 0) {
						Skript.error(sender, "No test script(s) have been ran yet!");
						return true;
					}
				} else {
					if (args[1].equalsIgnoreCase("all")) {
						scriptFiles = new File[]{TestMode.TEST_DIR.toFile()};
					} else {
						scriptFiles = getScriptsFromArgs(sender, args, TestMode.TEST_DIR.toFile());
						TestMode.lastTestFiles = scriptFiles;
					}
				}

				if (scriptFiles == null || scriptFiles.length == 0)
					return true;

				// Close previous loggers before we create a new one
				// This prevents closing logger errors
				timingLogHandler.close();
				logHandler.close();

				TestingLogHandler errorCounter = new TestingLogHandler(Level.SEVERE).start();

				for (File scriptFile : scriptFiles) {
					if (!scriptFile.exists()) {
						Skript.error("The test script '" + scriptFile.getName() + "' doesn't exist!");
						continue;
					}
					ScriptLoader.loadScripts(scriptFile, errorCounter)
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
				}
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

	private static File @Nullable [] getScriptsFromArgs(CommandSender sender, String[] args) {
		return getScriptsFromArgs(sender, args, Skript.getInstance().getScriptsFolder());
	}

	private static File @Nullable [] getScriptsFromArgs(CommandSender sender, String[] args, File directoryFile) {
		List<String> filtered = new ArrayList<>();
		boolean enable = args[0].equals("enable");
		// Remove any of the options if someone manually types it after the 1st argument
		for (int i = 1; i < args.length; i++) {
			if (args[i].matches("(?i)(all|scripts|aliases|config)"))
				continue;
			filtered.add(args[i]);
		}
		Map<String, File> scripts = new HashMap<>();
		for (int i = 0; i < filtered.size(); i++) {
			String current = filtered.get(i);
			File thisScript = getScriptFromArg(sender, current, directoryFile);
			// If the script was found just from one argument then we add it
			if (thisScript != null) {
				scripts.put(current, thisScript);
				continue;
			} else {
				String original = current;
				boolean found = false;
				// Now we check the following arguments because of spaces
				// Example: current = "\examples\chest" ; additional = "menus.sk"
				for (int i2 = i + 1; i2 < filtered.size(); i2++) {
					String additional = filtered.get(i2);
					current = current + " " + additional;
					File check = getScriptFromArg(sender, current, directoryFile);
					if (check != null) {
						scripts.put(current, check);
						found = true;
						i += i2 - i;
						break;
					}
				}
				// If we couldn't find anything, we can assume this argument only was invalid
				if (!found) {
					// Always allow '/' and '\' regardless of OS
					boolean directory = original.endsWith("/") || original.endsWith("\\") || original.endsWith(File.separator);
					Skript.error(sender, (directory ? m_invalid_folder : m_invalid_script).toString(original));
				}
			}
		}
		Map<String, File> filteredScripts = new HashMap<>();
		Set<Entry<String, File>> entries = scripts.entrySet();
		// Now we check to see if any of the files found is already set to be loaded if the directory was also provided
		for (Entry<String, File> entry : entries) {
			boolean add = true;
			for (Entry<String, File> other : entries) {
				if (entry.getKey().equals(other.getKey()))
					continue;

				if (entry.getKey().contains(other.getKey()) && entry.getValue().getPath().contains(other.getValue().getPath())) {
					add = false;
					break;
				}
			}
			if (add && !filteredScripts.containsKey(entry.getKey())) {
				boolean isDisabled = scriptIsDisabled(entry.getKey());
				// If we're enabling and this script is disabled, we can add it
				// If we're disabling and this script is enabled, we can add it
				if (enable == isDisabled)
					filteredScripts.put(entry.getKey(), entry.getValue());
			}
		}
		return filteredScripts.values().toArray(File[]::new);
	}

	public static boolean scriptIsDisabled(String path) {
		String[] split = path.split("\\\\");
		for (String segment : split) {
			if (segment.startsWith("-"))
				return true;
		}
		return false;
	}

	public static boolean scriptIsDisabled(File file) {
		return scriptIsDisabled(file.getPath());
	}

	private static @Nullable File getScriptFromArg(CommandSender sender, String arg, File directoryFile) {
		return getScriptFromArgs(sender, new String[]{"", arg}, directoryFile);
	}

	private static @Nullable File getScriptFromArgs(CommandSender sender, String[] args, File directoryFile) {
		String script = StringUtils.join(args, " ", 1, args.length);
        return ScriptLoader.getScriptFromName(script, directoryFile);
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

	private static Set<File> toggleFiles(File folder, boolean enable, boolean change) throws IOException {
		return toggleFiles(folder, enable, change, scriptIsDisabled(folder));
	}

	private static Set<File> toggleFiles(File folder, boolean enable, boolean change, boolean previouslyDisabled) throws IOException {
		Set<File> files = new HashSet<>();
		for (File file : folder.listFiles()) {
			if (file.isDirectory()) {
				File subFolder = file;
				boolean isDisabled = scriptIsDisabled(file);
				if (enable == isDisabled && change)
					subFolder = toggleFile(file, enable);
				files.addAll(toggleFiles(subFolder, enable, change, isDisabled || previouslyDisabled));
			} else {
				boolean isDisabled = scriptIsDisabled(file);
				// If we're enabling and this script is disabled, we don't want it to be parsed
				if (enable == isDisabled) {
					// Unless we are enabling 'all' in which we update it
					if (change)
						files.add(toggleFile(file, enable));
					continue;
				}
				// If we're enabling specifically or if we're enabling all, and it was previously disabled
				// Then we can add it to be parsed
				if (!change || enable == previouslyDisabled)
					files.add(file);
			}
		}

		return files;
	}

}
