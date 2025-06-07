package org.skriptlang.skript.bukkit.itemcomponents;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.registrations.Classes;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.EquippableModule;

public class ItemComponentModule implements AddonModule {

	@Override
	public boolean canLoad(SkriptAddon addon) {
		return Skript.isRunningMinecraft(1, 20, 5);
	}

	@Override
	public void init(SkriptAddon addon) {
		Classes.registerClass(new ClassInfo<>(ComponentWrapper.class, "itemcomponent")
			.user("item ?components?")
			.name("Item Component")
			.description("Represents an item component for items. i.e. equippable components.")
			.since("INSERT VERSION")
			.requiredPlugins("Minecraft 1.20.5+")
		);
	}

	@Override
	public void load(SkriptAddon addon) {
		addon.loadModules(new EquippableModule());
	}

}
