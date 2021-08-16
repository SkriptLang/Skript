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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

import ch.njol.skript.hooks.VaultHook;
import ch.njol.skript.hooks.regions.GriefPreventionHook;
import ch.njol.skript.hooks.regions.PreciousStonesHook;
import ch.njol.skript.hooks.regions.ResidenceHook;
import ch.njol.skript.hooks.regions.WorldGuardHook;
import org.bukkit.event.EventPriority;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.classes.Converter;
import ch.njol.skript.config.Config;
import ch.njol.skript.config.EnumParser;
import ch.njol.skript.config.Option;
import ch.njol.skript.config.OptionSection;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.VariableString;
import ch.njol.skript.lang.function.Function;
import ch.njol.skript.localization.Language;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.log.Verbosity;
import ch.njol.skript.timings.SkriptTimings;
import ch.njol.skript.update.ReleaseChannel;
import ch.njol.skript.util.FileUtils;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.chat.ChatMessages;
import ch.njol.skript.util.chat.LinkParseMode;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Setter;

/**
 * Important: don't save values from the config, a '/skript reload config/configs/all' won't work correctly otherwise!
 * 
 * @author Peter Güttinger
 */
@SuppressWarnings("unused")
public abstract class SkriptConfig {
	private SkriptConfig() {}
	
	@Nullable
	static Config mainConfig;
	static Collection<Config> configs = new ArrayList<Config>();
	
	final static Option<String> version = new Option<String>("version", Skript.getVersion().toString())
			.optional(true);
	
	public final static Option<String> language = new Option<String>("language", "english")
			.optional(true)
			.setter(new Setter<String>() {
				@Override
				public void set(final String s) {
					if (!Language.load(s)) {
						Skript.error("No language file found for '" + s + "'!");
					}
				}
			});
	
	final static Option<Boolean> checkForNewVersion = new Option<Boolean>("check for new version", false)
			.setter(new Setter<Boolean>() {

				@Override
				public void set(Boolean t) {
					SkriptUpdater updater = Skript.getInstance().getUpdater();
					if (updater != null)
						updater.setEnabled(t);
				}
			});
	final static Option<Timespan> updateCheckInterval = new Option<Timespan>("update check interval", new Timespan(12 * 60 * 60 * 1000))
			.setter(new Setter<Timespan>() {
				@Override
				public void set(final Timespan t) {
					SkriptUpdater updater = Skript.getInstance().getUpdater();
					if (updater != null)
						updater.setCheckFrequency(t.getTicks_i());
				}
			});
	final static Option<Integer> updaterDownloadTries = new Option<Integer>("updater download tries", 7)
			.optional(true);
	final static Option<String> releaseChannel = new Option<String>("release channel", "none")
			.setter(new Setter<String>() {

				@Override
				public void set(String t) {
					ReleaseChannel channel;
					if (t.equals("alpha")) { // Everything goes in alpha channel
						channel = new ReleaseChannel((name) -> true, t);
					} else if (t.equals("beta")) {
						channel = new ReleaseChannel((name) -> !name.contains("alpha"), t);
					} else if (t.equals("stable")) {
						channel = new ReleaseChannel((name) -> !name.contains("alpha") && !name.contains("beta"), t);
					} else if (t.equals("none")) {
						channel = new ReleaseChannel((name) -> false, t);
					} else {
						channel = new ReleaseChannel((name) -> false, t);
						Skript.error("Unknown release channel '" + t + "'.");
					}
					SkriptUpdater updater = Skript.getInstance().getUpdater();
					if (updater != null) {
						if (updater.getCurrentRelease().flavor.contains("spigot") && !t.equals("stable")) {
							Skript.error("Only stable Skript versions are uploaded to Spigot resources.");
						}
						updater.setReleaseChannel(channel);
					}
				}
			});
	
	// Legacy updater options. They have no effect
	@Deprecated
	final static Option<Boolean> automaticallyDownloadNewVersion = new Option<Boolean>("automatically download new version", false)
			.optional(true);
	@Deprecated
	final static Option<Boolean> updateToPrereleases = new Option<Boolean>("update to pre-releases", true)
			.optional(true);
	
