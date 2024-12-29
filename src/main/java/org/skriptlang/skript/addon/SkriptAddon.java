package org.skriptlang.skript.addon;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.skriptlang.skript.Skript;
import org.skriptlang.skript.localization.Localizer;
import org.skriptlang.skript.registration.SyntaxRegistry;
import org.skriptlang.skript.util.Registry;
import org.skriptlang.skript.util.ViewProvider;

import java.util.function.Supplier;

/**
 * A Skript addon is an extension to Skript that expands its features.
 * Typically, an addon instance may be obtained through {@link Skript#registerAddon(Class, String)}.
 */
@ApiStatus.Experimental
public interface SkriptAddon extends ViewProvider<SkriptAddon> {

	/**
	 * @return A class from the application that registered this addon.
	 * Typically, this is the main class or the specific class in which registration occurred.
	 */
	Class<?> source();

	/**
	 * @return The name of this addon.
	 */
	String name();

	/**
	 * Stores a registry under <code>registryClass</code>.
	 * If a registry is already stored under <code>registryClass</code>, it will be replaced.
	 * @param registryClass The class (key) to store <code>registry</code> under.
	 * @param registry The registry to store.
	 * @param <R> The type of registry.
	 */
	<R extends Registry<?>> void storeRegistry(Class<R> registryClass, R registry);

	/**
	 * Removes the registry stored under <code>registryClass</code>.
	 * It is safe to call this method even if a registry is not stored under <code>registryClass</code>.
	 * @param registryClass The class (key) that the registry to remove is under.
	 */
	void removeRegistry(Class<? extends Registry<?>> registryClass);

	/**
	 * Determines whether a registry has been stored under <code>registryClass</code>.
	 * @param registryClass The class (key) to search for a registry under.
	 * @return Whether a registry is stored under <code>registryClass</code>.
	 */
	boolean hasRegistry(Class<? extends Registry<?>> registryClass);

	/**
	 * Obtains the registry stored under <code>registryClass</code>.
	 * This method will never return null, meaning it may be necessary to call {@link #hasRegistry(Class)}
	 *  if you are not sure whether the registry you need exists.
	 * @param registryClass The class (key) that the registry is stored under.
	 * @return The registry stored under <code>registryClass</code>.
	 * @param <R> The type of registry.
	 */
	<R extends Registry<?>> R registry(Class<R> registryClass);

	/**
	 * Searches for a registry stored under <code>registryClass</code>.
	 * If the search fails, <code>putIfAbsent</code> will be used to get, store, and return a registry of the requested type.
	 * @param registryClass The class (key) to search for a registry under.
	 * @param putIfAbsent A supplier to use for creating an instance of the desired type of registry if one
	 *  is not already stored under <code>registryClass</code>.
	 * @return The registry stored under <code>registryClass</code> or created from <code>putIfAbsent</code>.
	 * @param <R> The type of registry.
	 */
	<R extends Registry<?>> R registry(Class<R> registryClass, Supplier<R> putIfAbsent);

	/**
	 * @return A syntax registry for this addon's syntax.
	 */
	SyntaxRegistry syntaxRegistry();

	/**
	 * @return A localizer for this addon's localizations.
	 */
	Localizer localizer();

	/**
	 * A helper method for loading addon modules.
	 * Modules will be loaded as described by {@link AddonModule}.
	 * @param modules The modules to load.
	 */
	default void loadModules(AddonModule... modules) {
		for (AddonModule module : modules) {
			module.init(this);
		}
		for (AddonModule module : modules) {
			module.load(this);
		}
	}

	/**
	 * Constructs an unmodifiable view of this addon.
	 * That is, the returned addon will return unmodifiable views of its {@link #syntaxRegistry()} and {@link #localizer()}.
	 * @return An unmodifiable view of this addon.
	 * @see SyntaxRegistry#unmodifiableView()
	 * @see Localizer#unmodifiableView()
	 */
	@Override
	@Contract("-> new")
	default SkriptAddon unmodifiableView() {
		return new SkriptAddonImpl.UnmodifiableAddon(this);
	}

}
