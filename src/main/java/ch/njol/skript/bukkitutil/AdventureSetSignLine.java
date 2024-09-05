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
package ch.njol.skript.bukkitutil;

import net.kyori.adventure.text.Component;

/**
 * A utility interface to access the Sign::line while also providing the same arguments to SignChangeEvent::line
 * Used in ExprSignText. Separated due to static versioning.
 */
@FunctionalInterface
public interface AdventureSetSignLine<T, S> {

	void line(int line, Component value);

	static void line(AdventureSetSignLine<Integer, String> setLineMethod, int line, Component value) {
		setLineMethod.line(line, value);
	}

}
