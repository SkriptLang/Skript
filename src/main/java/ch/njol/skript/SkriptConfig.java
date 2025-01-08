package ch.njol.skript;

import ch.njol.skript.config.Config;
import ch.njol.skript.config.EnumParser;
import ch.njol.skript.config.Option;
import ch.njol.skript.config.OptionSection;
import ch.njol.skript.hooks.Hook;
import ch.njol.skript.hooks.VaultHook;
import ch.njol.skript.hooks.regions.GriefPreventionHook;
import ch.njol.skript.hooks.regions.PreciousStonesHook;
import ch.njol.skript.hooks.regions.ResidenceHook;
import ch.njol.skript.hooks.regions.WorldGuardHook;
import ch.njol.skript.lang.function.Function;
import ch.njol.skript.localization.Language;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.log.Verbosity;
import ch.njol.skript.timings.SkriptTimings;
import ch.njol.skript.update.ReleaseChannel;
import ch.njol.skript.util.FileUtils;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Version;
import ch.njol.skript.util.chat.ChatMessages;
import ch.njol.skript.util.chat.LinkParseMode;
import ch.njol.skript.variables.Variables;
import co.aikar.timings.Timings;
import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.util.event.EventRegistry;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Important: don't save values from the config, a '/skript reload config/configs/all' won't work correctly otherwise!
 *
 * @author Peter Güttinger
 */
@SuppressWarnings("unused")
public class SkriptConfig {

	//<editor-fold desc="SkriptConfig events">
	/**
	 * Used for listening to events involving Skript's configuration.
	 * @see #eventRegistry()
	 */
	public interface Event extends org.skriptlang.skript.util.event.Event { }

	/**
	 * Called when Skript's configuration is successfully reloaded.
	 * This occurs when the reload process has finished, meaning the config is safe to reference.
	 */
	@FunctionalInterface
	public interface ReloadEvent extends Event {

		/**
		 * The method that is called when this event triggers.
		 */
		void onReload();

	}

	private static final EventRegistry<Event> eventRegistry = new EventRegistry<>();

	/**
	 * @return An event registry for the configuration's events.
	 */
	public static EventRegistry<Event> eventRegistry() {
		return eventRegistry;
	}
	//</editor-fold>

	@Nullable
	static Config mainConfig;
	static Collection<Config> configs = new ArrayList<>();

	static final Option<String> version = new Option<>("version", Skript.getVersion().toString())
			.optional(true);

	public static final Option<String> language = new Option<>("language", "english")
			.optional(true)
			.setter(s -> {
				if (!Language.load(s)) {
					Skript.error("No language file found for '" + s + "'!");
				}
			});

	public static final Option<Boolean> checkForNewVersion = new Option<>("check for new version", false)
			.setter(t -> {
				SkriptUpdater updater = Skript.getInstance().getUpdater();
				if (updater != null)
					updater.setEnabled(t);
			});
	public static final Option<Timespan> updateCheckInterval = new Option<>("update check interval", new Timespan(12 * 60 * 60 * 1000))
			.setter(t -> {
				SkriptUpdater updater = Skript.getInstance().getUpdater();
				if (updater != null)
					updater.setCheckFrequency(t.getAs(Timespan.TimePeriod.TICK));
			});
	static final Option<Integer> updaterDownloadTries = new Option<>("updater download tries", 7)
			.optional(true);
	public static final Option<String> releaseChannel = new Option<>("release channel", "none")
			.setter(t -> {
				ReleaseChannel channel;
				switch (t) {
					case "alpha":
					case "beta":
						Skript.warning("'alpha' and 'beta' are no longer valid release channels. Use 'prerelease' instead.");
					case "prerelease": // All development builds are valid
						channel = new ReleaseChannel((name) -> true, t);
						break;
					case "stable":
						// TODO a better option would be to check that it is not a pre-release through GH API
						channel = new ReleaseChannel((name) -> !(name.contains("-")), t);
						break;
					case "none":
						channel = new ReleaseChannel((name) -> false, t);
						break;
					default:
						channel = new ReleaseChannel((name) -> false, t);
						Skript.error("Unknown release channel '" + t + "'.");
						break;
				}
				SkriptUpdater updater = Skript.getInstance().getUpdater();
				if (updater != null) {
					updater.setReleaseChannel(channel);
				}
			});

