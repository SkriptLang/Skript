package org.skriptlang.skript.bukkit.block.blockdata;

import ch.njol.skript.registrations.Classes;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.HierarchicalAddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.block.blockdata.elements.CondBlockDataTag;
import org.skriptlang.skript.bukkit.block.blockdata.elements.ExprBlockData;
import org.skriptlang.skript.bukkit.block.blockdata.elements.ExprBlockDataTags;
import org.skriptlang.skript.bukkit.block.blockdata.elements.ExprBlockDataValues;

public class BlockDataModule extends HierarchicalAddonModule {

	public BlockDataModule(AddonModule parent) {
		super(parent);
	}

	@Override
	protected void initSelf(SkriptAddon addon) {
		Classes.registerClass(new BlockDataClassInfo());
	}

	@Override
	protected void loadSelf(SkriptAddon addon) {
		register(addon,
			CondBlockDataTag::register,
			ExprBlockData::register,
			ExprBlockDataTags::register,
			ExprBlockDataValues::register
		);
	}

	@Override
	public String name() {
		return "blockdata";
	}

}
