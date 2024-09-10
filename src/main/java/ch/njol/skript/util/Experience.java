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
public class Experience implements YggdrasilSerializable {
	
	private final int xp;
	
	public Experience() {
		xp = -1;
	}
	
	public Experience(final int xp) {
		this.xp = xp;
	}
	
	public int getXP() {
		return xp == -1 ? 1 : xp;
	}
	
	public int getInternalXP() {
		return xp;
	}
	
	@Override
	public String toString() {
		return xp == -1 ? "xp" : xp + " xp";
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + xp;
		return result;
	}
	
	@Override
	public boolean equals(final @Nullable Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Experience))
			return false;
		final Experience other = (Experience) obj;
		if (xp != other.xp)
			return false;
		return true;
	}
	
}
