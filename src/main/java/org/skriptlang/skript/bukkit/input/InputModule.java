package org.skriptlang.skript.bukkit.input;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.EnumClassInfo;
import ch.njol.skript.registrations.Classes;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.HierarchicalAddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.input.elements.conditions.CondIsPressingKey;
import org.skriptlang.skript.bukkit.input.elements.events.EvtPlayerInput;
import org.skriptlang.skript.bukkit.input.elements.expressions.ExprCurrentInputKeys;

public class InputModule extends HierarchicalAddonModule {

	public InputModule(AddonModule parentModule) {
		super(parentModule);
	}

	@Override
	protected boolean canLoadSelf(SkriptAddon addon) {
		return Skript.classExists("org.bukkit.Input");
	}

	@Override
	protected void initSelf(SkriptAddon addon) {
		Classes.registerClass(new EnumClassInfo<>(InputKey.class, "inputkey", "input keys")
			.user("input ?keys?")
			.name("Input Key")
			.description("Represents a movement input key that is pressed by a player.")
			.since("2.10")
			.requiredPlugins("Minecraft 1.21.3+"));
	}

	@Override
	protected void loadSelf(SkriptAddon addon) {
		register(addon,
			CondIsPressingKey::register,
			EvtPlayerInput::register,
			ExprCurrentInputKeys::register
		);
	}

	@Override
	public String name() {
		return "input key";
	}

}
