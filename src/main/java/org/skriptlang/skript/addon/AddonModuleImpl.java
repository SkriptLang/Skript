package org.skriptlang.skript.addon;

import org.skriptlang.skript.addon.AddonModule.ModuleOrigin;

class AddonModuleImpl {

	public record ModuleOriginImpl(SkriptAddon addon, String moduleName) implements ModuleOrigin { }

}
