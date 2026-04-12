package org.skriptlang.skript.addon;

import org.jetbrains.annotations.Unmodifiable;
import org.skriptlang.skript.Skript;
import org.skriptlang.skript.docs.Origin;
import org.skriptlang.skript.docs.Origin.AddonOrigin;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.SequencedCollection;
import java.util.stream.Collectors;

/**
 * A module is a component of a {@link SkriptAddon} used for registering syntax and other {@link Skript} components.
 * <br>
 * Modules have two loading phases: {@link #init(SkriptAddon)} followed by {@link #load(SkriptAddon)}.
 * <br>
 * The <code>init</code> phase should be used for loading components that are needed first or that may be used by other modules,
 *  such as class infos (think numeric types that are used everywhere).
 * <br>
 * The <code>load</code> phase should be used for loading components more specific to the module, such as syntax.
 * {@link #register(SkriptAddon, Registrar...)} provides a helper method to register many syntax elements and handles
 * assigning proper origins to the registered syntax.
 * <br>
 * For modules that are nested within other modules, use {@link HierarchicalAddonModule}. It provides automatic handling
 * for child modules and proper origins.
 * @see SkriptAddon#loadModules(AddonModule...)
 * @see HierarchicalAddonModule
 */
public interface AddonModule {

	/**
	 * Constructs an origin from an addon and a single module.
	 * @param addon The addon providing the module.
	 * @param module The module to construct this origin from.
	 * @return An origin from the provided information.
	 */
	static ModuleOrigin origin(SkriptAddon addon, AddonModule module) {
		return new AddonModuleImpl.ModuleOriginImpl(addon, module);
	}

	/**
	 * Constructs an origin from an addon and module chain.
	 * @param addon The addon providing the modules.
	 * @param modules The module chain, from most specific to root.
	 * @return An origin from the provided information.
	 */
	static ModuleOrigin origin(SkriptAddon addon, AddonModule... modules) {
		return new AddonModuleImpl.ModuleOriginImpl(addon, modules);
	}

	/**
	 * An origin to be used for something provided by one or more modules of an addon.
	 */
	sealed interface ModuleOrigin extends AddonOrigin permits AddonModuleImpl.ModuleOriginImpl {

		/**
		 * @return The providing modules for this origin, in order from most specific to least.
		 */
		@Unmodifiable SequencedCollection<AddonModule> modules();

		/**
		 * @return The names of the modules providing this origin, in order from most specific to least.
		 * @deprecated Use {@link #modules()} and call {@link AddonModule#name()} on each.
		 */
		@Deprecated(since="2.15", forRemoval = true)
		default String moduleName() {
			return modules().stream()
				.map(AddonModule::name)
				.collect(Collectors.joining(", "));
		}

	}

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
	 * @return The name of this module.
	 */
	String name();

	/**
	 * @return An origin representing this module.
	 */
	default Origin origin(SkriptAddon addon) {
		return AddonModule.origin(addon, this);
	}

	/**
	 * Provides a syntax registry that auto-applies the origin of this module/addon.
	 * @param addon The addon to register with
	 * @return An origin-applying {@link SyntaxRegistry}.
	 */
	default SyntaxRegistry moduleRegistry(SkriptAddon addon) {
		return SyntaxRegistry.withOrigin(addon.syntaxRegistry(), origin(addon));
	}

	/**
	 * Helper method that calls the given methods with a origin-applying {@link SyntaxRegistry}
	 * @param addon The addon to register with.
	 * @param registrationMethods A series of consumers to call to register syntax.
	 */
	default void register(SkriptAddon addon, Registrar... registrationMethods) {
		SyntaxRegistry registry = moduleRegistry(addon);
		for (var func : registrationMethods) {
			func.register(registry);
		}
	}

	/**
	 * A method of registration via a {@link SyntaxRegistry}
	 */
	@FunctionalInterface
	interface Registrar {
		void register(SyntaxRegistry registry);
	}

}
