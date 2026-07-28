package org.skriptlang.skript.bukkit.misc;

import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.HierarchicalAddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValueRegistry;
import org.skriptlang.skript.bukkit.misc.elements.effects.EffRotate;
import org.skriptlang.skript.bukkit.misc.elements.events.*;
import org.skriptlang.skript.bukkit.misc.elements.expressions.*;

public class MiscModule extends HierarchicalAddonModule {

	public MiscModule(AddonModule parentModule) {
		super(parentModule);
	}

	@Override
	protected void loadSelf(SkriptAddon addon) {
		EventValueRegistry eventValueRegistry = addon.registry(EventValueRegistry.class);

		MiscEvents.register(moduleRegistry(addon));

		register(addon,
			EvtAtTime::register,
			EvtRealTime::register,
			syntaxRegistry -> EvtPeriodical.register(syntaxRegistry, eventValueRegistry),
			syntaxRegistry -> EvtScript.register(syntaxRegistry, eventValueRegistry),
			syntaxRegistry -> EvtSkript.register(syntaxRegistry, eventValueRegistry),
			EffRotate::register,
			ExprBroadcastMessage::register,
			ExprColorOf::register,
			ExprItemOfEntity::register,
			ExprMOTD::register,
			ExprQuaternionAxisAngle::register,
			ExprRotate::register,
			ExprSkullTexture::register,
			ExprTextOf::register,
			ExprWithYawPitch::register
		);
	}

	@Override
	public String name() {
		return "misc";
	}

}
