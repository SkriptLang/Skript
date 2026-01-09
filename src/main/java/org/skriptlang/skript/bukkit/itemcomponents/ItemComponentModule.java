package org.skriptlang.skript.bukkit.itemcomponents;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.EquippableModule;
import org.skriptlang.skript.bukkit.itemcomponents.generic.ExprItemCompCopy;

public class ItemComponentModule implements AddonModule {

	@Override
	public boolean canLoad(SkriptAddon addon) {
		return Skript.classExists("io.papermc.paper.datacomponent.BuildableDataComponent");
	}

	@Override
	public void init(SkriptAddon addon) {
		Classes.registerClass(new ClassInfo<>(ComponentWrapper.class, "itemcomponent")
			.user("item ?components?")
			.name("Item Component")
			.description("Represents an item component for items. i.e. equippable components.")
			.since("2.13")
			.requiredPlugins("Minecraft 1.21.2+")
			.parser(new Parser<>() {
				@Override
				public boolean canParse(ParseContext context) {
					return false;
				}

				@Override
				public String toString(ComponentWrapper wrapper, int flags) {
					return "item component";
				}

				@Override
				public String toVariableNameString(ComponentWrapper wrapper) {
					return "item component#" + wrapper.hashCode();
				}
			})
			.after("itemstack", "itemtype", "slot")
		);
	}

	@Override
	public void load(SkriptAddon addon) {
		addon.loadModules(new EquippableModule());

		ExprItemCompCopy.register(addon.syntaxRegistry());
	}

	@Override
	public String name() {
		return "item component";
	}

}
