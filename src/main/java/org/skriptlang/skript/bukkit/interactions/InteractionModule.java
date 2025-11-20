package org.skriptlang.skript.bukkit.interactions;

import ch.njol.skript.Skript;

import java.io.IOException;

public class InteractionModule {

	public static void load() throws IOException {
		Skript.getAddonInstance().loadClasses("org.skriptlang.skript.bukkit.interactions", "elements");
	}
}
