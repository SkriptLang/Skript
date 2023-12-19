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

import ch.njol.skript.classes.Converter;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Used to get a specific value from instances of some type.
 * 
 * @param <R> the returned value type
 * @param <A> the type which holds the value
 * @author Peter Güttinger
 */
@SuppressWarnings("deprecation") // for backwards compatibility
public abstract class Getter<R, A> implements Converter<A, R> {
	
	/**
	 * Gets a value from the given object.
	 * 
	 * @param arg the object to get the value from
	 * @return the value
	 */
	@Nullable
	public abstract R get(A arg);
	
	/**
	 * Convenience method to make getter implement converter
	 */
	@Override
	@Nullable
	public final R convert(final A a) {
		return get(a);
	}
	
}
