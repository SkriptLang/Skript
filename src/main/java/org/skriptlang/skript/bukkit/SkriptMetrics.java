package org.skriptlang.skript.bukkit;

import ch.njol.skript.Skript;
import org.skriptlang.skript.config.ConfigOption;
import org.skriptlang.skript.config.SkriptConfig;
import ch.njol.skript.localization.Language;
import ch.njol.skript.update.Updater;
import ch.njol.skript.util.Version;
import ch.njol.skript.util.chat.ChatMessages;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.DrilldownPie;
import org.bstats.charts.SimplePie;
import org.jetbrains.annotations.Nullable;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * helper class to handle bstats metrics
 */
public class SkriptMetrics {

	/**
	 * Helper method to set up bstats charts on the supplied Metrics object
	 *
	 * @param metrics The Metrics object to which charts will be added.
	 */
	public static void setupMetrics(Metrics metrics) {
		// Enable metrics and register custom charts

		// sets up the old charts to prevent data splitting due to various user version
		setupLegacyMetrics(metrics);

		// add custom version charts for easier reading:
		metrics.addCustomChart(new DrilldownPie("drilldownPluginVersion", () -> {
			Version version = Skript.getVersion();
			Table<String, String, Integer> table = HashBasedTable.create(1, 1);
			table.put(
				version.getMajor() + "." + version.getMinor(), // upper label
				version.toString(), // lower label
				1 // weight
			);
			return table.rowMap();
		}));

		metrics.addCustomChart(new DrilldownPie("drilldownMinecraftVersion", () -> {
			Version version = Skript.getMinecraftVersion();
			Table<String, String, Integer> table = HashBasedTable.create(1, 1);
			table.put(
				version.getMajor() + "." + version.getMinor(), // upper label
				version.toString(), // lower label
				1 // weight
			);
			return table.rowMap();
		}));

		metrics.addCustomChart(new SimplePie("buildFlavor", () -> {
			Updater updater = Skript.getInstance().getUpdater();
			if (updater != null)
				return updater.getCurrentRelease().flavor;
			return "unknown";
		}));

		//
		// config options
		//

		metrics.addCustomChart(new DrilldownPie("drilldownPluginLanguage", () -> {
			String lang = Language.getName();
			return isDefaultMap(lang, SkriptConfig.LANGUAGE.defaultValue());
		}));

		metrics.addCustomChart(new DrilldownPie("drilldownUpdateChecker", () -> {
			Table<String, String, Integer> table = HashBasedTable.create(1, 1);
			table.put(
				SkriptConfig.CHECK_FOR_NEW_VERSION.value().toString(), // upper label
				SkriptConfig.UPDATE_CHECK_INTERVAL.value().toString(), // lower label
				1 // weight
			);
			return table.rowMap();
		}));
		metrics.addCustomChart(new SimplePie("releaseChannel", SkriptConfig.RELEASE_CHANNEL::value));

		// effect commands
		metrics.addCustomChart(new DrilldownPie("drilldownEffectCommands", () -> {
			Table<String, String, Integer> table = HashBasedTable.create(1, 1);
			table.put(
				SkriptConfig.ENABLE_EFFECT_COMMANDS.value().toString(), // upper label
				SkriptConfig.EFFECT_COMMAND_TOKEN.value(), // lower label
				1 // weight
			);
			return table.rowMap();
		}));
		metrics.addCustomChart(new SimplePie("effectCommandsOps", () ->
			SkriptConfig.ALLOW_OPS_TO_USE_EFFECT_COMMANDS.value().toString()
		));
		metrics.addCustomChart(new SimplePie("logEffectCommands", () ->
			SkriptConfig.LOG_EFFECT_COMMANDS.value().toString()
		));

		metrics.addCustomChart(new SimplePie("loadDefaultAliases", () ->
			SkriptConfig.LOAD_DEFAULT_ALIASES.value().toString()
		));

		metrics.addCustomChart(new SimplePie("playerVariableFix", () ->
			SkriptConfig.ENABLE_PLAYER_VARIABLE_FIX.value().toString()
		));
		metrics.addCustomChart(new SimplePie("uuidsWithPlayers", () ->
			SkriptConfig.USE_PLAYER_UUIDS_IN_VARIABLE_NAMES.value().toString()
		));

		metrics.addCustomChart(new DrilldownPie("drilldownDateFormat", () -> {
			String value = ((SimpleDateFormat) SkriptConfig.DATE_FORMAT.value()).toPattern();
			String defaultValue = ((SimpleDateFormat) SkriptConfig.DATE_FORMAT.defaultValue()).toPattern();
			return isDefaultMap(value, defaultValue, "default");
		}));

		metrics.addCustomChart(new DrilldownPie("drilldownLogVerbosity", () -> {
			String verbosity = SkriptConfig.VERBOSITY.value().name().toLowerCase(Locale.ENGLISH).replace('_', ' ');
			String defaultValue = SkriptConfig.VERBOSITY.defaultValue().name().toLowerCase(Locale.ENGLISH).replace('_', ' ');
			return isDefaultMap(verbosity, defaultValue);
		}));

		metrics.addCustomChart(new DrilldownPie("drilldownPluginPriority", () -> {
			String priority = SkriptConfig.DEFAULT_EVENT_PRIORITY.value().name().toLowerCase(Locale.ENGLISH).replace('_', ' ');
			String defaultValue = SkriptConfig.DEFAULT_EVENT_PRIORITY.defaultValue().name().toLowerCase(Locale.ENGLISH).replace('_', ' ');
			return isDefaultMap(priority, defaultValue);
		}));
		metrics.addCustomChart(new SimplePie("cancelledByDefault", () ->
			SkriptConfig.LISTEN_CANCELLED_BY_DEFAULT.value().toString()
		));

		metrics.addCustomChart(new DrilldownPie("drilldownNumberAccuracy", () ->
			isDefaultMap(SkriptConfig.NUMBER_ACCURACY)
		));

		metrics.addCustomChart(new DrilldownPie("drilldownMaxTargetDistance", () ->
			isDefaultMap(SkriptConfig.MAX_TARGET_BLOCK_DISTANCE)
		));

		metrics.addCustomChart(new SimplePie("caseSensitiveFunctions", () ->
			SkriptConfig.CASE_SENSITIVE.value().toString()
		));
		metrics.addCustomChart(new SimplePie("caseSensitiveVariables", () ->
			String.valueOf(!SkriptConfig.CASE_INSENSITIVE_VARIABLES.value())
		));
		metrics.addCustomChart(new SimplePie("caseSensitiveCommands", () ->
			String.valueOf(!SkriptConfig.CASE_INSENSITIVE_COMMANDS.value())
		));

		metrics.addCustomChart(new SimplePie("disableSaveWarnings", () ->
			SkriptConfig.DISABLE_OBJECT_CANNOT_BE_SAVED_WARNINGS.value().toString()
		));
		metrics.addCustomChart(new SimplePie("disableAndOrWarnings", () ->
			SkriptConfig.DISABLE_MISSING_AND_OR_WARNINGS.value().toString()
		));
		metrics.addCustomChart(new SimplePie("disableStartsWithWarnings", () ->
			SkriptConfig.DISABLE_VARIABLE_STARTING_WITH_EXPRESSION_WARNINGS.value().toString()
		));

		metrics.addCustomChart(new SimplePie("softApiExceptions", () ->
			SkriptConfig.API_SOFT_EXCEPTIONS.value().toString()
		));

		metrics.addCustomChart(new SimplePie("timingsStatus", () -> {
			if (!Skript.classExists("co.aikar.timings.Timings"))
				return "unsupported";
			return SkriptConfig.ENABLE_TIMINGS.value().toString();
		}));

		metrics.addCustomChart(new SimplePie("parseLinks", () ->
			ChatMessages.linkParseMode.name().toLowerCase(Locale.ENGLISH)
		));

		metrics.addCustomChart(new SimplePie("colorResetCodes", () ->
			SkriptConfig.COLOR_RESET_CODES.value().toString()
		));

		metrics.addCustomChart(new SimplePie("keepLastUsage", () ->
			SkriptConfig.KEEP_LAST_USAGE_DATES.value().toString()
		));

		metrics.addCustomChart(new DrilldownPie("drilldownParsetimeWarningThreshold", () ->
			isDefaultMap(SkriptConfig.LONG_PARSE_TIME_WARNING_THRESHOLD, "disabled")
		));
	}

