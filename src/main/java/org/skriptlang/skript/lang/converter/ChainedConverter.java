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

import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

final class ChainedConverter<From, Middle, To> implements Converter<From, To> {

	private final Converter<From, Middle> first;
	private final Converter<Middle, To> second;

	public ChainedConverter(Converter<From, Middle> first, Converter<Middle, To> second) {
		this.first = first;
		this.second = second;
	}

	@Override
	@Nullable
	public To convert(From from) {
		Middle middle = first.convert(from);
		if (middle == null)
			return null;
		return second.convert(middle);
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return first.toString(e, debug) + " -> " + second.toString(e, debug);
	}

	@Override
	public String toString() {
		return "ChainedConverter{first=" + first + ",second=" + second + "}";
	}

}
