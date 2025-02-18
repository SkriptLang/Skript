package ch.njol.skript;

import org.jetbrains.annotations.Unmodifiable;
import org.skriptlang.skript.Skript;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.localization.Localizer;
import org.skriptlang.skript.registration.SyntaxRegistry;
import org.skriptlang.skript.util.Registry;

import java.util.Collection;
import java.util.function.Supplier;

/**
 * Bridge for interacting with the modern API classes from {@link org.skriptlang.skript}.
 */
final class ModernSkriptBridge {

	private ModernSkriptBridge() { }

	/**
	 * Similar to {@link Skript#unmodifiableView()}, but permits addon registration.
	 */
	public static final class SpecialUnmodifiableSkript implements Skript {

		private final Skript skript;
		private final Skript unmodifiableSkript;

		public SpecialUnmodifiableSkript(Skript skript) {
			this.skript = skript;
			this.unmodifiableSkript = skript.unmodifiableView();
		}

		@Override
		public SkriptAddon registerAddon(Class<?> source, String name) {
			return skript.registerAddon(source, name);
		}

		@Override
		public @Unmodifiable Collection<SkriptAddon> addons() {
			return unmodifiableSkript.addons();
		}

		@Override
		public Class<?> source() {
			return unmodifiableSkript.source();
		}

		@Override
		public String name() {
			return unmodifiableSkript.name();
		}

		@Override
		public <R extends Registry<?>> void storeRegistry(Class<R> registryClass, R registry) {
			unmodifiableSkript.storeRegistry(registryClass, registry);
		}

		@Override
		public void removeRegistry(Class<? extends Registry<?>> registryClass) {
			unmodifiableSkript.removeRegistry(registryClass);
		}

		@Override
		public boolean hasRegistry(Class<? extends Registry<?>> registryClass) {
			return unmodifiableSkript.hasRegistry(registryClass);
		}

		@Override
		public <R extends Registry<?>> R registry(Class<R> registryClass) {
			return unmodifiableSkript.registry(registryClass);
		}

		@Override
		public <R extends Registry<?>> R registry(Class<R> registryClass, Supplier<R> putIfAbsent) {
			return unmodifiableSkript.registry(registryClass, putIfAbsent);
		}

		@Override
		public SyntaxRegistry syntaxRegistry() {
			return unmodifiableSkript.syntaxRegistry();
		}

		@Override
		public Localizer localizer() {
			return unmodifiableSkript.localizer();
		}
	}

}