	public final static Option<Boolean> enableEffectCommands = new Option<Boolean>("enable effect commands", false);
	public final static Option<String> effectCommandToken = new Option<String>("effect command token", "!");
	public final static Option<Boolean> allowOpsToUseEffectCommands = new Option<Boolean>("allow ops to use effect commands", false);
	
	// everything handled by Variables
	public final static OptionSection databases = new OptionSection("databases");
	
	public final static Option<Boolean> usePlayerUUIDsInVariableNames = new Option<Boolean>("use player UUIDs in variable names", false); // TODO change to true later (as well as in the default config)
	public final static Option<Boolean> enablePlayerVariableFix = new Option<Boolean>("player variable fix", true);
	
	@SuppressWarnings("null")
	private final static DateFormat shortDateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
	private final static Option<DateFormat> dateFormat = new Option<DateFormat>("date format", shortDateFormat, new Converter<String, DateFormat>() {
		@Override
		@Nullable
		public DateFormat convert(final String s) {
			try {
				if (s.equalsIgnoreCase("default"))
					return null;
				return new SimpleDateFormat(s);
			} catch (final IllegalArgumentException e) {
				Skript.error("'" + s + "' is not a valid date format. Please refer to https://docs.oracle.com/javase/8/docs/api/java/text/SimpleDateFormat.html for instructions on the format.");
			}
			return null;
		}
	});
	
	public static String formatDate(final long timestamp) {
		final DateFormat f = dateFormat.value();
		synchronized (f) {
			return "" + f.format(timestamp);
		}
	}
	
	final static Option<Verbosity> verbosity = new Option<>("verbosity", Verbosity.NORMAL, new EnumParser<>(Verbosity.class, "verbosity"))
			.setter(SkriptLogger::setVerbosity);
	
	public final static Option<EventPriority> defaultEventPriority = new Option<EventPriority>("plugin priority", EventPriority.NORMAL, new Converter<String, EventPriority>() {
		@Override
		@Nullable
		public EventPriority convert(final String s) {
			try {
				return EventPriority.valueOf(s.toUpperCase(Locale.ENGLISH));
			} catch (final IllegalArgumentException e) {
				Skript.error("The plugin priority has to be one of lowest, low, normal, high, or highest.");
				return null;
			}
		}
	});

	public final static Option<Boolean> logPlayerCommands = new Option<Boolean>("log player commands", false);
	
	/**
	 * Maximum number of digits to display after the period for floats and doubles
	 */
	public final static Option<Integer> numberAccuracy = new Option<Integer>("number accuracy", 2);
	
	public final static Option<Integer> maxTargetBlockDistance = new Option<Integer>("maximum target block distance", 100);
	
	public final static Option<Boolean> caseSensitive = new Option<Boolean>("case sensitive", false);
	public final static Option<Boolean> allowFunctionsBeforeDefs = new Option<Boolean>("allow function calls before definations", false)
			.optional(true);
	
	public final static Option<Boolean> disableVariableConflictWarnings = new Option<Boolean>("disable variable conflict warnings", false);
	public final static Option<Boolean> disableObjectCannotBeSavedWarnings = new Option<Boolean>("disable variable will not be saved warnings", false);
	public final static Option<Boolean> disableMissingAndOrWarnings = new Option<Boolean>("disable variable missing and/or warnings", false);
	public final static Option<Boolean> disableVariableStartingWithExpressionWarnings = new Option<Boolean>("disable starting a variable's name with an expression warnings", false)
			.setter(new Setter<Boolean>() {

				@Override
				public void set(Boolean t) {
					VariableString.disableVariableStartingWithExpressionWarnings = t;
				}
			});
	
	@Deprecated
	public final static Option<Boolean> enableScriptCaching = new Option<Boolean>("enable script caching", false)
			.optional(true);
	
	public final static Option<Boolean> keepConfigsLoaded = new Option<Boolean>("keep configs loaded", false)
			.optional(true);
	
	public final static Option<Boolean> addonSafetyChecks = new Option<Boolean>("addon safety checks", false)
			.optional(true);
	
	public final static Option<Boolean> apiSoftExceptions = new Option<Boolean>("soft api exceptions", false);
	
