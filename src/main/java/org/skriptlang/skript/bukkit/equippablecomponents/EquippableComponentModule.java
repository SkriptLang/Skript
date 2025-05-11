package org.skriptlang.skript.bukkit.equippablecomponents;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.ItemSource;
import ch.njol.skript.util.slot.Slot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.components.EquippableComponent;
import org.skriptlang.skript.lang.converter.Converters;

import java.io.IOException;

public class EquippableComponentModule {

	public static void load() throws IOException {
		if (!Skript.classExists("org.bukkit.inventory.meta.components.EquippableComponent"))
			return;

		Skript.getAddonInstance().loadClasses("org.skriptlang.skript.bukkit.equippablecomponents", "elements");

		Classes.registerClass(new ClassInfo<>(EquippableWrapper.class, "equippablecomponent")
			.user("equippable ?components?")
			.name("Equippable Components")
			.description("Represents an equippable component used for items.")
			.requiredPlugins("Minecraft 1.21.2+")
			.since("INSERT VERSION")
		);

		Converters.registerConverter(EquippableComponent.class, EquippableWrapper.class, EquippableWrapper::new);
		Converters.registerConverter(ItemStack.class, EquippableWrapper.class, EquippableWrapper::new);
		Converters.registerConverter(ItemType.class, EquippableWrapper.class, itemType -> new EquippableWrapper(new ItemSource(itemType)));
		Converters.registerConverter(Slot.class, EquippableWrapper.class, slot -> new EquippableWrapper(new ItemSource(slot)));
	}

}
