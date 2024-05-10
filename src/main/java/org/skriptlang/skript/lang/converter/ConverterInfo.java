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
 * @param <F> The type to convert from.
 * @param <T> The type to convert to.
 */
public final class ConverterInfo<F, T> {

	private final Class<F> from;
	private final Class<T> to;
	private final Converter<F, T> converter;
	private final int flags;

	public ConverterInfo(Class<F> from, Class<T> to, Converter<F, T> converter, int flags) {
		this.from = from;
		this.to = to;
		this.converter = converter;
		this.flags = flags;
	}

	public Class<F> getFrom() {
		return from;
	}

	public Class<T> getTo() {
		return to;
	}

	public Converter<F, T> getConverter() {
		return converter;
	}

	public int getFlags() {
		return flags;
	}

	@Override
	public String toString() {
		return "ConverterInfo{from=" + from + ",to=" + to + ",converter=" + converter + ",flags=" + flags + "}";
	}

}
