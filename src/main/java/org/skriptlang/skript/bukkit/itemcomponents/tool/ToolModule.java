package org.skriptlang.skript.bukkit.itemcomponents.tool;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.ItemSource;
import ch.njol.skript.util.slot.Slot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.components.ToolComponent;
import org.bukkit.inventory.meta.components.ToolComponent.ToolRule;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.lang.converter.Converter;
import org.skriptlang.skript.lang.converter.Converters;

import java.io.IOException;

public class ToolModule implements AddonModule {

	@Override
	public boolean canLoad(SkriptAddon addon) {
		return Skript.classExists("org.bukkit.inventory.meta.components.ToolComponent");
	}

	@Override
	public void init(SkriptAddon addon) {
		Classes.registerClass(new ClassInfo<>(ToolWrapper.class, "toolcomponent")
			.user("tool ?components?")
			.name("Tool Component")
			.description("Represents a tool component used for items.")
			.requiredPlugins("Minecraft 1.20.6+")
			.since("INSERT VERSION")
			.defaultExpression(new EventValueExpression<>(ToolWrapper.class))
			.cloner(ToolWrapper::clone)
		);

		Classes.registerClass(new ClassInfo<>(ToolRule.class, "toolrule")
			.user("tool ?rules?")
			.name("Tool Rule")
			.description("""
				Represents a rule that can be applied to a tool component.
				A tool rule consists of:
					- Block types that the rule should be applied to
					- Mining speed for the blocks
					- Whether the blocks should drop their respective items
				""")
			.requiredPlugins("Minecraft 1.20.6+")
			.since("INSERT VERSION")
			.defaultExpression(new EventValueExpression<>(ToolRule.class))
		);

		Converters.registerConverter(ToolComponent.class, ToolWrapper.class, ToolWrapper::new, Converter.NO_RIGHT_CHAINING);
		Converters.registerConverter(ItemStack.class, ToolWrapper.class, ToolWrapper::new, Converter.NO_RIGHT_CHAINING);
		Converters.registerConverter(ItemType.class, ToolWrapper.class, itemType -> new ToolWrapper(new ItemSource(itemType)), Converter.NO_RIGHT_CHAINING);
		Converters.registerConverter(Slot.class, ToolWrapper.class, slot -> new ToolWrapper(new ItemSource(slot)), Converter.NO_RIGHT_CHAINING);
	}

	@Override
	public void load(SkriptAddon addon) {
		try {
			Skript.getAddonInstance().loadClasses("org.skriptlang.skript.bukkit.itemcomponents.tool", "elements");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
