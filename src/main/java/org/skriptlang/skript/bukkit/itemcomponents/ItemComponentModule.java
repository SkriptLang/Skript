package org.skriptlang.skript.bukkit.itemcomponents;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.HierarchicalAddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.EquippableModule;
import org.skriptlang.skript.bukkit.itemcomponents.generic.elements.expressions.ExprItemCompCopy;

import java.util.List;

public class ItemComponentModule extends HierarchicalAddonModule {

	public ItemComponentModule(AddonModule parentModule) {
		super(parentModule);
	}

	@Override
	protected boolean canLoadSelf(SkriptAddon addon) {
		return Skript.classExists("io.papermc.paper.datacomponent.BuildableDataComponent");
	}

	@Override
	public Iterable<AddonModule> children() {
		return List.of(new EquippableModule(this));
	}

	@Override
	protected void initSelf(SkriptAddon addon) {
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
	protected void loadSelf(SkriptAddon addon) {
		register(addon, ExprItemCompCopy::register);
	}

	@Override
	public String name() {
		return "item component";
	}

}
