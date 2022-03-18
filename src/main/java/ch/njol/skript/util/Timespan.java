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

import java.util.HashMap;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.localization.GeneralWords;
import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.LanguageChangeListener;
import ch.njol.skript.localization.Noun;
import ch.njol.util.NonNullPair;
import ch.njol.util.coll.CollectionUtils;
import ch.njol.yggdrasil.YggdrasilSerializable;

public class Timespan implements YggdrasilSerializable, Comparable<Timespan> { // REMIND unit


	public enum Times {

		TICK(new Noun("time.tick"), 50L),
		SECOND(new Noun("time.second"), 1000L),
		MINUTE(new Noun("time.minute"), SECOND.getTime() * 60L),
		HOUR(new Noun("time.hour"), MINUTE.getTime() * 60L),
		DAY(new Noun("time.day"), HOUR.getTime() * 24L),
		WEEK(new Noun("time.week"), DAY.getTime() * 7L),
		MONTH(new Noun("time.month"), DAY.getTime() * 30L),
		YEAR(new Noun("time.year"), DAY.getTime() * 365L);

		private final Noun name;
		private final long time;

		Times(Noun name, long time) {
			this.name = name;
			this.time = time;
		}

		public Noun getName() {
			return name;
		}

		public long getTime() {
			return time;
		}

	}

	public static final Pattern TIMESPAN_PATTERN = Pattern.compile("^\\d+:\\d\\d(:\\d\\d)?(\\.\\d{1,4})?$");
	public static final Pattern TIMESPAN_NUMBER_PATTERN = Pattern.compile("^\\d+(\\.\\d+)?$");
	private static final HashMap<String, Long> parseValues = new HashMap<>();
	private final long millis;

	@SuppressWarnings("unchecked")
	final static NonNullPair<Noun, Long>[] simpleValues = new NonNullPair[] {
		new NonNullPair<>(Times.YEAR.getName(), Times.YEAR.getTime()),
		new NonNullPair<>(Times.MONTH.getName(), Times.MONTH.getTime()),
		new NonNullPair<>(Times.WEEK.getName(), Times.WEEK.getTime()),
		new NonNullPair<>(Times.DAY.getName(), Times.DAY.getTime()),
		new NonNullPair<>(Times.HOUR.getName(), Times.HOUR.getTime()),
		new NonNullPair<>(Times.MINUTE.getName(), Times.MINUTE.getTime()),
		new NonNullPair<>(Times.SECOND.getName(), Times.SECOND.getTime())
	};

	static {
		Language.addListener(new LanguageChangeListener() {
			@Override
			public void onLanguageChange() {
				for (Times t : Times.values()) {
					parseValues.put(t.getName().getSingular().toLowerCase(), t.getTime());
					parseValues.put(t.getName().getPlural().toLowerCase(), t.getTime());
				}
			}
		});
	}
	
