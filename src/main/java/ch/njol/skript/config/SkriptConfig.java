package ch.njol.skript.config;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.SkriptUpdater;
import ch.njol.skript.lang.function.Function;
import ch.njol.skript.localization.Language;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.log.Verbosity;
import ch.njol.skript.timings.SkriptTimings;
import ch.njol.skript.update.ReleaseChannel;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.chat.ChatMessages;
import ch.njol.skript.variables.Variables;
import co.aikar.timings.Timings;
import org.bukkit.event.EventPriority;
import org.skriptlang.skript.util.event.EventRegistry;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Represents all options in Skript's config file.
 */
public interface SkriptConfig {

	ConfigOption<String> VERSION = new ConfigOption<>("version", Skript.getVersion().toString());

	ConfigOption<String> LANGUAGE = new ConfigOption<>("language", "english") {
		@Override
		void onLoad() {
			if (!Language.load(value())) {
				Skript.error("No language file found for '%s'!".formatted(value()));
			}
		}
	};

	ConfigOption<Boolean> CHECK_FOR_NEW_VERSION = new ConfigOption<>("check for new version", false) {
		@Override
		void onLoad() {
			SkriptUpdater updater = Skript.getInstance().getUpdater();
			if (updater != null) {
				updater.setEnabled(value());
			}
		}
	};

	ConfigOption<Timespan> UPDATE_CHECK_INTERVAL = new ConfigOption<>("update check interval", new Timespan(12 * 60 * 60 * 1000)) {
		@Override
		void onLoad() {
			SkriptUpdater updater = Skript.getInstance().getUpdater();
			if (updater != null) {
				updater.setCheckFrequency(value().getAs(Timespan.TimePeriod.TICK));
			}
		}
	};

	ConfigOption<String> RELEASE_CHANNEL = new ConfigOption<>("release channel", "none") {
		@Override
		void onLoad() {
			ReleaseChannel channel;
			String value = value();
			switch (value) {
				case "alpha", "beta" -> {
					Skript.warning("'alpha' and 'beta' are no longer valid release channels. Use 'prerelease' instead.");

					channel = new ReleaseChannel((name) -> true, value());
				}
				case "prerelease" -> channel = new ReleaseChannel((name) -> true, value());
				case "stable" -> channel = new ReleaseChannel((name) -> !(name.contains("-")), value());
				case "none" -> channel = new ReleaseChannel((name) -> false, value());
				default -> {
					channel = new ReleaseChannel((name) -> false, value());
					Skript.error("Unknown release channel '%s'.".formatted(value()));
				}
			}
			SkriptUpdater updater = Skript.getInstance().getUpdater();
			if (updater != null) updater.setReleaseChannel(channel);
		}
	};

	ConfigOption<Boolean> ENABLE_EFFECT_COMMANDS = new ConfigOption<>("enable effect commands", false);
	ConfigOption<String> EFFECT_COMMAND_TOKEN = new ConfigOption<>("effect command token", "!");
	ConfigOption<Boolean> ALLOW_OPS_TO_USE_EFFECT_COMMANDS = new ConfigOption<>("allow ops to use effect commands", false);
	ConfigOption<Boolean> LOG_EFFECT_COMMANDS = new ConfigOption<>("log effect commands", false);
	ConfigOption<Boolean> USE_PLAYER_UUIDS_IN_VARIABLE_NAMES = new ConfigOption<>("use player UUIDs in variable names", false);
	ConfigOption<Boolean> ENABLE_PLAYER_VARIABLE_FIX = new ConfigOption<>("player variable fix", true);

