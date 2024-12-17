package ch.njol.skript.util;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.localization.GeneralWords;
import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.Noun;
import ch.njol.util.NonNullPair;
import ch.njol.util.coll.CollectionUtils;
import ch.njol.yggdrasil.YggdrasilSerializable;
import com.google.common.base.Preconditions;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.temporal.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.time.temporal.ChronoUnit.MILLIS;

/**
 * Represents a duration of time, such as 2 days, similar to {@link Duration}.
 */
public class Timespan implements YggdrasilSerializable, Comparable<Timespan>, TemporalAmount { // REMIND unit

	private static final Pattern TIMESPAN_PATTERN = Pattern.compile("^(\\d+):(\\d\\d)(:\\d\\d){0,2}(?<ms>\\.\\d{1,4})?$");
	private static final Pattern TIMESPAN_NUMBER_PATTERN = Pattern.compile("^\\d+(\\.\\d+)?$");
	private static final Pattern TIMESPAN_SPLIT_PATTERN = Pattern.compile("[:.]");
	private static final Pattern SHORT_FORM_PATTERN = Pattern.compile("^(\\d+(?:\\.\\d+)?)([a-zA-Z]+)$");

	private static final List<NonNullPair<Noun, Long>> SIMPLE_VALUES = Arrays.asList(
		new NonNullPair<>(TimePeriod.YEAR.name, TimePeriod.YEAR.time),
		new NonNullPair<>(TimePeriod.MONTH.name, TimePeriod.MONTH.time),
		new NonNullPair<>(TimePeriod.WEEK.name, TimePeriod.WEEK.time),
		new NonNullPair<>(TimePeriod.DAY.name, TimePeriod.DAY.time),
		new NonNullPair<>(TimePeriod.HOUR.name, TimePeriod.HOUR.time),
		new NonNullPair<>(TimePeriod.MINUTE.name, TimePeriod.MINUTE.time),
		new NonNullPair<>(TimePeriod.SECOND.name, TimePeriod.SECOND.time)
	);

	private static final Map<String, Long> PARSE_VALUES = new HashMap<>();

	static {
		Language.addListener(() -> {
			for (TimePeriod time : TimePeriod.values()) {
				PARSE_VALUES.put(time.name.getSingular().toLowerCase(Locale.ENGLISH), time.getTime());
				PARSE_VALUES.put(time.name.getPlural().toLowerCase(Locale.ENGLISH), time.getTime());
				PARSE_VALUES.put(time.shortName.getSingular().toLowerCase(Locale.ENGLISH), time.getTime());
				PARSE_VALUES.put(time.shortName.getPlural().toLowerCase(Locale.ENGLISH), time.getTime());
			}
		});
	}

	public static @Nullable Timespan parse(String value) {
		return parse(value, ParseContext.DEFAULT);
	}

