package org.skriptlang.skript.bukkit.itemcomponents.equippable;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.ItemSource;
import ch.njol.skript.util.slot.Slot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.components.EquippableComponent;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.lang.converter.Converter;
import org.skriptlang.skript.lang.converter.Converters;

import java.io.IOException;

public class EquippableModule implements AddonModule {

	@Override
	public boolean canLoad(SkriptAddon addon) {
		return Skript.classExists("org.bukkit.inventory.meta.components.EquippableComponent");
	}

	@Override
	public void init(SkriptAddon addon) {
		Classes.registerClass(new ClassInfo<>(EquippableWrapper.class, "equippablecomponent")
			.user("equippable ?components?")
			.name("Equippable Components")
			.description("Represents an equippable component used for items.")
			.requiredPlugins("Minecraft 1.21.2+")
			.since("INSERT VERSION")
			.defaultExpression(new EventValueExpression<>(EquippableWrapper.class))
			.cloner(EquippableWrapper::clone)
		);

		Converters.registerConverter(EquippableComponent.class, EquippableWrapper.class, EquippableWrapper::new, Converter.NO_RIGHT_CHAINING);
		Converters.registerConverter(ItemStack.class, EquippableWrapper.class, EquippableWrapper::new, Converter.NO_RIGHT_CHAINING);
		Converters.registerConverter(ItemType.class, EquippableWrapper.class, itemType -> new EquippableWrapper(new ItemSource(itemType)), Converter.NO_RIGHT_CHAINING);
		Converters.registerConverter(Slot.class, EquippableWrapper.class, slot -> new EquippableWrapper(new ItemSource(slot)), Converter.NO_RIGHT_CHAINING);
	}

	@Override
	public void load(SkriptAddon addon) {
        try {
            Skript.getAddonInstance().loadClasses("org.skriptlang.skript.bukkit.itemcomponents.equippable", "elements");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
