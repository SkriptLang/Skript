package org.skriptlang.skript.addon;

import org.jetbrains.annotations.ApiStatus;
import org.skriptlang.skript.Skript;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxOrigin;
import org.skriptlang.skript.registration.SyntaxRegistry.Key;

import java.util.Set;

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
	 * Registers syntax such that it belongs to the current module.
	 *
	 * @param addon The addon this module belongs to.
	 * @param registry The registry to add this syntax to.
	 * @param cls The syntax info.
	 * @param <I> The type of syntax.
	 */
	default <I extends SyntaxInfo<?>> void register(SkriptAddon addon, Key<I> registry, I cls) {
		//noinspection unchecked
		addon.syntaxRegistry().register(registry, (I) cls.toBuilder()
				.origin(SyntaxOrigin.of(addon, this))
				.build());
	}

	/**
	 * Registers syntax such that it belongs to the current module.
	 *
	 * @param addon The addon this module belongs to.
	 * @param registry The registry to add this syntax to.
	 * @param classes The syntax infos.
	 * @param <I> The type of syntax.
	 */
	@SuppressWarnings("unchecked")
	default <I extends SyntaxInfo<?>> void register(SkriptAddon addon, Key<I> registry, I... classes) {
		for (I cls : classes) {
			register(addon, registry, cls);
		}
	}

}
