package org.skriptlang.skript.bukkit.input;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.EnumClassInfo;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.util.Getter;
import org.bukkit.event.player.PlayerInputEvent;

import java.io.IOException;

public class InputModule {

	public static void load() throws IOException {
		if (!Skript.classExists("org.bukkit.Input"))
			return;

		Skript.getAddonInstance().loadClasses("org.skriptlang.skript.bukkit.input.elements");

		Classes.registerClass(new EnumClassInfo<>(InputKey.class, "inputkey", "input keys")
			.user("input ?keys?")
			.name("Input Key")
			.description("Represents a movement input key that is pressed by a player.")
			.since("INSERT VERSION")
			.requiredPlugins("Minecraft 1.21.3+"));

		EventValues.registerEventValue(PlayerInputEvent.class, InputKey[].class, new Getter<>() {
			@Override
			public InputKey[] get(PlayerInputEvent event) {
				return InputKey.fromInput(event.getInput()).toArray(new InputKey[0]);
			}
		}, EventValues.TIME_NOW);

		EventValues.registerEventValue(PlayerInputEvent.class, InputKey[].class, new Getter<>() {
			@Override
			public InputKey[] get(PlayerInputEvent event) {
				return InputKey.fromInput(event.getPlayer().getCurrentInput()).toArray(new InputKey[0]);
			}
		}, EventValues.TIME_PAST);
	}

}
