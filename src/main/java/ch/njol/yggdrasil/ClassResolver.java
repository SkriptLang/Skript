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
package ch.njol.yggdrasil;

import org.eclipse.jdt.annotation.Nullable;

public interface ClassResolver {
	
	/**
	 * Resolves a class by its ID.
	 * 
	 * @param id The ID used when storing objects
	 * @return The Class object that represents data with the given ID, or null if the ID does not belong to the implementor
	 */
	@Nullable
	Class<?> getClass(String id);
	
	/**
	 * Gets an ID for a Class. The ID is used to identify the type of saved object.
	 * <p>
	 * @param clazz The class to get the ID of
	 * @return The ID of the given class, or null if this is not a class of the implementor
	 */
	// TODO make sure that it's unique
	@Nullable
	String getID(Class<?> clazz);
	
}
