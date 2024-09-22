package org.skriptlang.skript.bukkit.spark;

import me.lucko.spark.api.statistic.misc.DoubleAverageInfo;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Arrays;
import java.util.Locale;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An instance of a statistic to use with the {@link org.skriptlang.skript.bukkit.spark.expressions.ExprMSPT} expression.
 * Can be an average, median, min, max, or specific percentile.
 */
public class MsptStatistic {

	/**
	 * The type of a MSPT statistic.
	 */
	private enum MsptStatisticType {
		AVERAGE("(?:average|mean)", DoubleAverageInfo::mean),
		MEDIAN("median", DoubleAverageInfo::median),
		MAX("max(?:imum)?", DoubleAverageInfo::max),
		MIN("min(?:imum)?", DoubleAverageInfo::min),
		PERCENTILE("(\\d{1,2}(?:\\.\\d+)?)(?:st|nd|rd|th)\\s+percentile", DoubleAverageInfo::percentile);

		private final Pattern pattern;
		private @UnknownNullability Function<DoubleAverageInfo, Double> function;
		private @UnknownNullability BiFunction<DoubleAverageInfo, Double, Double> percentileFunction;

		MsptStatisticType(String pattern, Function<DoubleAverageInfo, Double> function) {
			this.pattern = Pattern.compile(pattern);
			this.function = function;
		}

		MsptStatisticType(String pattern, BiFunction<DoubleAverageInfo, Double, Double> percentileFunction) {
			this.pattern = Pattern.compile(pattern);
			this.percentileFunction = percentileFunction;
		}

		/**
		 * @return the function to apply to an info to get this statistic. For {@link MsptStatisticType#PERCENTILE},
		 * 			use {@link MsptStatisticType#getPercentileFunction()}, since this method will return null.
		 */
		private Function<DoubleAverageInfo, Double> getFunction() {
			return function;
		}

		/**
		 * @return the function to apply to an info to get the percentile statistic.
		 * 			Only use for {@link MsptStatisticType#PERCENTILE}.
		 */
		private BiFunction<DoubleAverageInfo, Double, Double> getPercentileFunction() {
			return percentileFunction;
		}
	}

	private final MsptStatisticType type;
	private double value;

	/**
	 * Attempts to parse a string into a MSPT statistic.
	 * @param input the string to parse
	 * @return a new MsptStatistic, or null if parsing failed.
	 */
	public static @Nullable MsptStatistic parse(String input) {
		for (MsptStatisticType type : MsptStatisticType.values()) {
			Matcher matcher = type.pattern.matcher(input);
			if (matcher.matches()) {
				if (type != MsptStatisticType.PERCENTILE)
					return new MsptStatistic(type);
				return new MsptStatistic(type, Double.parseDouble(matcher.group(1)) / 100);
			}
		}
		return null;
	}

	private MsptStatistic(MsptStatisticType type) {
		if (type == MsptStatisticType.PERCENTILE)
			throw new IllegalArgumentException("Must supply a value for the percentile type!");
		this.type = type;
	}

	private MsptStatistic(MsptStatisticType type, double value) {
		this.type = type;
		this.value = value;
	}

	/**
	 * Gets this statistic from a {@link DoubleAverageInfo}.
	 * @param info the info from which to get this statistic.
	 * @return this statistic's value from the info.
	 */
	public double get(DoubleAverageInfo info) {
		if (this.type == MsptStatisticType.PERCENTILE)
			return type.getPercentileFunction().apply(info, value);
		return type.getFunction().apply(info);
	}

	/**
	 * Gets this statistic from multiple {@link DoubleAverageInfo}.
	 * @param infos the infos from which to get this statistic.
	 * @return this statistic's values from the infos.
	 */
	public Double[] get(DoubleAverageInfo...infos) {
		if (this.type == MsptStatisticType.PERCENTILE)
			return Arrays.stream(infos).map(info -> type.getPercentileFunction().apply(info, value)).toArray(Double[]::new);
		return Arrays.stream(infos).map(type.getFunction()).toArray(Double[]::new);
	}

	@Override
	public String toString() {
		if (type == MsptStatisticType.PERCENTILE)
			return value + "th percentile";
		return type.name().toLowerCase(Locale.ENGLISH);
	}

}
