package org.skriptlang.skript.addon;

import org.skriptlang.skript.addon.AddonModule.ModuleOrigin;

class AddonModuleImpl {

	public record ModuleOriginImpl(SkriptAddon addon, String moduleName) implements ModuleOrigin {

		public ModuleOriginImpl(SkriptAddon addon, String moduleName) {
			this.addon = addon.unmodifiableView();
			this.moduleName = moduleName;
		}

	}

}
