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

	protected ChildAddonModule(AddonModule parentModule) {
		this.parentModule = parentModule;
	}

	@Override
	public void init(SkriptAddon addon) {
		init(addon, parentModule);
	}

	/**
	 * Used for loading the components of this module that are needed first or by other modules (e.g. class infos).
	 * <b>This method will always be called before {@link #load(SkriptAddon, AddonModule)}</b>.
	 * @param addon The addon this module belongs to.
	 * @param parentModule The parent module that created this child module.
	 * @see #load(SkriptAddon)
	 */
	protected abstract void init(SkriptAddon addon, AddonModule parentModule);

	@Override
	public void load(SkriptAddon addon) {
		load(addon, parentModule);
	}

	/**
	 * Used for loading the components (e.g. syntax) of this module.
	 * @param addon The addon this module belongs to.
	 * @param parentModule The parent module that created this child module.
	 * @see #init(SkriptAddon, AddonModule)
	 */
	protected abstract void load(SkriptAddon addon, AddonModule parentModule);

	@Override
	public boolean canLoad(SkriptAddon addon) {
		return canLoad(addon, parentModule);
	}

	/**
	 * Allow addons to specify whether they can load or not.
	 * Called prior to {@link #init(SkriptAddon)}
	 *
	 * @param addon The addon this module belongs to.
	 * @param parentModule The parent module that created this child module.
	 * @return Whether this module can load.
	 */
	protected boolean canLoad(SkriptAddon addon, AddonModule parentModule) {
		return AddonModule.super.canLoad(addon);
	}

	@Override
	public ModuleOrigin origin(SkriptAddon addon) {
		return ChildAddonModule.origin(addon, parentModule, name());
	}
}
