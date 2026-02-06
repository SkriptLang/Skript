package org.skriptlang.skript.bukkit.input;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.EnumClassInfo;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.registrations.EventValues;
import org.bukkit.event.player.PlayerInputEvent;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.ChildAddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.input.elements.conditions.CondIsPressingKey;
import org.skriptlang.skript.bukkit.input.elements.events.EvtPlayerInput;
import org.skriptlang.skript.bukkit.input.elements.expressions.ExprCurrentInputKeys;

import java.util.List;

public class InputModule extends ChildAddonModule {

	public InputModule(AddonModule parentModule) {
		super(parentModule);
	}

	@Override
	public boolean canLoad(SkriptAddon addon) {
		return Skript.classExists("org.bukkit.Input");
	}

	@Override
	public void init(SkriptAddon addon) {
		Classes.registerClass(new EnumClassInfo<>(InputKey.class, "inputkey", "input keys")
			.user("input ?keys?")
			.name("Input Key")
			.description("Represents a movement input key that is pressed by a player.")
			.since("2.10")
			.requiredPlugins("Minecraft 1.21.3+"));
	}

	@Override
	public void load(SkriptAddon addon) {
		register(addon, List.of(
			CondIsPressingKey::register,
			EvtPlayerInput::register,
			ExprCurrentInputKeys::register
		));

		EventValues.registerEventValue(PlayerInputEvent.class, InputKey[].class,
			event -> InputKey.fromInput(event.getInput()).toArray(new InputKey[0]));
		EventValues.registerEventValue(PlayerInputEvent.class, InputKey[].class,
			event -> InputKey.fromInput(event.getPlayer().getCurrentInput()).toArray(new InputKey[0]),
			EventValues.TIME_PAST);
	}

	@Override
	public String name() {
		return "input key";
	}

}