	public static final Option<Boolean> enableEffectCommands = new Option<>("enable effect commands", false);
	public static final Option<String> effectCommandToken = new Option<>("effect command token", "!");
	public static final Option<Boolean> allowOpsToUseEffectCommands = new Option<>("allow ops to use effect commands", false);

	public static final Option<Boolean> logEffectCommands = new Option<>("log effect commands", false);

	// everything handled by Variables
	public static final OptionSection databases = new OptionSection("databases");

	public static final Option<Boolean> usePlayerUUIDsInVariableNames = new Option<>("use player UUIDs in variable names", false); // TODO change to true later (as well as in the default config)
	public static final Option<Boolean> enablePlayerVariableFix = new Option<>("player variable fix", true);

	@SuppressWarnings("null")
	private static final DateFormat shortDateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
	public static final Option<DateFormat> dateFormat = new Option<>("date format", shortDateFormat, s -> {
		try {
			if (s.equalsIgnoreCase("default"))
				return null;
			return new SimpleDateFormat(s);
		} catch (final IllegalArgumentException e) {
			Skript.error("'" + s + "' is not a valid date format. Please refer to https://docs.oracle.com/javase/8/docs/api/java/text/SimpleDateFormat.html for instructions on the format.");
		}
		return null;
	});

	public static String formatDate(final long timestamp) {
		final DateFormat f = dateFormat.value();
		synchronized (f) {
			return "" + f.format(timestamp);
		}
	}

	public static final Option<Verbosity> verbosity = new Option<>("verbosity", Verbosity.NORMAL, new EnumParser<>(Verbosity.class, "verbosity"))
			.setter(SkriptLogger::setVerbosity);

	public static final Option<EventPriority> defaultEventPriority = new Option<>("plugin priority", EventPriority.NORMAL, s -> {
		try {
			return EventPriority.valueOf(s.toUpperCase(Locale.ENGLISH));
		} catch (final IllegalArgumentException e) {
			Skript.error("The plugin priority has to be one of lowest, low, normal, high, or highest.");
			return null;
		}
	});

	/**
	 * Determines whether `on &lt;event&gt;` will be triggered by cancelled events or not.
	 */
	public static final Option<Boolean> listenCancelledByDefault = new Option<>("listen to cancelled events by default", false)
			.optional(true);


	/**
	 * Maximum number of digits to display after the period for floats and doubles
	 */
	public static final Option<Integer> numberAccuracy = new Option<>("number accuracy", 2);

	public static final Option<Integer> maxTargetBlockDistance = new Option<>("maximum target block distance", 100);

	public static final Option<Boolean> caseSensitive = new Option<>("case sensitive", false);
	public static final Option<Boolean> allowFunctionsBeforeDefs = new Option<>("allow function calls before definations", false)
			.optional(true);

	public static final Option<Boolean> disableObjectCannotBeSavedWarnings = new Option<>("disable variable will not be saved warnings", false);
	public static final Option<Boolean> disableMissingAndOrWarnings = new Option<>("disable variable missing and/or warnings", false);
	public static final Option<Boolean> disableVariableStartingWithExpressionWarnings =
		new Option<>("disable starting a variable's name with an expression warnings", false);
	public static final Option<Boolean> disableUnreachableCodeWarnings = new Option<>("disable unreachable code warnings", false);

	@Deprecated
	public static final Option<Boolean> enableScriptCaching = new Option<>("enable script caching", false)
			.optional(true);

	public static final Option<Boolean> keepConfigsLoaded = new Option<>("keep configs loaded", false)
			.optional(true);

	public static final Option<Boolean> addonSafetyChecks = new Option<>("addon safety checks", false)
			.optional(true);

	public static final Option<Boolean> apiSoftExceptions = new Option<>("soft api exceptions", false);

