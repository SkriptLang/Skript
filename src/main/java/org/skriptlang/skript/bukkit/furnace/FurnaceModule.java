package org.skriptlang.skript.bukkit.furnace;

import ch.njol.skript.Skript;
import java.io.IOException;

public class FurnaceModule {

	public static void load() throws IOException{
		Skript.getAddonInstance().loadClasses("org.skriptlang.skript.bukkit.furnace", "elements");
	}

}
