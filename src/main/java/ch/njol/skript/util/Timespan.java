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
import java.util.Locale;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.localization.GeneralWords;
import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.LanguageChangeListener;
import ch.njol.skript.localization.Noun;
import ch.njol.util.NonNullPair;
import ch.njol.util.coll.CollectionUtils;
import ch.njol.yggdrasil.YggdrasilSerializable;

/**
 * @author Peter Güttinger
 * @edited by Mirreducki. Increased maximum timespan.
 */
public class Timespan implements YggdrasilSerializable, Comparable<Timespan> { // REMIND unit

	private final static Noun m_tick = new Noun("time.tick");
	private final static Noun m_second = new Noun("time.second");
	private final static Noun m_minute = new Noun("time.minute");
	private final static Noun m_hour = new Noun("time.hour");
	private final static Noun m_day = new Noun("time.day");
	private final static Noun m_week = new Noun("time.week");
	private final static Noun m_month = new Noun("time.month");
	private final static Noun m_year = new Noun("time.year");
	final static Noun[] names = {m_tick, m_second, m_minute, m_hour, m_day, m_week, m_month, m_year};
	final static long[] times = {50L, 1000L, 1000L * 60L, 1000L * 60L * 60L, 1000L * 60L * 60L * 24L,  1000L * 60L * 60L * 24L * 7L,  1000L * 60L * 60L * 24L * 30L,  1000L * 60L * 60L * 24L * 365L};
	final static HashMap<String, Long> parseValues = new HashMap<>();
	static {
		Language.addListener(new LanguageChangeListener() {
			@Override
			public void onLanguageChange() {
				for (int i = 0; i < names.length; i++) {
					parseValues.put(names[i].getSingular().toLowerCase(Locale.ENGLISH), times[i]);
					parseValues.put(names[i].getPlural().toLowerCase(Locale.ENGLISH), times[i]);
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
		if (s.matches("^\\d+:\\d\\d(:\\d\\d)?(\\.\\d{1,4})?$")) { // MM:SS[.ms] or HH:MM:SS[.ms]
			final String[] ss = s.split("[:.]");
			final long[] times = {1000L * 60L * 60L, 1000L * 60L, 1000L, 1L}; // h, m, s, ms
			
			final int offset = ss.length == 3 && !s.contains(".") || ss.length == 4 ? 0 : 1;
			for (int i = 0; i < ss.length; i++) {
				t += times[offset + i] * Utils.parseLong("" + ss[i]);	
			}
		} else { // <number> minutes/seconds/.. etc
			final String[] subs = s.toLowerCase(Locale.ENGLISH).split("\\s+");
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
					amount = 1;
					sub = subs[++i];
				} else if (sub.matches("^\\d+(\\.\\d+)?$")) {
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

				final Long d = parseValues.get(sub.toLowerCase(Locale.ENGLISH));
				if (d == null)
					return null;
				
				if (minecraftTime && d != times[0]) // times[0] == tick
					amount /= 72f;
				
				t += Math.round(amount * d);
				
				isMinecraftTimeSet = true;
				
			}
		}
		return new Timespan(t);
	}
	
	private final long millis;
	
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
	 * @addon I only keep this to allow for older addons to still work. / Mirre
	 */
	@Deprecated
	public static Timespan fromTicks(final int ticks) {
		return new Timespan(ticks * 50L);
	}
	
	public static Timespan fromTicks_i(final long ticks) {
		return new Timespan(ticks * 50L);
	}
	
	public long getMilliSeconds() {
		return millis;
	}
	
	public long getTicks_i() {
		return Math.round((millis / 50.0));
	}
	
	/**
	 * @deprecated Use getTicks_i() instead. Since this method limits timespan to Integer.MAX_VALUE.
	 * @addon I only keep this to allow for older addons to still work. / Mirre
	 * @Well if need the ticks because of a method that takes a int input it doesn't really matter.
	 */
	@Deprecated
	public int getTicks() {
		return Math.round((millis >= Float.MAX_VALUE ? Float.MAX_VALUE : millis) / 50f);
	}
	
	@Override
	public String toString() {
		return toString(millis);
	}
	
	public String toString(final int flags) {
		return toString(millis, flags);
	}
	
	@SuppressWarnings("unchecked")
	final static NonNullPair<Noun, Long>[] simpleValues = new NonNullPair[] {
			new NonNullPair<>(m_day,  1000L * 60 * 60 * 24),
			new NonNullPair<>(m_hour, 1000L * 60 * 60),
			new NonNullPair<>(m_minute, 1000L * 60),
			new NonNullPair<>(m_second, 1000L)
	};
	
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
		final Timespan other = (Timespan) obj;
		if (millis != other.millis)
			return false;
		return true;
	}
	
}
