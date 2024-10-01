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

import org.jetbrains.annotations.Nullable;

import ch.njol.yggdrasil.YggdrasilSerializable;

/**
 * @author Peter Güttinger
 */
public class Timeperiod implements YggdrasilSerializable {
	
	public final int start, end;
	
	public Timeperiod() {
		start = end = 0;
	}
	
	public Timeperiod(final int start, final int end) {
		this.start = (start + 24000) % 24000;
		this.end = (end + 24000) % 24000;
	}
	
	public Timeperiod(final int time) {
		start = end = (time + 24000) % 24000;
	}
	
	public boolean contains(final int time) {
		return start <= end ? (time >= start && time <= end) : (time <= end || time >= start);
	}
	
	public boolean contains(final Time t) {
		return contains(t.getTicks());
	}
	
	/**
	 * @return "start-end" or "start" if start == end
	 */
	@Override
	public String toString() {
		return "" + Time.toString(start) + (start == end ? "" : "-" + Time.toString(end));
	}
	
	@Override
	public int hashCode() {
		return start + end << 16;
	}
	
	@Override
	public boolean equals(final @Nullable Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof Timeperiod))
			return false;
		final Timeperiod other = (Timeperiod) obj;
		return (end == other.end && start == other.start);
	}
	
}
