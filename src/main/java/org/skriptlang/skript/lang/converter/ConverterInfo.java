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

/**
 * Holds information about a {@link Converter}.
 *
 * @param <From> The type to convert from.
 * @param <To> The type to convert to.
 */
public final class ConverterInfo<From, To> {

	final Class<From> from;
	final Class<To> to;
	final Converter<From, To> converter;
	final int flags;

	public ConverterInfo(Class<From> from, Class<To> to, Converter<From, To> converter, int flags) {
		this.from = from;
		this.to = to;
		this.converter = converter;
		this.flags = flags;
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

	public int getFlags() {
		return flags;
	}

	@Override
	public String toString() {
		return "ConverterInfo{from=" + from + ",to=" + to + ",converter=" + converter + ",flag=" + flags + "}";
	}

}
