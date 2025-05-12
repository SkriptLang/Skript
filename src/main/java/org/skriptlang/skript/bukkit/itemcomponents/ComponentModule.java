package org.skriptlang.skript.bukkit.itemcomponents;

import org.skriptlang.skript.bukkit.itemcomponents.equippable.EquippableModule;

import java.io.IOException;

public class ComponentModule {

	public static void load() throws IOException {
		EquippableModule.load();
	}

}