	@SuppressWarnings("removal")
	public static final Option<Boolean> enableTimings = new Option<>("enable timings", false)
			.setter(t -> {
				if (!Skript.classExists("co.aikar.timings.Timings")) { // Check for Timings
					if (t) // Warn the server admin that timings won't work
						Skript.warning("Timings cannot be enabled! You are running Bukkit/Spigot, but Paper is required.");
					SkriptTimings.setEnabled(false); // Just to be sure, deactivate timings support completely
					return;
				}
				if (Timings.class.isAnnotationPresent(Deprecated.class)) { // check for deprecated Timings
					if (t) // Warn the server admin that timings won't work
						Skript.warning("Timings cannot be enabled! Paper no longer supports Timings as of 1.19.4.");
					SkriptTimings.setEnabled(false); // Just to be sure, deactivate timings support completely
					return;
				}
				// If we get here, we can safely enable timings
				if (t)
					Skript.info("Timings support enabled!");
				SkriptTimings.setEnabled(t); // Config option will be used
			});

	public static final Option<String> parseLinks = new Option<>("parse links in chat messages", "disabled")
			.setter(t -> {
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
			});

	public static final Option<Boolean> caseInsensitiveVariables = new Option<>("case-insensitive variables", true)
			.setter(t -> Variables.caseInsensitiveVariables = t);

	public static final Option<Boolean> caseInsensitiveCommands = new Option<>("case-insensitive commands", false)
		.optional(true);

	public static final Option<Boolean> colorResetCodes = new Option<>("color codes reset formatting", true)
			.setter(t -> {
				try {
					ChatMessages.colorResetCodes = t;
				} catch (Error e) {
					// Ignore it, we're on unsupported server platform and class loading failed
				}
			});

	public static final Option<String> scriptLoaderThreadSize = new Option<>("script loader thread size", "0")
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

	public static final Option<Boolean> allowUnsafePlatforms = new Option<>("allow unsafe platforms", false)
			.optional(true);

	public static final Option<Boolean> keepLastUsageDates = new Option<>("keep command last usage dates", false)
			.optional(true);

	public static final Option<Boolean> loadDefaultAliases = new Option<>("load default aliases", true)
			.optional(true);

	public static final Option<Boolean> executeFunctionsWithMissingParams = new Option<>("execute functions with missing parameters", true)
			.optional(true)
			.setter(t -> Function.executeWithNulls = t);

	public final static Option<Boolean> disableHookVault = new Option<>("disable hooks.vault", false)
		.optional(true)
		.setter(value -> {
			userDisableHooks(VaultHook.class, value);
		});
	public final static Option<Boolean> disableHookGriefPrevention = new Option<>("disable hooks.regions.grief prevention", false)
		.optional(true)
		.setter(value -> {
			userDisableHooks(GriefPreventionHook.class, value);
		});
	public final static Option<Boolean> disableHookPreciousStones = new Option<>("disable hooks.regions.precious stones", false)
		.optional(true)
		.setter(value -> {
			userDisableHooks(PreciousStonesHook.class, value);
		});
	public final static Option<Boolean> disableHookResidence = new Option<>("disable hooks.regions.residence", false)
		.optional(true)
		.setter(value -> {
			userDisableHooks(ResidenceHook.class, value);
		});
	public final static Option<Boolean> disableHookWorldGuard = new Option<>("disable hooks.regions.worldguard", false)
		.optional(true)
		.setter(value -> {
			userDisableHooks(WorldGuardHook.class, value);
		});
	/**
	 * Disables the specified hook depending on the option value, or gives an error if this isn't allowed at this time.
	 */
	private static void userDisableHooks(Class<? extends Hook<?>> hookClass, boolean value) {
		if (Skript.isFinishedLoadingHooks()) {
			Skript.error("Hooks cannot be disabled once the server has started. " +
				"Please restart the server to disable the hooks.");
			return;
		}
		if (value) {
			Skript.disableHookRegistration(hookClass);
		}
	}

