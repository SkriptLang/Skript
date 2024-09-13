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
package ch.njol.skript.config;

import java.util.Locale;

import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import org.skriptlang.skript.lang.converter.Converter;

/**
 * @author Peter Güttinger
 */
public class EnumParser<E extends Enum<E>> implements Converter<String, E> {
	
	private final Class<E> enumType;
	@Nullable
	private final String allowedValues;
	private final String type;
	
	public EnumParser(final Class<E> enumType, final String type) {
		assert enumType != null;
		this.enumType = enumType;
		this.type = type;
		if (enumType.getEnumConstants().length <= 12) {
			final StringBuilder b = new StringBuilder(enumType.getEnumConstants()[0].name());
			for (final E e : enumType.getEnumConstants()) {
				if (b.length() != 0)
					b.append(", ");
				b.append(e.name().toLowerCase(Locale.ENGLISH).replace('_', ' '));
			}
			allowedValues = b.toString();
		} else {
			allowedValues = null;
		}
	}
	
	@Override
	@Nullable
	public E convert(final String s) {
		try {
			return Enum.valueOf(enumType, s.toUpperCase(Locale.ENGLISH).replace(' ', '_'));
		} catch (final IllegalArgumentException e) {
			Skript.error("'" + s + "' is not a valid value for " + type + (allowedValues == null ? "" : ". Allowed values are: " + allowedValues));
			return null;
		}
	}
	
	@Override
	public String toString() {
		return "EnumParser{enum=" + enumType + ",allowedValues=" + allowedValues + ",type=" + type + "}";
	}
}
