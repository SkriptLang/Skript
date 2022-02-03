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
package org.skriptlang.skript.registration;

import ch.njol.skript.SkriptAddon;

import java.io.IOException;

/**
 * A module is a part of a {@link SkriptAddon} containing related syntax, classinfos, converters, etc.
 * Modules can be loaded using {@link SkriptAddon#loadModules(String, String...)}.
 * Note that when loading 'org.skriptlang.skript.X', the module class should be placed at 'org.skriptlang.skript.X.ModuleClassHere'
 * 	as the mentioned method will not search deeper than the provided subpackages.
 */
public abstract class Module {

	/**
	 * @param addon The addon responsible for registering this module.
	 *              To be used for registering syntax, classinfos, etc.
	 */
	public abstract void register(SkriptAddon addon) throws IOException;

	/**
	 * Loads syntax elements for this module assuming "elements" to be the location of syntax elements.
	 * @param loader The SkriptAddon to load syntax with.
	 */
	public final void loadSyntax(SkriptAddon loader) {
		loadSyntax(loader, "elements");
	}

	/**
	 * Loads syntax elements for this module.
	 * @param loader The SkriptAddon to load syntax with.
	 * @param packageName The location of syntax elements (ex: "elements")
	 */
	public final void loadSyntax(SkriptAddon loader, String packageName) {
		loader.loadClasses(getClass().getPackage().getName() + "." + packageName);
	}

}
