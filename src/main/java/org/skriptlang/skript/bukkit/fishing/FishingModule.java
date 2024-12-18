package org.skriptlang.skript.bukkit.fishing;

import ch.njol.skript.Skript;

import java.io.IOException;

public class FishingModule {

	public static void load() throws IOException {
		Skript.getAddonInstance().loadClasses("org.skriptlang.skript.bukkit.fishing", "elements");
	}

}