	public final static Option<Pattern> playerNameRegexPattern = new Option<>("player name regex pattern", Pattern.compile("[a-zA-Z0-9_]{1,16}"), s -> {
		try {
			return Pattern.compile(s);
		} catch (PatternSyntaxException e) {
			Skript.error("Invalid player name regex pattern: " + e.getMessage());
			return null;
		}
	}).optional(true);

	public static final Option<Timespan> longParseTimeWarningThreshold = new Option<>("long parse time warning threshold", new Timespan(0));


	public static final Option<Timespan> runtimeErrorFrameDuration = new Option<>("runtime errors.frame duration", new Timespan(Timespan.TimePeriod.SECOND, 1));

	public static final Option<Integer> runtimeErrorLimitTotal = new Option<>("runtime errors.total errors per frame", 8);
	public static final Option<Integer> runtimeWarningLimitTotal = new Option<>("runtime errors.total warnings per frame", 8);

	public static final Option<Integer> runtimeErrorLimitLine = new Option<>("runtime errors.errors from one line per frame", 2);
	public static final Option<Integer> runtimeWarningLimitLine = new Option<>("runtime errors.warnings from one line per frame", 2);

	public static final Option<Integer> runtimeErrorLimitLineTimeout = new Option<>("runtime errors.error spam timeout limit", 4);
	public static final Option<Integer> runtimeWarningLimitLineTimeout = new Option<>("runtime errors.warning spam timeout limit", 4);

	public static final Option<Integer> runtimeErrorTimeoutDuration = new Option<>("runtime errors.error timeout length", 10);
	public static final Option<Integer> runtimeWarningTimeoutDuration = new Option<>("runtime errors.warning timeout length", 10);

	/**
	 * This should only be used in special cases
	 */
	public static @Nullable Config getConfig() {
		return mainConfig;
	}

	// also used for reloading
	static void load() {
		if (mainConfig != null)
			mainConfig.invalidate(); // todo
		try {
			File configFile = new File(Skript.getInstance().getDataFolder(), "config.sk");

			if (!configFile.exists()) {
				Skript.error("Config file 'config.sk' does not exist!");
				return;
			}
			if (!configFile.canRead()) {
				Skript.error("Config file 'config.sk' cannot be read!");
				return;
			}

			Config mainConfig;
			try {
				mainConfig = new Config(configFile, false, false, ":");
			} catch (IOException ex) {
				Skript.exception(ex, "Could not load the main config");
				return;
			}
			SkriptConfig.mainConfig = mainConfig;

			String configVersion = mainConfig.getValue(version.key);
			if (configVersion == null || Skript.getVersion().compareTo(new Version(configVersion)) != 0) {
				if (!mainConfig.getMainNode().isValid()) {
					Skript.error("Your config is outdated, but cannot be updated because it contains errors.");
					return;
				}

				try (InputStream stream = Skript.getInstance().getResource("config.sk")) {
					if (stream == null) {
						Skript.error("Your config is outdated, but Skript couldn't find the newest config in its jar.");
						return;
					}
					Config newConfig = new Config(stream, "Skript.jar/config.sk", false, false, ":");

					File backup = FileUtils.backup(configFile);
					boolean updated = mainConfig.updateNodes(newConfig);
					mainConfig.getMainNode().set(version.key, Skript.getVersion().toString());
					mainConfig.save(configFile);
					SkriptConfig.mainConfig = mainConfig;

					if (updated) {
						Skript.info("Your configuration has been updated to the latest version. " +
							"A backup of your old config file has been created as " + backup.getName());
					} else {
						Skript.info("Your configuration is outdated, but no changes were performed. " +
							"A backup of your config file has been created as " + backup.getName());
					}
				} catch (IOException ex) {
					Skript.exception(ex, "Could not update the main config");
					return;
				}
			}

			mainConfig.load(SkriptConfig.class);
		} catch (RuntimeException ex) {
			Skript.exception(ex, "An error occurred while loading the config");
		}

		// trigger reload event handlers
		eventRegistry().events(ReloadEvent.class).forEach(ReloadEvent::onReload);
	}

}
