/**
 * This file is part of Skript.
 * <p>
 * Skript is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Skript is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * <p>
 * Copyright 2011-2017 Peter Güttinger and contributors
 */
package ch.njol.skript.util;

import ch.njol.yggdrasil.YggdrasilSerializable;
import org.eclipse.jdt.annotation.Nullable;

public class GameruleValue<T> implements YggdrasilSerializable {
	private final T gameruleValue;

	public GameruleValue(T gameruleValue) {
		this.gameruleValue = gameruleValue;
	}

	public T getGameruleValue() {
		return gameruleValue;
	}

	@Override
	public String toString() {
		return gameruleValue.toString();
	}

	@Override
	public boolean equals(@Nullable Object other) {
		if (!(other instanceof GameruleValue)) return false;
		return this.gameruleValue.equals(((GameruleValue) other).gameruleValue);
	}
}
