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
 * They are intended for providing organization and structure.
 * Modules can be loaded using {@link SkriptAddon#loadModules(String, String...)}.
 * Note that when loading 'org.skriptlang.skript.X', the module class should be placed at 'org.skriptlang.skript.X.ModuleClassHere'
 * 	as the mentioned method will not search deeper than the provided subpackages.
 * The example below is a possible organization structure that a project using Modules could use.
 * <pre>
 * <b>potions</b>
 * |- elements
 *    |- PotionsExpr.java
 * |- PotionsModule.java
 * <b>math</b>
 * |- elements
 *    |- MathExpr.java
 * |- MathModule.java
 * <b>MyPlugin.java</b>
 * </pre>
 */
public abstract class Module {

	/**
	 * @param addon The addon responsible for registering this module.
	 *              To be used for registering syntax, classinfos, etc.
	 */
	public abstract void register(SkriptAddon addon) throws IOException;

	/**
	 * Loads syntax elements for this module.
	 * @param loader The SkriptAddon to load syntax with.
	 * @param subPackageName The location of syntax elements (ex: "elements")
	 *                    Elements should <b>not</b> be contained within the main module package.
	 *                    They should be within a subpackage of the package containing the Module class.
	 */
	public void loadSyntax(SkriptAddon loader, String subPackageName) {
		loader.loadClasses(getClass().getPackage().getName() + "." + subPackageName);
	}

}
