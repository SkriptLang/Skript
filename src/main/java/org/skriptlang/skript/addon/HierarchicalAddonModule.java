package org.skriptlang.skript.addon;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * An {@link AddonModule} that supports parent/child hierarchies with automatic lifecycle forwarding.
 * <p>
 * Modules extending this class should:
 * <ul>
 *     <li>Override {@link #children()} to declare child modules</li>
 *     <li>Override {@link #canLoadSelf(SkriptAddon)}, {@link #initSelf(SkriptAddon)}, and/or {@link #loadSelf(SkriptAddon)} for module-specific loading</li>
 * </ul>
 * <p>
 * Lifecycle methods ({@link #canLoad}, {@link #init}, {@link #load}) are automatically forwarded to children.
 * Origin chains are built automatically from the parent hierarchy.
 */
public abstract class HierarchicalAddonModule implements AddonModule {

	private final @Nullable AddonModule parentModule;
	private final List<AddonModule> loadableChildren = new ArrayList<>();

	/**
	 * Constructs a module with no parent.
	 */
	protected HierarchicalAddonModule() {
		this.parentModule = null;
	}

	/**
	 * Constructs a child module with the given parent.
	 * @param parentModule The parent module that created this child module.
	 */
	protected HierarchicalAddonModule(@Nullable AddonModule parentModule) {
		this.parentModule = parentModule;
	}

	/**
	 * Returns the parent module, or null if this is a root module.
	 * @return The parent module, or null.
	 */
	public @Nullable AddonModule parent() {
		return parentModule;
	}

	/**
	 * Override to provide child modules. Default returns an empty list.
	 * @return An iterable of child modules.
	 */
	public Iterable<AddonModule> children() {
		return List.of();
	}

	/**
	 * Builds the module reference chain from this module up to root.
	 * @return List of modules from most specific (this) to root parent.
	 */
	private List<AddonModule> moduleChain() {
		List<AddonModule> chain = new ArrayList<>();
		AddonModule current = this;
		while (current != null) {
			chain.add(current);
			if (current instanceof HierarchicalAddonModule hierarchical) {
				current = hierarchical.parent();
			} else {
				break;
			}
		}
		return chain;
	}

	/**
	 * Override for module-specific canLoad logic. Default returns true.
	 * @param addon The addon this module belongs to.
	 * @return Whether this module can load.
	 */
	protected boolean canLoadSelf(SkriptAddon addon) {
		return true;
	}

	@Override
	public final boolean canLoad(SkriptAddon addon) {
		if (!canLoadSelf(addon)) {
			return false;
		}

		// Filter children that can load
		loadableChildren.clear();
		for (AddonModule child : children()) {
			if (child.canLoad(addon)) {
				loadableChildren.add(child);
			}
		}
		return true;
	}

	/**
	 * Override for module-specific initialization.
	 * @param addon The addon this module belongs to.
	 */
	protected void initSelf(SkriptAddon addon) {
	}

	@Override
	public final void init(SkriptAddon addon) {
		initSelf(addon);
		for (AddonModule child : loadableChildren) {
			child.init(addon);
		}
	}

	/**
	 * Override for module-specific loading.
	 * @param addon The addon this module belongs to.
	 */
	protected abstract void loadSelf(SkriptAddon addon);

	@Override
	public final void load(SkriptAddon addon) {
		loadSelf(addon);
		for (AddonModule child : loadableChildren) {
			child.load(addon);
		}
	}

	@Override
	public final ModuleOrigin origin(SkriptAddon addon) {
		AddonModule[] modules = moduleChain().toArray(new AddonModule[0]);
		return AddonModule.origin(addon, modules);
	}

}
