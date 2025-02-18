package org.skriptlang.skript.addon;

import org.jetbrains.annotations.ApiStatus;
import org.skriptlang.skript.Skript;

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
@FunctionalInterface
@ApiStatus.Experimental
public interface AddonModule {

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

}
