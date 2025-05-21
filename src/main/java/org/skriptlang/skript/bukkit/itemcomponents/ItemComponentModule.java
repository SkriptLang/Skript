package org.skriptlang.skript.bukkit.itemcomponents;

import ch.njol.skript.Skript;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.EquippableModule;

import java.io.IOException;

public class ItemComponentModule {

	public static void load() throws IOException {
		if(!Skript.isRunningMinecraft(1, 20, 5))
			return;
		EquippableModule.load();
	}

}