	ConfigOption<DateFormat> DATE_FORMAT = new ConfigOption<>("date format", DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)) {
		@Override
		DateFormat parse(String input) {
			try {
				if (input.equalsIgnoreCase("default"))
					return null;
				return new SimpleDateFormat(input);
			} catch (IllegalArgumentException e) {
				Skript.error("'" + input + "' is not a valid date format. Please refer to https://docs.oracle.com/javase/8/docs/api/java/text/SimpleDateFormat.html for instructions.");
			}
			return null;
		}
	};

	ConfigOption<Verbosity> VERBOSITY = new ConfigOption<>("verbosity", Verbosity.NORMAL) {
		private static final EnumParser<Verbosity> parser = new EnumParser<>(Verbosity.class, "verbosity");

		@Override
		void onLoad() {
			SkriptLogger.setVerbosity(value());
		}

		@Override
		Verbosity parse(String input) {
			return parser.convert(input);
		}
	};

	ConfigOption<EventPriority> DEFAULT_EVENT_PRIORITY = new ConfigOption<>("plugin priority", EventPriority.NORMAL) {
		@Override
		EventPriority parse(String input) {
			try {
				return EventPriority.valueOf(input.toUpperCase(Locale.ENGLISH));
			} catch (IllegalArgumentException e) {
				Skript.error("The plugin priority must be one of lowest, low, normal, high, or highest.");
				return null;
			}
		}
	};

	ConfigOption<Boolean> LISTEN_CANCELLED_BY_DEFAULT = new ConfigOption<>("listen to cancelled events by default", false);
	ConfigOption<Integer> NUMBER_ACCURACY = new ConfigOption<>("number accuracy", 2);
	ConfigOption<Integer> MAX_TARGET_BLOCK_DISTANCE = new ConfigOption<>("maximum target block distance", 100);
	ConfigOption<Boolean> CASE_SENSITIVE = new ConfigOption<>("case sensitive", false);
	ConfigOption<Boolean> DISABLE_OBJECT_CANNOT_BE_SAVED_WARNINGS = new ConfigOption<>("disable variable will not be saved warnings", false);
	ConfigOption<Boolean> DISABLE_MISSING_AND_OR_WARNINGS = new ConfigOption<>("disable variable missing and/or warnings", false);
	ConfigOption<Boolean> DISABLE_VARIABLE_STARTING_WITH_EXPRESSION_WARNINGS = new ConfigOption<>("disable starting a variable's name with an expression warnings", false);
	ConfigOption<Boolean> DISABLE_UNREACHABLE_CODE_WARNINGS = new ConfigOption<>("disable unreachable code warnings", false);

	ConfigOption<Boolean> ENABLE_SCRIPT_CACHING = new ConfigOption<>("enable script caching", false);
	ConfigOption<Boolean> ADDON_SAFETY_CHECKS = new ConfigOption<>("addon safety checks", false);
	ConfigOption<Boolean> API_SOFT_EXCEPTIONS = new ConfigOption<>("soft api exceptions", false);

	ConfigOption<Boolean> ENABLE_TIMINGS = new ConfigOption<>("enable timings", false) {
		@Override
		void onLoad() {
			if (!Skript.classExists("co.aikar.timings.Timings")) {
				if (value()) Skript.warning("Timings cannot be enabled! Paper is required.");
				SkriptTimings.setEnabled(false);
				return;
			}
			if (Timings.class.isAnnotationPresent(Deprecated.class)) {
				if (value())
					Skript.warning("Timings cannot be enabled! Paper no longer supports Timings as of 1.19.4.");
				SkriptTimings.setEnabled(false);
				return;
			}
			if (value()) Skript.info("Timings support enabled!");
			SkriptTimings.setEnabled(value());
		}
	};

	ConfigOption<Boolean> CASE_INSENSITIVE_VARIABLES = new ConfigOption<>("case-insensitive variables", true) {
		@Override
		void onLoad() {
			Variables.caseInsensitiveVariables = value();
		}
	};

	ConfigOption<Boolean> CASE_INSENSITIVE_COMMANDS = new ConfigOption<>("case-insensitive commands", false);
	ConfigOption<Boolean> COLOR_RESET_CODES = new ConfigOption<>("color codes reset formatting", true) {
		@Override
		void onLoad() {
			ChatMessages.colorResetCodes = value();
		}
	};

	ConfigOption<String> SCRIPT_LOADER_THREAD_SIZE = new ConfigOption<>("script loader thread size", "0") {
		@Override
		void onLoad() {
			int asyncLoaderSize;
			if (value().equalsIgnoreCase("processor count")) {
				asyncLoaderSize = Runtime.getRuntime().availableProcessors();
			} else {
				try {
					asyncLoaderSize = Integer.parseInt(value());
				} catch (NumberFormatException e) {
					Skript.error("Invalid option: " + value());
					return;
				}
			}
			ScriptLoader.setAsyncLoaderSize(asyncLoaderSize);
		}
	};

	ConfigOption<Pattern> PLAYER_NAME_REGEX_PATTTERN = new ConfigOption<>("player name regex pattern", Pattern.compile("[a-zA-Z0-9_]{1,16}")) {
		@Override
		Pattern parse(String input) {
			try {
				return Pattern.compile(input);
			} catch (PatternSyntaxException e) {
				Skript.error("Invalid player name regex pattern: " + e.getMessage());
				return null;
			}
		}
	};

	ConfigOption<Boolean> ALLOW_UNSAFE_PLATFORMS = new ConfigOption<>("allow unsafe platforms", false);
	ConfigOption<Boolean> KEEP_LAST_USAGE_DATES = new ConfigOption<>("keep command last usage dates", false);
	ConfigOption<Boolean> LOAD_DEFAULT_ALIASES = new ConfigOption<>("load default aliases", true);
	ConfigOption<Boolean> EXECUTE_FUNCTIONS_WITH_MISSING_PARAMS = new ConfigOption<>("execute functions with missing parameters", true) {
		@Override
		void onLoad() {
			Function.executeWithNulls = value();
		}
	};

	ConfigOption<Timespan> LONG_PARSE_TIME_WARNING_THRESHOLD = new ConfigOption<>("long parse time warning threshold", new Timespan(0));

	ConfigOption<Timespan> RUNTIME_ERROR_FRAME_DURATION = new ConfigOption<>("runtime errors.frame duration", new Timespan(Timespan.TimePeriod.SECOND, 1));

	ConfigOption<Integer> RUNTIME_ERROR_LIMIT_TOTAL = new ConfigOption<>("runtime errors.total errors per frame", 8);
	ConfigOption<Integer> RUNTIME_WARNING_LIMIT_TOTAL = new ConfigOption<>("runtime errors.total warnings per frame", 8);

	ConfigOption<Integer> RUNTIME_ERROR_LIMIT_LINE = new ConfigOption<>("runtime errors.errors from one line per frame", 2);
	ConfigOption<Integer> RUNTIME_WARNING_LIMIT_LINE = new ConfigOption<>("runtime errors.warnings from one line per frame", 2);

	ConfigOption<Integer> RUNTIME_ERROR_LIMIT_LINE_TIMEOUT = new ConfigOption<>("runtime errors.error spam timeout limit", 4);
	ConfigOption<Integer> RUNTIME_WARNING_LIMIT_LINE_TIMEOUT = new ConfigOption<>("runtime errors.warning spam timeout limit", 4);

	ConfigOption<Integer> RUNTIME_ERROR_TIMEOUT_DURATION = new ConfigOption<>("runtime errors.error timeout length", 10);
	ConfigOption<Integer> RUNTIME_WARNING_TIMEOUT_DURATION = new ConfigOption<>("runtime errors.warning timeout length", 10);


	//<editor-fold desc="SkriptConfig events">

	/**
	 * Used for listening to events involving Skript's configuration.
	 *
	 * @see #eventRegistry()
	 */
	interface Event extends org.skriptlang.skript.util.event.Event {
	}

	/**
	 * Called when Skript's configuration is successfully reloaded.
	 * This occurs when the reload process has finished, meaning the config is safe to reference.
	 */
	@FunctionalInterface
	interface ReloadEvent extends Event {

		/**
		 * The method that is called when this event triggers.
		 */
		void onReload();

	}

	/**
	 * Stores all events related to Skript's configuration.
	 */
	class Events {
		private static final EventRegistry<Event> eventRegistry = new EventRegistry<>();
	}

	/**
	 * @return An event registry for the configuration's events.
	 */
	static EventRegistry<Event> eventRegistry() {
		return Events.eventRegistry;
	}
	//</editor-fold>

}
