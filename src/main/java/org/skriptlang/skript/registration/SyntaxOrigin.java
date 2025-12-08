package org.skriptlang.skript.registration;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.skriptlang.skript.addon.AddonModule;
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
	 * Constructs a syntax origin from an addon and module.
	 * @param addon The addon to construct this origin from.
	 * @param module The module to include in this origin.
	 * @return An origin pointing to the provided addon and module.
	 */
	@Contract("_, _ -> new")
	static SyntaxOrigin of(SkriptAddon addon, AddonModule module) {
		return new ModuleOrigin(addon, module);
	}

	/**
	 * An origin describing the addon and module a syntax has originated from.
	 * @see SyntaxOrigin#of(SkriptAddon, AddonModule)
	 */
	final class ModuleOrigin implements SyntaxOrigin {

		private final SkriptAddon addon;
		private final AddonModule module;

		private ModuleOrigin(SkriptAddon addon, AddonModule module) {
			this.addon = addon.unmodifiableView();
			this.module = module;
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

		/**
		 * @return The module used for registering this element.
		 */
		public AddonModule module() {
			return module;
		}

	}

	/**
	 * @return A string representing this origin.
	 */
	String name();

}
