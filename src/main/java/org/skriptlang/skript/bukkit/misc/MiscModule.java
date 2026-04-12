package org.skriptlang.skript.bukkit.misc;

import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.HierarchicalAddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.misc.elements.effects.EffRotate;
import org.skriptlang.skript.bukkit.misc.elements.expressions.*;

public class MiscModule extends HierarchicalAddonModule {

	public MiscModule(AddonModule parentModule) {
		super(parentModule);
	}

	@Override
	protected void loadSelf(SkriptAddon addon) {
		register(addon,
			EffRotate::register,
			ExprBroadcastMessage::register,
			ExprItemOfEntity::register,
			ExprMOTD::register,
			ExprQuaternionAxisAngle::register,
			ExprRotate::register,
			ExprTextOf::register,
			ExprWithYawPitch::register
		);
	}

	@Override
	public String name() {
		return "misc";
	}

}
