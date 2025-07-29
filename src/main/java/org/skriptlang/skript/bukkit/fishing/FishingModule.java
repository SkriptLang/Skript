package org.skriptlang.skript.bukkit.fishing;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.EnumClassInfo;
import ch.njol.skript.registrations.Classes;
import org.bukkit.event.player.PlayerFishEvent;

import java.io.IOException;

public class FishingModule {

	public static void load() throws IOException {
		// Register the Fishing State enum as a Skript type
		Classes.registerClass(new EnumClassInfo<>(PlayerFishEvent.State.class, "fishingstate", "fishing states")
			.user("fishing ?states?")
			.name("Fishing State")
			.description("Represents the different states of a fishing event.")
			.since("2.11")
		);

		Skript.getAddonInstance().loadClasses("org.skriptlang.skript.bukkit.fishing", "elements");
	}
}
