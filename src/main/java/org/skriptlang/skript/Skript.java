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
package org.skriptlang.skript;

import ch.njol.util.NonNullPair;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.UnmodifiableView;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.Collection;

/**
 * The main class for everything related to Skript.
 */
@ApiStatus.Experimental
public interface Skript extends SkriptAddon {

	/**
	 * This implementation makes use of default implementations of required classes.
	 * @param name The name for this Skript instance to use.
	 * @return A pair containing a default Skript implementation and the modifiable {@link SkriptAddon} backing it.
	 */
	@Contract("_ -> new")
	static NonNullPair<Skript, SkriptAddon> createInstance(String name) {
		SkriptImpl skript = new SkriptImpl(name);
		return new NonNullPair<>(skript, skript.addon);
	}

	/**
	 * @return An unmodifiable view of the syntax registry containing all syntax registered by this Skript and its addons.
	 */
	@UnmodifiableView
	SyntaxRegistry registry();

	/**
	 * Registers the provided addon with this Skript and loads the provided modules.
	 * @param name The name of the addon to register.
	 */
	@Contract("_ -> new")
	SkriptAddon registerAddon(String name);

	/**
	 * @return An unmodifiable snapshot of addons currently registered with this Skript.
	 */
	@Unmodifiable
	Collection<SkriptAddon> addons();

}