	public static @Nullable Timespan parse(String value, ParseContext context) {
		if (value.isEmpty())
			return null;

		long totalMillis = 0;
		boolean minecraftTime = false;
		boolean isMinecraftTimeSet = false;

		Matcher matcher = TIMESPAN_PATTERN.matcher(value);
		if (matcher.matches()) { // MM:SS[.ms] or HH:MM:SS[.ms] or DD:HH:MM:SS[.ms]
			String[] substring = TIMESPAN_SPLIT_PATTERN.split(value);
			long[] times = {1L, TimePeriod.SECOND.time, TimePeriod.MINUTE.time, TimePeriod.HOUR.time, TimePeriod.DAY.time}; // ms, s, m, h, d
			boolean hasMs = value.contains(".");
			int length = substring.length;
			int offset = 2; // MM:SS[.ms]

			if (length == 4 && !hasMs || length == 5) // DD:HH:MM:SS[.ms]
				offset = 0;
			else if (length == 3 && !hasMs || length == 4) // HH:MM:SS[.ms]
				offset = 1;

			for (int i = 0; i < substring.length; i++) {
				totalMillis += times[offset + i] * Utils.parseLong(substring[i]);
			}
		} else { // <number> minutes/seconds/.. etc
			String[] substring = value.toLowerCase(Locale.ENGLISH).split("\\s+");
			for (int i = 0; i < substring.length; i++) {
				String sub = substring[i];

				if (sub.equals(GeneralWords.and.toString())) {
					if (i == 0 || i == substring.length - 1)
						return null;
					continue;
				}

				double amount = 1;
				if (Noun.isIndefiniteArticle(sub)) {
					if (i == substring.length - 1)
						return null;
					sub = substring[++i];
				} else if (TIMESPAN_NUMBER_PATTERN.matcher(sub).matches()) {
					if (i == substring.length - 1)
						return null;
					try {
						amount = Double.parseDouble(sub);
					} catch (NumberFormatException e) {
						throw new IllegalArgumentException("Invalid timespan: " + value);
					}
					sub = substring[++i];
				}

				if (CollectionUtils.contains(Language.getList("time.real"), sub)) {
					if (i == substring.length - 1 || isMinecraftTimeSet && minecraftTime)
						return null;
					sub = substring[++i];
				} else if (CollectionUtils.contains(Language.getList("time.minecraft"), sub)) {
					if (i == substring.length - 1 || isMinecraftTimeSet && !minecraftTime)
						return null;
					minecraftTime = true;
					sub = substring[++i];
				}

				if (sub.endsWith(","))
					sub = sub.substring(0, sub.length() - 1);

				if (context == ParseContext.COMMAND) {
					Matcher shortFormMatcher = SHORT_FORM_PATTERN.matcher(sub);
					if (shortFormMatcher.matches()) {
						amount = Double.parseDouble(shortFormMatcher.group(1));
						sub = shortFormMatcher.group(2).toLowerCase(Locale.ENGLISH);
					}
				}

				Long millis = PARSE_VALUES.get(sub.toLowerCase(Locale.ENGLISH));
				if (millis == null)
					return null;

				if (minecraftTime && millis != TimePeriod.TICK.time)
					amount /= 72f;

				totalMillis += Math.round(amount * millis);

				isMinecraftTimeSet = true;
			}
		}
		return new Timespan(totalMillis);
	}

	public static Timespan fromDuration(Duration duration) {
		return new Timespan(duration.toMillis());
	}

	public static String toString(long millis) {
		return toString(millis, 0);
	}

	public static String toString(long millis, int flags) {
		for (int i = 0; i < SIMPLE_VALUES.size() - 1; i++) {
			NonNullPair<Noun, Long> pair = SIMPLE_VALUES.get(i);
			long second1 = pair.getSecond();
			if (millis >= second1) {
				long remainder = millis % second1;
				double second = 1. * remainder / SIMPLE_VALUES.get(i + 1).getSecond();
				if (!"0".equals(Skript.toString(second))) { // bad style but who cares...
					return toString(Math.floor(1. * millis / second1), pair, flags) + " " + GeneralWords.and + " " + toString(remainder, flags);
				} else {
					return toString(1. * millis / second1, pair, flags);
				}
			}
		}
		return toString(1. * millis / SIMPLE_VALUES.get(SIMPLE_VALUES.size() - 1).getSecond(), SIMPLE_VALUES.get(SIMPLE_VALUES.size() - 1), flags);
	}

	private static String toString(double amount, NonNullPair<Noun, Long> pair, int flags) {
		return pair.getFirst().withAmount(amount, flags);
	}

	private final long millis;

	public Timespan() {
		millis = 0;
	}

	/**
	 * Builds a Timespan from the given milliseconds.
	 *
	 * @param millis The milliseconds of Timespan
	 */
	public Timespan(long millis) {
		Preconditions.checkArgument(millis >= 0, "millis must be >= 0");
		this.millis = millis;
	}

	/**
	 * Builds a Timespan from the given long parameter of a specific {@link TimePeriod}.
	 *
	 * @param timePeriod The requested TimePeriod
	 * @param time       The time of the requested TimePeriod
	 */
	public Timespan(TimePeriod timePeriod, long time) {
		Preconditions.checkArgument(time >= 0, "time must be >= 0");
		this.millis = time * timePeriod.getTime();
	}

	/**
	 * @deprecated Use {@link #Timespan(TimePeriod, long)}
	 */
	@Deprecated(forRemoval = true)
	public static Timespan fromTicks(long ticks) {
		return new Timespan(ticks * 50L);
	}

	/**
	 * @deprecated Use {@link #Timespan(TimePeriod, long)} instead.
	 */
	@Deprecated(forRemoval = true)
	public static Timespan fromTicks_i(long ticks) {
		return new Timespan(ticks * 50L);
	}

	/**
	 * @deprecated Use {@link Timespan#getAs(TimePeriod)}
	 */
	@Deprecated(forRemoval = true)
	public long getMilliSeconds() {
		return getAs(TimePeriod.MILLISECOND);
	}

