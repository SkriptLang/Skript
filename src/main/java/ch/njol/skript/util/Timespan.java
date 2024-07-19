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
package ch.njol.skript.util;

import ch.njol.skript.Skript;
import ch.njol.skript.localization.GeneralWords;
import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.LanguageChangeListener;
import ch.njol.skript.localization.Noun;
import ch.njol.util.NonNullPair;
import ch.njol.util.coll.CollectionUtils;
import ch.njol.yggdrasil.YggdrasilSerializable;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.ApiStatus.ScheduledForRemoval;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Timespan implements YggdrasilSerializable, Comparable<Timespan> { // REMIND unit


	public enum TimePeriod {

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
		private final long time;

		TimePeriod(long time) {
			this.name = new Noun("time." + this.name().toLowerCase(Locale.ENGLISH));
			this.time = time;
		}

		public long getTime() {
			return time;
		}

	}

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
		Language.addListener(new LanguageChangeListener() {
			@Override
			public void onLanguageChange() {
				for (TimePeriod time : TimePeriod.values()) {
					PARSE_VALUES.put(time.name.getSingular().toLowerCase(Locale.ENGLISH), time.getTime());
					PARSE_VALUES.put(time.name.getPlural().toLowerCase(Locale.ENGLISH), time.getTime());
				}
			}
		});
	}

	private static final Pattern TIMESPAN_PATTERN = Pattern.compile("^(\\d+):(\\d\\d)(:\\d\\d){0,2}(?<ms>\\.\\d{1,4})?$");
	private static final Pattern TIMESPAN_NUMBER_PATTERN = Pattern.compile("^\\d+(\\.\\d+)?$");
	private static final Pattern TIMESPAN_SPLIT_PATTERN = Pattern.compile("[:.]");

	private final long millis;
	
	@Nullable
	public static Timespan parse(String value) {
		if (value.isEmpty())
			return null;

		long t = 0;
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
				t += times[offset + i] * Utils.parseLong("" + substring[i]);
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

				Long d = PARSE_VALUES.get(sub.toLowerCase(Locale.ENGLISH));
				if (d == null)
					return null;
				
				if (minecraftTime && d != TimePeriod.TICK.time)
					amount /= 72f;
				
				t += Math.round(amount * d);
				
				isMinecraftTimeSet = true;
				
			}
		}

		return new Timespan(t);
	}
	
	public Timespan() {
		millis = 0;
	}

	/**
	 * Builds a Timespan from the given milliseconds.
	 *
	 * @param millis The milliseconds of Timespan
	 */
	public Timespan(long millis) {
		if (millis < 0)
			throw new IllegalArgumentException("millis must be >= 0");
		this.millis = millis;
	}

	/**
	 * Builds a Timespan from the given long parameter of a specific {@link TimePeriod}.
	 *
	 * @param timePeriod The requested TimePeriod
	 * @param time The time of the requested TimePeriod
	 */
	public Timespan(TimePeriod timePeriod, long time) {
		if (time < 0)
			throw new IllegalArgumentException("time must be >= 0");
		this.millis = time * timePeriod.getTime();
	}

	/**
	 * Builds a Timespan from the given long parameter.
	 *
	 * @deprecated Use {@link #Timespan(TimePeriod, long)}
	 * 
	 * @param ticks The amount of Minecraft ticks to convert to a timespan.
	 * @return Timespan based on the provided long.
	 */
	@Deprecated
	@ApiStatus.ScheduledForRemoval
	public static Timespan fromTicks(long ticks) {
		return new Timespan(ticks * 50L);
	}

	/**
	 * @deprecated Use {@link Timespan#fromTicks(long)} instead. Old API naming changes.
	 */
	@Deprecated
	@ScheduledForRemoval
	public static Timespan fromTicks_i(long ticks) {
		return new Timespan(ticks * 50L);
	}

	/**
	 * @deprecated Use {@link Timespan#getAs(TimePeriod)}
	 *
	 * @return the amount of milliseconds this timespan represents.
	 */
	@Deprecated
	@ScheduledForRemoval
	public long getMilliSeconds() {
		return millis;
	}

	/**
	 * @deprecated Use {@link Timespan#getAs(TimePeriod)}
	 *
	 * @return the amount of Minecraft ticks this timespan represents.
	 */
	@Deprecated
	@ScheduledForRemoval
	public long getTicks() {
		return Math.round((millis / 50.0));
	}

	/**
	 * @return the amount of TimePeriod this timespan represents.
	 */
	public long getAs(TimePeriod timePeriod) {
		return millis / timePeriod.getTime();
	}

	/**
	 * @deprecated Use getTicks() instead. Old API naming changes.
	 */
	@Deprecated
	@ScheduledForRemoval
	public long getTicks_i() {
		return Math.round((millis / 50.0));
	}

	@Override
	public String toString() {
		return toString(millis);
	}
	
	public String toString(int flags) {
		return toString(millis, flags);
	}
	
	public static String toString(long millis) {
		return toString(millis, 0);
	}
	
	@SuppressWarnings("null")
	public static String toString(long millis, int flags) {
		for (int i = 0; i < SIMPLE_VALUES.size() - 1; i++) {
			if (millis >= SIMPLE_VALUES.get(i).getSecond()) {
				long remainder = millis % SIMPLE_VALUES.get(i).getSecond();
				double second = 1. * remainder / SIMPLE_VALUES.get(i + 1).getSecond();
				if (!"0".equals(Skript.toString(second))) { // bad style but who cares...
					return toString(Math.floor(1. * millis / SIMPLE_VALUES.get(i).getSecond()), SIMPLE_VALUES.get(i), flags) + " " + GeneralWords.and + " " + toString(remainder, flags);
				} else {
					return toString(1. * millis / SIMPLE_VALUES.get(i).getSecond(), SIMPLE_VALUES.get(i), flags);
				}
			}
		}
		return toString(1. * millis / SIMPLE_VALUES.get(SIMPLE_VALUES.size() - 1).getSecond(), SIMPLE_VALUES.get(SIMPLE_VALUES.size() - 1), flags);
	}
	
	private static String toString(double amount, NonNullPair<Noun, Long> pair, int flags) {
		return pair.getFirst().withAmount(amount, flags);
	}

	/**
	 * Compare this Timespan with another
	 * @param time the Timespan to be compared.
	 * @return -1 if this Timespan is less than argument Timespan, 0 if equals and 1 if greater than
	 */
	@Override
	public int compareTo(@Nullable Timespan time) {
		return Long.compare(millis, time == null ? millis : time.millis);
	}
	
	@Override
	public int hashCode() {
		int prime = 31;
		int result = 1;
		result = prime * result + (int) (millis / Integer.MAX_VALUE);
		return result;
	}
	
	@Override
	public boolean equals(@Nullable Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Timespan))
			return false;

		return millis == ((Timespan) obj).millis;
	}
	
}
