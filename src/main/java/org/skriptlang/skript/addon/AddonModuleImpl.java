package org.skriptlang.skript.addon;

import org.skriptlang.skript.addon.AddonModule.ModuleOrigin;

import java.util.List;
import java.util.SequencedCollection;

class AddonModuleImpl {

	public record ModuleOriginImpl(SkriptAddon addon, SequencedCollection<AddonModule> modules) implements ModuleOrigin {

		/**
		 * Constructs a module origin from an addon and module chain.
		 * @param addon The addon providing the modules.
		 * @param modules The module chain, from most specific to root.
		 */
		public ModuleOriginImpl(SkriptAddon addon, AddonModule... modules) {
			this(addon.unmodifiableView(), List.of(modules));
		}

	}

}