	/**
	 * @deprecated Use {@link Timespan#getAs(TimePeriod)}
	 */
	@Deprecated(forRemoval = true)
	public long getTicks() {
		return getAs(TimePeriod.TICK);
	}

	/**
	 * @deprecated Use {@link Timespan#getAs(TimePeriod)}
	 */
	@Deprecated(forRemoval = true)
	public long getTicks_i() {
		return getAs(TimePeriod.TICK);
	}

	/**
	 * @return the amount of TimePeriod this timespan represents.
	 */
	public long getAs(TimePeriod timePeriod) {
		return millis / timePeriod.getTime();
	}

	/**
	 * @return Converts this timespan to a {@link Duration}.
	 */
	public Duration getDuration() {
		return Duration.ofMillis(millis);
	}

	@Override
	public long get(TemporalUnit unit) {
		if (unit instanceof TimePeriod period)
			return this.getAs(period);

		if (!(unit instanceof ChronoUnit chrono))
			throw new UnsupportedTemporalTypeException("Not a supported temporal unit: " + unit);

		return switch (chrono) {
			case MILLIS -> this.getAs(TimePeriod.MILLISECOND);
			case SECONDS -> this.getAs(TimePeriod.SECOND);
			case MINUTES -> this.getAs(TimePeriod.MINUTE);
			case HOURS -> this.getAs(TimePeriod.HOUR);
			case DAYS -> this.getAs(TimePeriod.DAY);
			case WEEKS -> this.getAs(TimePeriod.WEEK);
			case MONTHS -> this.getAs(TimePeriod.MONTH);
			case YEARS -> this.getAs(TimePeriod.YEAR);
			default -> throw new UnsupportedTemporalTypeException("Not a supported time unit: " + chrono);
		};
	}

	@Override
	public List<TemporalUnit> getUnits() {
		return List.<TemporalUnit>of(TimePeriod.values()).reversed();
	}

	@Override
	public Temporal addTo(Temporal temporal) {
		return temporal.plus(millis, MILLIS);
	}

	@Override
	public Temporal subtractFrom(Temporal temporal) {
		return temporal.minus(millis, MILLIS);
	}

	@Override
	public int compareTo(@Nullable Timespan time) {
		return Long.compare(millis, time == null ? millis : time.millis);
	}

	@Override
	public int hashCode() {
		return 31 + (int) (millis / Integer.MAX_VALUE);
	}

	@Override
	public boolean equals(@Nullable Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Timespan other))
			return false;

		return millis == other.millis;
	}

	@Override
	public String toString() {
		return toString(millis);
	}

	public String toString(int flags) {
		return toString(millis, flags);
	}

	/**
	 * Represents the unit used for the current {@link Timespan}.
	 */
	public enum TimePeriod implements TemporalUnit {

		MILLISECOND(1L),
		TICK(50L),
		SECOND(1000L),
		MINUTE(SECOND.time * 60L),
		HOUR(MINUTE.time * 60L),
		DAY(HOUR.time * 24L),
		WEEK(DAY.time * 7L),
		MONTH(DAY.time * 30L), // Who cares about 28, 29 or 31 days?
		YEAR(DAY.time * 365L);

		private final Noun name;
		private final Noun shortName;
		private final long time;

		TimePeriod(long time) {
			this.name = new Noun("time." + this.name().toLowerCase(Locale.ENGLISH) + ".full");
			this.shortName = new Noun("time." + this.name().toLowerCase(Locale.ENGLISH) + ".short");
			this.time = time;
		}

		public long getTime() {
			return time;
		}

		public String getFullForm() {
			return name.toString();
		}

		public String getShortForm() {
			return shortName.toString();
		}

		@Override
		public Duration getDuration() {
			return Duration.ofMillis(time);
		}

		@Override
		public boolean isDurationEstimated() {
			return false;
		}

		@Override
		public boolean isDateBased() {
			return false;
		}

		@Override
		public boolean isTimeBased() {
			return true;
		}

		@Override
		public <R extends Temporal> R addTo(R temporal, long amount) {
			//noinspection unchecked
			return (R) temporal.plus(amount, this);
		}

		@Override
		public long between(Temporal temporal1Inclusive, Temporal temporal2Exclusive) {
			return temporal1Inclusive.until(temporal2Exclusive, this);
		}

	}

}
