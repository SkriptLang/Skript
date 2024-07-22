package org.skriptlang.skript.addon;

import org.skriptlang.skript.localization.Localizer;
import org.skriptlang.skript.registration.SyntaxRegistry;
import org.skriptlang.skript.util.Registry;
import org.skriptlang.skript.util.ViewProvider;

import java.util.function.Supplier;

class SkriptAddonImpl {

	static class UnmodifiableAddon implements SkriptAddon {

		private final SkriptAddon addon;
		private final Localizer unmodifiableLocalizer;

		UnmodifiableAddon(SkriptAddon addon) {
			this.addon = addon;
			this.unmodifiableLocalizer = addon.localizer().unmodifiableView();
		}

		@Override
		public Class<?> source() {
			return addon.source();
		}

		@Override
		public String name() {
			return addon.name();
		}

		@Override
		public <R extends Registry<?>> void storeRegistry(Class<R> registryClass, R registry) {
			throw new UnsupportedOperationException("Cannot store registries on an unmodifiable addon");
		}

		@Override
		public void removeRegistry(Class<? extends Registry<?>> registryClass) {
			throw new UnsupportedOperationException("Cannot remove registries from an unmodifiable addon");
		}

		@Override
		public boolean hasRegistry(Class<? extends Registry<?>> registryClass) {
			return addon.hasRegistry(registryClass);
		}

		@Override
		public <R extends Registry<?>> R registry(Class<R> registryClass) {
			R registry = addon.registry(registryClass);
			if (registry instanceof ViewProvider) {
				//noinspection unchecked
				registry = ((ViewProvider<R>) registry).unmodifiableView();
			}
			return registry;
		}

		@Override
		public <R extends Registry<?>> R registry(Class<R> registryClass, Supplier<R> putIfAbsent) {
			throw new UnsupportedOperationException("Cannot store registries on an unmodifiable addon");
		}

		@Override
		public SyntaxRegistry syntaxRegistry() {
			return addon.syntaxRegistry().unmodifiableView();
		}

		@Override
		public Localizer localizer() {
			return unmodifiableLocalizer;
		}

		@Override
		public void loadModules(AddonModule... modules) {
			throw new UnsupportedOperationException("Cannot load modules using an unmodifiable addon");
		}

	}

}
