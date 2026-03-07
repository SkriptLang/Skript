package org.skriptlang.skript.addon;

import org.jetbrains.annotations.Unmodifiable;
import org.skriptlang.skript.addon.AddonModule.ModuleOrigin;

import java.util.List;
import java.util.SequencedCollection;

class AddonModuleImpl {

	public static final class ModuleOriginImpl implements ModuleOrigin {

		private final SkriptAddon addon;
		private final SequencedCollection<AddonModule> modules;

		/**
		 * Constructs a module origin from an addon and module chain.
		 * @param addon The addon providing the modules.
		 * @param modules The module chain, from most specific to root.
		 */
		public ModuleOriginImpl(SkriptAddon addon, AddonModule... modules) {
			this.addon = addon.unmodifiableView();
			this.modules = List.of(modules);
		}

		@Override
		public SkriptAddon addon() {
			return addon;
		}

		@Override
		public @Unmodifiable SequencedCollection<AddonModule> modules() {
			return modules;
		}

	}

}
