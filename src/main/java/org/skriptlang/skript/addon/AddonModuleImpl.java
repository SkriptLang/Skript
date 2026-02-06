package org.skriptlang.skript.addon;

import org.skriptlang.skript.addon.AddonModule.ModuleOrigin;

class AddonModuleImpl {

	public record ModuleOriginImpl(SkriptAddon addon, String... moduleNames) implements ModuleOrigin {

		public ModuleOriginImpl(SkriptAddon addon, String... moduleNames) {
			this.addon = addon.unmodifiableView();
			this.moduleNames = moduleNames;
		}

	}

}
