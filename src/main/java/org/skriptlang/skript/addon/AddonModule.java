package org.skriptlang.skript.addon;

import org.skriptlang.skript.Skript;
import org.skriptlang.skript.docs.Origin.AddonOrigin;

/**
 * A module is a component of a {@link SkriptAddon} used for registering syntax and other {@link Skript} components.
 * <br>
 * Modules have two loading phases: {@link #init(SkriptAddon)} followed by {@link #load(SkriptAddon)}.
 * <br>
 * The <code>init</code> phase should be used for loading components that are needed first or that may be used by other modules,
 *  such as class infos (think numeric types that are used everywhere).
 * <br>
 * The <code>load</code> phase should be used for loading components more specific to the module, such as syntax.
 * @see SkriptAddon#loadModules(AddonModule...)
 */
public interface AddonModule {

	/**
	 * Constructs an origin from an addon and module name.
	 * @param addon The addon providing the module.
	 * @param moduleNames The names of the providing modules. The most specific module name should be first.
	 * @return An origin from the provided information.
	 */
	static ModuleOrigin origin(SkriptAddon addon, String... moduleNames) {
		return new AddonModuleImpl.ModuleOriginImpl(addon, moduleNames);
	}

	/**
	 * An origin to be used for something provided by one or more modules of an addon.
	 */
	sealed interface ModuleOrigin extends AddonOrigin permits AddonModuleImpl.ModuleOriginImpl, ChildAddonModule.ChildModuleOriginImpl {

		/**
		 * @return The names of the modules represented by this origin.
		 */
		String[] moduleNames();

	}

	/**
	 * Used for loading the components of this module that are needed first or by other modules (e.g. class infos).
	 * <b>This method will always be called before {@link #load(SkriptAddon)}</b>.
	 * @param addon The addon this module belongs to.
	 * @see #load(SkriptAddon)
	 */
	default void init(SkriptAddon addon) { }

	/**
	 * Used for loading the components (e.g. syntax) of this module.
	 * @param addon The addon this module belongs to.
	 * @see #init(SkriptAddon)
	 */
	void load(SkriptAddon addon);

	/**
	 * Allow addons to specify whether they can load or not.
	 * Called prior to {@link #init(SkriptAddon)}
	 *
	 * @param addon The addon this module belongs to.
	 * @return Whether this module can load.
	 */
	default boolean canLoad(SkriptAddon addon) {
		return true;
	}

	/**
	 * @return A name representing this module to be using in the {@link ModuleOrigin}. e.g. "discord", "nbt", "particles"...
	 */
	String name();

	/**
	 * @return An origin representing this module.
	 */
	default ModuleOrigin origin(SkriptAddon addon) {
		return AddonModule.origin(addon, name());
	}

}
