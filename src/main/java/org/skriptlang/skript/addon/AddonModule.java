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
package org.skriptlang.skript.addon;

import org.jetbrains.annotations.ApiStatus;
import org.skriptlang.skript.Skript;
import org.skriptlang.skript.registration.SyntaxRegistry;

/**
 * A module is a component of a {@link SkriptAddon} used for registering syntax and other {@link Skript} components.
 */
@ApiStatus.Experimental
@FunctionalInterface
public interface AddonModule {

	/**
	 *
	 * @param addon The addon this module belongs to.
	 * @param registry A syntax registry for registering this addon's syntax.
	 */
	void load(SkriptAddon addon, SyntaxRegistry registry);

}