	public final static Option<Boolean> enableTimings = new Option<Boolean>("enable timings", false)
			.setter(new Setter<Boolean>() {

				@Override
				public void set(Boolean t) {
					if (Skript.classExists("co.aikar.timings.Timings")) { // Check for Paper server
						if (t)
							Skript.info("Timings support enabled!");
						SkriptTimings.setEnabled(t); // Config option will be used
					} else { // Not running Paper
						if (t) // Warn the server admin that timings won't work
							Skript.warning("Timings cannot be enabled! You are running Bukkit/Spigot, but Paper is required.");
						SkriptTimings.setEnabled(false); // Just to be sure, deactivate timings support completely
					}
				}
				
			});
	
	public final static Option<String> parseLinks = new Option<String>("parse links in chat messages", "disabled")
			.setter(new Setter<String>() {

				@Override
				public void set(String t) {
					try {
						switch (t) {
							case "false":
							case "disabled":
								ChatMessages.linkParseMode = LinkParseMode.DISABLED;
								break;
							case "true":
							case "lenient":
								ChatMessages.linkParseMode = LinkParseMode.LENIENT;
								break;
							case "strict":
								ChatMessages.linkParseMode = LinkParseMode.STRICT;
								break;
							default:
								ChatMessages.linkParseMode = LinkParseMode.DISABLED;
								Skript.warning("Unknown link parse mode: " + t + ", please use disabled, strict or lenient");
						}
					} catch (Error e) {
						// Ignore it, we're on unsupported server platform and class loading failed
					}
				}
				
			});

	public final static Option<Boolean> caseInsensitiveVariables = new Option<Boolean>("case-insensitive variables", true)
			.setter(new Setter<Boolean>() {

				@Override
				public void set(Boolean t) {
					Variables.caseInsensitiveVariables = t;
				}
				
			})
			.optional(true);
	
	public final static Option<Boolean> colorResetCodes = new Option<Boolean>("color codes reset formatting", true)
			.setter(new Setter<Boolean>() {

				@Override
				public void set(Boolean t) {
					try {
						ChatMessages.colorResetCodes = t;
					} catch (Error e) {
						// Ignore it, we're on unsupported server platform and class loading failed
					}
				}
				
			});

	public final static Option<String> scriptLoaderThreadSize = new Option<>("script loader thread size", "0")
			.setter(s -> {
				int asyncLoaderSize;
				
				if (s.equalsIgnoreCase("processor count")) {
					asyncLoaderSize = Runtime.getRuntime().availableProcessors();
				} else {
					try {
						asyncLoaderSize = Integer.parseInt(s);
					} catch (NumberFormatException e) {
						Skript.error("Invalid option: " + s);
						return;
					}
				}
				
				ScriptLoader.setAsyncLoaderSize(asyncLoaderSize);
			})
			.optional(true);
	
	public final static Option<Boolean> allowUnsafePlatforms = new Option<Boolean>("allow unsafe platforms", false)
			.optional(true);

	public final static Option<Boolean> keepLastUsageDates = new Option<Boolean>("keep command last usage dates", false)
			.optional(true);
	
	public final static Option<Boolean> loadDefaultAliases = new Option<Boolean>("load default aliases", true)
			.optional(true);

	public final static Option<Boolean> executeFunctionsWithMissingParams = new Option<Boolean>("execute functions with missing parameters", true)
			.optional(true)
			.setter(new Setter<Boolean>() {

				@Override
				public void set(Boolean t) {
					Function.executeWithNulls = t;
				}
				
			});

	public final static Option<Boolean> disableHookVault = new Option<>("disable hooks.vault", false)
		.optional(true)
		.setter(value -> {
			if (value) {
				Skript.disableHookRegistration(VaultHook.class);
			}
		});
	public final static Option<Boolean> disableHookGriefPrevention = new Option<>("disable hooks.regions.grief prevention", false)
		.optional(true)
		.setter(value -> {
			if (value) {
				Skript.disableHookRegistration(GriefPreventionHook.class);
			}
		});
	public final static Option<Boolean> disableHookPreciousStones = new Option<>("disable hooks.regions.precious stones", false)
		.optional(true)
		.setter(value -> {
			if (value) {
				Skript.disableHookRegistration(PreciousStonesHook.class);
			}
		});
	public final static Option<Boolean> disableHookResidence = new Option<>("disable hooks.regions.residence", false)
		.optional(true)
		.setter(value -> {
			if (value) {
				Skript.disableHookRegistration(ResidenceHook.class);
			}
		});
	public final static Option<Boolean> disableHookWorldGuard = new Option<>("disable hooks.regions.worldguard", false)
		.optional(true)
		.setter(value -> {
			if (value) {
				Skript.disableHookRegistration(WorldGuardHook.class);
			}
		});

