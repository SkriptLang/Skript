package org.skriptlang.skript.addon;

import java.util.ArrayList;
import java.util.List;

/**
 * A child module is intended to be created by another {@link AddonModule} as a way to further organize elements.
 * It behaves the same way as a normal module, but allows accessing the parent module if needed.
 */
public abstract class ChildAddonModule implements AddonModule {

	private final AddonModule parentModule;

	/**
	 * Constructs an origin from an addon and module name.
	 * @param addon The addon providing the module.
	 * @param moduleName The name of the providing module.
	 * @return An origin from the provided information.
	 */
	static ModuleOrigin origin(SkriptAddon addon, AddonModule parentModule, String moduleName) {
		return new ChildModuleOriginImpl(addon, parentModule, moduleName);
	}

	/**
	 * An origin to be used for something provided by a child module of an addon.
	 */
	static final class ChildModuleOriginImpl implements ModuleOrigin {

		SkriptAddon addon;
		AddonModule parentModule;
		String[] moduleNames;

		/**
		 * Constructs a child module origin.
		 * @param addon The addon providing the module. Ideally, this is the same as the parent module's addon.
		 * @param parentModule The parent module that created this child module.
		 * @param moduleName The name of the providing module.
		 */
		public ChildModuleOriginImpl(SkriptAddon addon, AddonModule parentModule, String moduleName) {
			this.addon = addon;
			this.parentModule = parentModule;

			List<String> names = new ArrayList<>(List.of(moduleName));
			names.addAll(List.of(parentModule.origin(addon).moduleNames()));
			moduleNames = names.toArray(new String[0]);
		}

		@Override
		public String[] moduleNames() {
			return moduleNames;
		}

		@Override
		public SkriptAddon addon() {
			return addon;
		}

	}

	/**
	 * Constructs a child addon module with the given parent module.
	 * @param parentModule The parent module that created this child module.
	 */
	protected ChildAddonModule(AddonModule parentModule) {
		this.parentModule = parentModule;
	}

	@Override
	public ModuleOrigin origin(SkriptAddon addon) {
		return ChildAddonModule.origin(addon, parentModule, name());
	}
}