	/**
	 * Helper method to set up legacy charts (pre 2.9.2)
	 *
	 * @param metrics The Metrics object to which charts will be added.
	 */
	private static void setupLegacyMetrics(Metrics metrics) {
		// Enable metrics and register legacy charts
		metrics.addCustomChart(new SimplePie("pluginLanguage", Language::getName));
		metrics.addCustomChart(new SimplePie("updateCheckerEnabled", () ->
			SkriptConfig.CHECK_FOR_NEW_VERSION.value().toString()
		));
		metrics.addCustomChart(new SimplePie("logVerbosity", () ->
			SkriptConfig.VERBOSITY.value().name().toLowerCase(Locale.ENGLISH).replace('_', ' ')
		));
		metrics.addCustomChart(new SimplePie("pluginPriority", () ->
			SkriptConfig.DEFAULT_EVENT_PRIORITY.value().name().toLowerCase(Locale.ENGLISH).replace('_', ' ')
		));
		metrics.addCustomChart(new SimplePie("effectCommands", () ->
			SkriptConfig.ENABLE_EFFECT_COMMANDS.value().toString()
		));
		metrics.addCustomChart(new SimplePie("maxTargetDistance", () ->
			SkriptConfig.MAX_TARGET_BLOCK_DISTANCE.value().toString()
		));
	}

