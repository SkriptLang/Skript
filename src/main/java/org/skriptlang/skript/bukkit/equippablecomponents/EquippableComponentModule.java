package org.skriptlang.skript.bukkit.equippablecomponents;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.EnumClassInfo;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.slot.EquipmentSlot.EquipSlot;
import org.bukkit.inventory.meta.components.EquippableComponent;
import org.skriptlang.skript.lang.comparator.Comparator;
import org.skriptlang.skript.lang.comparator.Comparators;
import org.skriptlang.skript.lang.comparator.Relation;

import java.io.IOException;

public class EquippableComponentModule {

	public static void load() throws IOException {
		if (!Skript.classExists("org.bukkit.inventory.meta.components.EquippableComponent"))
			return;

		Skript.getAddonInstance().loadClasses("org.skriptlang.skript.bukkit.equippablecomponents", "elements");

		Classes.registerClass(new EnumClassInfo<>(EquipSlot.class, "equipmentslot", "equipment slot")
			.user("equipment ?slots?")
			.name("Equipment Slot")
			.description("Represents an equipment slot")
			.since("INSERT VERSION")
		);

		Classes.registerClass(new ClassInfo<>(EquippableComponent.class, "equippablecomponent")
			.user("equippable ?components?")
			.name("Equippable Components")
			.description("Represents an equippable component used for items.")
			.requiredPlugins("Minecraft 1.21.2+")
			.since("INSERT VERSION")
		);

		Comparators.registerComparator(EquipSlot.class, EquipSlot.class, new Comparator<EquipSlot, EquipSlot>() {
			@Override
			public Relation compare(EquipSlot slot1, EquipSlot slot2) {
				return Relation.get(slot2.equals(slot1));
			}
		});

	}

}