	@Nullable
	public static Timespan parse(final String s) {
		if (s.isEmpty())
			return null;

		long t = 0;
		boolean minecraftTime = false;
		boolean isMinecraftTimeSet = false;

		if (TIMESPAN_PATTERN.matcher(s).matches()) { // MM:SS[.ms] or HH:MM:SS[.ms]
			final String[] ss = s.split("[:.]");
			final long[] times = {Times.HOUR.getTime(), Times.MINUTE.getTime(), Times.SECOND.getTime(), 1L}; // h, m, s, ms
			
			final int offset = ss.length == 3 && !s.contains(".") || ss.length == 4 ? 0 : 1;
			for (int i = 0; i < ss.length; i++) {
				t += times[offset + i] * Utils.parseLong("" + ss[i]);
			}
		} else { // <number> minutes/seconds/.. etc
			final String[] subs = s.toLowerCase().split("\\s+");
			for (int i = 0; i < subs.length; i++) {
				String sub = subs[i];
				
				if (sub.equals(GeneralWords.and.toString())) {
					if (i == 0 || i == subs.length - 1)
						return null;
					continue;
				}
				
				double amount = 1;
				if (Noun.isIndefiniteArticle(sub)) {
					if (i == subs.length - 1)
						return null;
					sub = subs[++i];
				} else if (TIMESPAN_NUMBER_PATTERN.matcher(sub).matches()) {
					if (i == subs.length - 1)
						return null;
					try {
						amount = Double.parseDouble(sub);
					} catch (NumberFormatException e) {
						throw new IllegalArgumentException("invalid timespan: " + s);
					}
					sub = subs[++i];
				}
				
				if (CollectionUtils.contains(Language.getList("time.real"), sub)) {
					if (i == subs.length - 1 || isMinecraftTimeSet && minecraftTime)
						return null;
					sub = subs[++i];
				} else if (CollectionUtils.contains(Language.getList("time.minecraft"), sub)) {
					if (i == subs.length - 1 || isMinecraftTimeSet && !minecraftTime)
						return null;
					minecraftTime = true;
					sub = subs[++i];
				}
				
				if (sub.endsWith(","))
					sub = sub.substring(0, sub.length() - 1);

				final Long d = parseValues.get(sub.toLowerCase());
				if (d == null)
					return null;
				
				if (minecraftTime && d != Times.TICK.getTime()) // times[0] == tick
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
	
	public Timespan(final long millis) {
		if (millis < 0)
			throw new IllegalArgumentException("millis must be >= 0");
		this.millis = millis;
	}
	
	/**
	 * @deprecated Use fromTicks_i(long ticks) instead. Since this method limits timespan to 50 * Integer.MAX_VALUE.
	 * Keeping this for older addons to stay working.
	 */
	@Deprecated
	public static Timespan fromTicks(final int ticks) {
		return new Timespan(ticks * Times.TICK.getTime());
	}
	
	public static Timespan fromTicks_i(final long ticks) {
		return new Timespan(ticks * Times.TICK.getTime());
	}
	
	public long getMilliSeconds() {
		return millis;
	}
	
	public long getTicks_i() {
		return Math.round((millis / 50f));
	}
	
	/**
	 * @deprecated Use getTicks_i() instead. This method limits timespan to Integer.MAX_VALUE.
	 * Keeping this for older addons to stay working.
	 * If you need the ticks for a method that takes an int input then it wouldn't matter.
	 */
	@Deprecated
	public int getTicks() {
		return Math.round((millis >= Float.MAX_VALUE ? Float.MAX_VALUE : millis) / 50f);
	}

	public long getSeconds() {
		return millis / Times.SECOND.getTime();
	}

	public long getMinutes() {
		return millis / Times.MINUTE.getTime();
	}

	public long getHours() {
		return millis / Times.HOUR.getTime();
	}

	public long getDays() {
		return millis / Times.DAY.getTime();
	}

	public long getWeeks() {
		return millis / Times.WEEK.getTime();
	}

	public long getMonths() {
		return millis / Times.MONTH.getTime();
	}

	public long getYears() {
		return millis / Times.YEAR.getTime();
	}
	
	@Override
	public String toString() {
		return toString(millis);
	}
	
	public String toString(final int flags) {
		return toString(millis, flags);
	}
	
	public static String toString(final long millis) {
		return toString(millis, 0);
	}
	
	@SuppressWarnings("null")
	public static String toString(final long millis, final int flags) {
		for (int i = 0; i < simpleValues.length - 1; i++) {
			if (millis >= simpleValues[i].getSecond()) {
				final double second = 1. * (millis % simpleValues[i].getSecond()) / simpleValues[i + 1].getSecond();
				if (!"0".equals(Skript.toString(second))) { // bad style but who cares...
					return toString(Math.floor(1. * millis / simpleValues[i].getSecond()), simpleValues[i], flags) + " " + GeneralWords.and + " " + toString(second, simpleValues[i + 1], flags);
				} else {
					return toString(1. * millis / simpleValues[i].getSecond(), simpleValues[i], flags);
				}
			}
		}
		return toString(1. * millis / simpleValues[simpleValues.length - 1].getSecond(), simpleValues[simpleValues.length - 1], flags);
	}
	
	private static String toString(final double amount, final NonNullPair<Noun, Long> p, final int flags) {
		return p.getFirst().withAmount(amount, flags);
	}
	
	@Override
	public int compareTo(final @Nullable Timespan o) {
		final long d = o == null ? millis : millis - o.millis;
		return d > 0 ? 1 : d < 0 ? -1 : 0;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (millis/Integer.MAX_VALUE);
		return result;
	}
	
	@Override
	public boolean equals(final @Nullable Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Timespan))
			return false;

		return millis == ((Timespan) obj).millis;
	}
	
}
