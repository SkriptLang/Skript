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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.localization.Message;
import ch.njol.util.Math2;
import ch.njol.yggdrasil.YggdrasilSerializable;

/**
 * @author Peter Güttinger
 */
public class Time implements YggdrasilSerializable {
	
	private final static int TICKS_PER_HOUR = 1000, TICKS_PER_DAY = 24 * TICKS_PER_HOUR;
	private final static double TICKS_PER_MINUTE = 1000. / 60;
	/**
	 * 0 ticks == 6:00
	 */
	private final static int HOUR_ZERO = 6 * TICKS_PER_HOUR;
	
	private final int time;

	private static final Pattern DAY_TIME_PATTERN = Pattern.compile("(\\d?\\d)(:(\\d\\d))? ?(am|pm)", Pattern.CASE_INSENSITIVE);
	private static final Pattern TIME_PATTERN = Pattern.compile("\\d?\\d:\\d\\d", Pattern.CASE_INSENSITIVE);

	public Time() {
		time = 0;
	}
	
	public Time(final int time) {
		this.time = Math2.mod(time, TICKS_PER_DAY);
	}
	
	/**
	 * @return Ticks in Minecraft time (0 ticks == 6:00)
	 */
	public int getTicks() {
		return time;
	}
	
	/**
	 * @return Ticks in day time (0 ticks == 0:00)
	 */
	public int getTime() {
		return (time + HOUR_ZERO) % TICKS_PER_DAY;
	}
	
	@Override
	public String toString() {
		return toString(time);
	}
	
	public static String toString(final int ticks) {
		assert 0 <= ticks && ticks < TICKS_PER_DAY;
		final int t = (ticks + HOUR_ZERO) % TICKS_PER_DAY;
		int hours = t / TICKS_PER_HOUR;
		int minutes = (int) (Math.round((t % TICKS_PER_HOUR) / TICKS_PER_MINUTE));
		if (minutes >= 60) {
			hours = (hours + 1) % 24;
			minutes -= 60;
		}
		return "" + hours + ":" + (minutes < 10 ? "0" : "") + minutes;
	}
	
	private final static Message m_error_24_hours = new Message("time.errors.24 hours");
	private final static Message m_error_12_hours = new Message("time.errors.12 hours");
	private final static Message m_error_60_minutes = new Message("time.errors.60 minutes");
	
	/**
	 * @param s The trim()med string to parse
	 * @return The parsed time of null if the input was invalid
	 */
	@SuppressWarnings("null")
	@Nullable
	public static Time parse(final String s) {
//		if (s.matches("\\d+")) {
//			return new Time(Integer.parseInt(s));
//		} else
		if (TIME_PATTERN.matcher(s).matches()) {
			int hours = Utils.parseInt(s.split(":")[0]);
			if (hours == 24) { // allows to write 24:00 - 24:59 instead of 0:00-0:59
				hours = 0;
			} else if (hours > 24) {
				Skript.error("" + m_error_24_hours);
				return null;
			}
			final int minutes = Utils.parseInt(s.split(":")[1]);
			if (minutes >= 60) {
				Skript.error("" + m_error_60_minutes);
				return null;
			}
			return new Time((int) Math.round(hours * TICKS_PER_HOUR - HOUR_ZERO + minutes * TICKS_PER_MINUTE));
		} else {
			final Matcher m = DAY_TIME_PATTERN.matcher(s);
			if (m.matches()) {
				int hours = Utils.parseInt(m.group(1));
				if (hours == 12) {
					hours = 0;
				} else if (hours > 12) {
					Skript.error("" + m_error_12_hours);
					return null;
				}
				int minutes = 0;
				if (m.group(3) != null)
					minutes = Utils.parseInt(m.group(3));
				if (minutes >= 60) {
					Skript.error("" + m_error_60_minutes);
					return null;
				}
				if (m.group(4).equalsIgnoreCase("pm"))
					hours += 12;
				return new Time((int) Math.round(hours * TICKS_PER_HOUR - HOUR_ZERO + minutes * TICKS_PER_MINUTE));
			}
		}
		return null;
	}
	
	@Override
	public int hashCode() {
		return time;
	}
	
	@Override
	public boolean equals(final @Nullable Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Time))
			return false;
		final Time other = (Time) obj;
		return time == other.time;
	}
	
}