	/**
	 * This should only be used in special cases
	 */
	@Nullable
	public static Config getConfig() {
		return mainConfig;
	}
	
	// also used for reloading
	static boolean load() {
		try {
			final File oldConfigFile = new File(Skript.getInstance().getDataFolder(), "config.cfg");
			final File configFile = new File(Skript.getInstance().getDataFolder(), "config.sk");
			if (oldConfigFile.exists()) {
				if (!configFile.exists()) {
					oldConfigFile.renameTo(configFile);
					Skript.info("[1.3] Renamed your 'config.cfg' to 'config.sk' to match the new format");
				} else {
					Skript.error("Found both a new and an old config, ignoring the old one");
				}
			}
			if (!configFile.exists()) {
				Skript.error("Config file 'config.sk' does not exist!");
				return false;
			}
			if (!configFile.canRead()) {
				Skript.error("Config file 'config.sk' cannot be read!");
				return false;
			}
			
			Config mc;
			try {
				mc = new Config(configFile, false, false, ":");
			} catch (final IOException e) {
				Skript.error("Could not load the main config: " + e.getLocalizedMessage());
				return false;
			}
			mainConfig = mc;
			
			if (!Skript.getVersion().toString().equals(mc.get(version.key))) {
				try {
					final InputStream in = Skript.getInstance().getResource("config.sk");
					if (in == null) {
						Skript.error("Your config is outdated, but Skript couldn't find the newest config in its jar.");
						return false;
					}
					final Config newConfig = new Config(in, "Skript.jar/config.sk", false, false, ":");
					in.close();
					
					boolean forceUpdate = false;
					
					if (mc.getMainNode().get("database") != null) { // old database layout
						forceUpdate = true;
						try {
							final SectionNode oldDB = (SectionNode) mc.getMainNode().get("database");
							assert oldDB != null;
							final SectionNode newDBs = (SectionNode) newConfig.getMainNode().get(databases.key);
							assert newDBs != null;
							final SectionNode newDB = (SectionNode) newDBs.get("database 1");
							assert newDB != null;
							
							newDB.setValues(oldDB);
							
							// '.db' was dynamically added before
							final String file = newDB.getValue("file");
							assert file != null;
							if (!file.endsWith(".db"))
								newDB.set("file", file + ".db");
							
							final SectionNode def = (SectionNode) newDBs.get("default");
							assert def != null;
							def.set("backup interval", "" + mc.get("variables backup interval"));
						} catch (final Exception e) {
							Skript.error("An error occurred while trying to update the config's database section.");
							Skript.error("You'll have to update the config yourself:");
							Skript.error("Open the new config.sk as well as the created backup, and move the 'database' section from the backup to the start of the 'databases' section");
							Skript.error("of the new config (i.e. the line 'databases:' should be directly above 'database:'), and add a tab in front of every line that you just copied.");
							return false;
						}
					}
					
					if (newConfig.setValues(mc, version.key, databases.key) || forceUpdate) { // new config is different
						final File bu = FileUtils.backup(configFile);
						newConfig.getMainNode().set(version.key, Skript.getVersion().toString());
						if (mc.getMainNode().get(databases.key) != null)
							newConfig.getMainNode().set(databases.key, mc.getMainNode().get(databases.key));
						mc = mainConfig = newConfig;
						mc.save(configFile);
						Skript.info("Your configuration has been updated to the latest version. A backup of your old config file has been created as " + bu.getName());
					} else { // only the version changed
						mc.getMainNode().set(version.key, Skript.getVersion().toString());
						mc.save(configFile);
					}
				} catch (final IOException e) {
					Skript.error("Could not load the new config from the jar file: " + e.getLocalizedMessage());
				}
			}
			
			mc.load(SkriptConfig.class);
			
//			if (!keepConfigsLoaded.value())
//				mainConfig = null;
		} catch (final RuntimeException e) {
			Skript.exception(e, "An error occurred while loading the config");
			return false;
		}
		return true;
	}
	
}
