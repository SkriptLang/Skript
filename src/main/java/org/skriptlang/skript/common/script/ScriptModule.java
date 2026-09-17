package org.skriptlang.skript.common.script;

import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.HierarchicalAddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValueRegistry;
import org.skriptlang.skript.common.script.elements.events.*;
import org.skriptlang.skript.common.script.elements.expressions.*;

public class ScriptModule extends HierarchicalAddonModule {

	public ScriptModule(AddonModule parentModule) {
		super(parentModule);
	}

	@Override
	protected void loadSelf(SkriptAddon addon) {
		EventValueRegistry eventValueRegistry = addon.registry(EventValueRegistry.class);
		register(addon,
			syntaxRegistry -> EvtScript.register(syntaxRegistry, eventValueRegistry),
			EvtScripts::register,
			ExprLoadingScripts::register,
			ExprUnloadingScripts::register
		);
	}

	@Override
	public String name() {
		return "script";
	}

}
