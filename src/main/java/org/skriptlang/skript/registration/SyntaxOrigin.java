package org.skriptlang.skript.registration;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.skriptlang.skript.addon.SkriptAddon;

/**
 * The origin of a syntax, currently only used for documentation purposes.
 */
@FunctionalInterface
@ApiStatus.Experimental
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
			this.addon = addon.unmodifiableView();
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
		 * @see SkriptAddon#unmodifiableView()
		 */
		public SkriptAddon addon() {
			return addon;
		}

	}

	/**
	 * @return A string representing this origin.
	 */
	String name();

}