	/**
	 * Provides a Map for use with a {@link DrilldownPie} chart. Meant to be used in cases where a single default option has majority share, with many or custom alternative options.
	 * Creates a chart where the default option is presented against "other", then clicking on "other" shows the alternative options.
	 *
	 * @param value        The option the user chose.
	 * @param defaultValue The default option for this chart.
	 * @param <T>          The type of the option.
	 * @return A Map that can be returned directly to a {@link DrilldownPie}.
	 */
	private static <T> Map<String, Map<String, Integer>> isDefaultMap(@Nullable T value, T defaultValue) {
		return isDefaultMap(value, defaultValue, defaultValue.toString());
	}

	/**
	 * Provides a Map for use with a {@link DrilldownPie} chart. Meant to be used in cases where a single default option has majority share, with many or custom alternative options.
	 * Creates a chart where the default option is presented against "other", then clicking on "other" shows the alternative options.
	 *
	 * @param value        The option the user chose.
	 * @param defaultValue The default option for this chart.
	 * @param defaultLabel The label to use as the default option for this chart
	 * @param <T>          The type of the option.
	 * @return A Map that can be returned directly to a {@link DrilldownPie}.
	 */
	private static <T> Map<String, Map<String, Integer>> isDefaultMap(@Nullable T value, @Nullable T defaultValue, String defaultLabel) {
		Table<String, String, Integer> table = HashBasedTable.create(1, 1);
		table.put(
			Objects.equals(value, defaultValue) ? defaultLabel : "other", // upper label
			String.valueOf(value), // lower label
			1 // weight
		);
		return table.rowMap();
	}

	/**
	 * Provides a Map for use with a {@link DrilldownPie} chart. Meant to be used in cases where a single default option has majority share, with many or custom alternative options.
	 * Creates a chart where the default option is presented against "other", then clicking on "other" shows the alternative options.
	 *
	 * @param option The {@link ConfigOption} from which to pull the current and default values
	 * @param <T>    The type of the option.
	 * @return A Map that can be returned directly to a {@link DrilldownPie}.
	 */
	private static <T> Map<String, Map<String, Integer>> isDefaultMap(ConfigOption<T> option) {
		return isDefaultMap(option.value(), option.defaultValue(), option.defaultValue().toString());
	}

	/**
	 * Provides a Map for use with a {@link DrilldownPie} chart. Meant to be used in cases where a single default option has majority share, with many or custom alternative options.
	 * Creates a chart where the default option is presented against "other", then clicking on "other" shows the alternative options.
	 *
	 * @param option       The {@link ConfigOption} from which to pull the current and default values
	 * @param defaultLabel The label to use as the default option for this chart
	 * @param <T>          The type of the option.
	 * @return A Map that can be returned directly to a {@link DrilldownPie}.
	 */
	private static <T> Map<String, Map<String, Integer>> isDefaultMap(ConfigOption<T> option, String defaultLabel) {
		return isDefaultMap(option.value(), option.defaultValue(), defaultLabel);
	}

}
