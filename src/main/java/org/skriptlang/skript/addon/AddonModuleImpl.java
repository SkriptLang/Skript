package org.skriptlang.skript.addon;

import org.skriptlang.skript.addon.AddonModule.ModuleOrigin;

import java.util.List;
import java.util.SequencedCollection;

class AddonModuleImpl {

	public record ModuleOriginImpl(SkriptAddon addon, Class<?> originClass, SequencedCollection<AddonModule> modules) implements ModuleOrigin {

		/**
		 * Constructs a module origin from an addon and module chain.
		 * @param addon The addon providing the modules.
		 * @param originClass The class of the thing this origin describes.
		 * @param modules The module chain, from most specific to root.
		 */
		public ModuleOriginImpl(SkriptAddon addon, Class<?> originClass, AddonModule... modules) {
			this(addon.unmodifiableView(), originClass, List.of(modules));
		}

	}

}
