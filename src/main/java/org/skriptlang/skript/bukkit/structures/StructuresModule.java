package org.skriptlang.skript.bukkit.structures;

import java.io.IOException;

import ch.njol.skript.Skript;

public class StructuresModule {

	public static void load() throws IOException {
		Skript.getAddonInstance().loadClasses("org.skriptlang.skript.bukkit.structures", "elements");
	}

}
