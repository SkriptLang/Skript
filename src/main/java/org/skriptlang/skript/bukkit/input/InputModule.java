package org.skriptlang.skript.bukkit.input;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.EnumClassInfo;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.registrations.EventValues;
import org.bukkit.event.player.PlayerInputEvent;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.input.elements.conditions.CondIsPressingKey;
import org.skriptlang.skript.bukkit.input.elements.events.EvtPlayerInput;
import org.skriptlang.skript.bukkit.input.elements.expressions.ExprCurrentInputKeys;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class InputModule implements AddonModule {

	@Override
	public void init(SkriptAddon addon) {
		if (Skript.classExists("org.bukkit.Input"))
			Classes.registerClass(new EnumClassInfo<>(InputKey.class, "inputkey", "input keys")
				.user("input ?keys?")
				.name("Input Key")
				.description("Represents a movement input key that is pressed by a player.")
				.since("2.10")
				.requiredPlugins("Minecraft 1.21.3+"));
	}

	@Override
	public void load(SkriptAddon addon) {
		if (Skript.classExists("org.bukkit.Input")) {
			SyntaxRegistry registry = addon.syntaxRegistry();

			CondIsPressingKey.register(registry);
			EvtPlayerInput.register(registry);
			ExprCurrentInputKeys.register(registry);

			EventValues.registerEventValue(PlayerInputEvent.class, InputKey[].class,
				event -> InputKey.fromInput(event.getInput()).toArray(new InputKey[0]));
			EventValues.registerEventValue(PlayerInputEvent.class, InputKey[].class,
				event -> InputKey.fromInput(event.getPlayer().getCurrentInput()).toArray(new InputKey[0]),
				EventValues.TIME_PAST);
		}
	}

}
