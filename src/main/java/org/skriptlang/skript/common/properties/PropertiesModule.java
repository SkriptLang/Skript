package org.skriptlang.skript.common.properties;

import ch.njol.skript.SkriptConfig;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.HierarchicalAddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.common.properties.elements.conditions.*;
import org.skriptlang.skript.common.properties.elements.expressions.*;

public class PropertiesModule extends HierarchicalAddonModule {

	public PropertiesModule(AddonModule parentModule) {
		super(parentModule);
	}

	@Override
	protected void loadSelf(SkriptAddon addon) {
		register(addon, PropExprScale::register);
		if (SkriptConfig.useTypeProperties.value()) { // not using canLoad since this should only gate old properties, not new ones
			register(addon,
				PropCondContains::register,
				PropCondIsEmpty::register,
				PropExprAmount::register,
				PropExprCustomName::register,
				PropExprName::register,
				PropExprNumber::register,
				PropExprSize::register,
				PropExprValueOf::register,
				PropExprWXYZ::register
			);
		}
	}

	@Override
	public String name() {
		return "type properties";
	}

}
