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

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.UnmodifiableView;
import org.skriptlang.skript.addon.SkriptAddon;

/**
 * The origin of a syntax, currently only used for documentation purposes.
 */
@ApiStatus.Experimental
@FunctionalInterface
public interface SyntaxOrigin {

	/**
	 * Constructs a syntax origin from an addon.
	 * @param addon The addon to construct this origin from.
	 * @return An origin pointing to the provided addon.
	 */
	@Contract("_ -> new")
	static SyntaxOrigin of(SkriptAddon addon) {
		return new AddonOrigin(addon);
	}

	/**
	 * A basic origin describing the addon a syntax has originated from.
	 * @see SyntaxOrigin#of(SkriptAddon)
	 */
	final class AddonOrigin implements SyntaxOrigin {

		private final SkriptAddon addon;

		private AddonOrigin(SkriptAddon addon) {
			this.addon = SkriptAddon.unmodifiableView(addon);
		}

		/**
		 * @return A string representing the name of the addon this origin describes.
		 * Equivalent to {@link SkriptAddon#name()}.
		 */
		@Override
		public String name() {
			return addon.name();
		}

		/**
		 * @return An unmodifiable view of the addon this origin describes.
		 * @see SkriptAddon#unmodifiableView(SkriptAddon) 
		 */
		@UnmodifiableView
		public SkriptAddon addon() {
			return addon;
		}

	}

	/**
	 * @return A string representing this origin.
	 */
	String name();

}
