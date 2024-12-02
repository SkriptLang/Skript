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

import java.lang.reflect.Field;
import java.util.Locale;

import org.jetbrains.annotations.Nullable;

/**
 * @author Peter Güttinger
 */
public class OptionSection {
	
	public final String key;
	
	public OptionSection(final String key) {
		this.key = key;
	}
	
	@SuppressWarnings("unchecked")
	@Nullable
	public final <T> T get(String key) {
		if (this.getClass() == OptionSection.class)
			return null;
		key = "" + key.toLowerCase(Locale.ENGLISH);
		for (final Field f : this.getClass().getDeclaredFields()) {
			f.setAccessible(true);
			if (Option.class.isAssignableFrom(f.getType())) {
				try {
					final Option<?> o = (Option<?>) f.get(this);
					if (o.key.equals(key))
						return (T) o.value();
				} catch (final IllegalArgumentException e) {
					assert false;
				} catch (final IllegalAccessException e) {
					assert false;
				}
			}
		}
		return null;
	}
	
}
