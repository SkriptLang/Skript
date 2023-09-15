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
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.util;

import java.lang.annotation.*;
import java.util.Iterator;

/**
 * Represents a class which is a container, i.e. something like a collection.<br>
 * If this is used, a {@link ContainerType} annotation must be added to the implementing class which holds the class instance the containser holds.
 *
 * @author Peter Güttinger
 */
public interface Container<T> {

	@SuppressWarnings("null")
	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	@Documented
	public static @interface ContainerType {
		Class<?> value();
	}

	/**
	 * @return All element within this container in no particular order
	 */
	public Iterator<T> containerIterator();

}
