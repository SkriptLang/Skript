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
package org.skriptlang.skript.lang.converter;

import ch.njol.skript.lang.Debuggable;
import ch.njol.skript.registrations.Classes;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Holds information about a {@link Converter}.
 *
 * @param <From> The type to convert from.
 * @param <To> The type to convert to.
 */

public final class ConverterInfo<From, To> implements Debuggable {

	final Class<From> from;
	final Class<To> to;
	final Converter<From, To> converter;
	final int flag;

	public ConverterInfo(Class<From> from, Class<To> to, Converter<From, To> converter, int flag) {
		this.from = from;
		this.to = to;
		this.converter = converter;
		this.flag = flag;
	}

	public Class<From> getFrom() {
		return from;
	}

	public Class<To> getTo() {
		return to;
	}

	public Converter<From, To> getConverter() {
		return converter;
	}

	public int getFlag() {
		return flag;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		if (debug)
			return converter.toString(e, true);
		return toString();
	}

	@Override
	public String toString() {
		return Classes.getExactClassName(from) + " to " + Classes.getExactClassName(to);
	}

}
