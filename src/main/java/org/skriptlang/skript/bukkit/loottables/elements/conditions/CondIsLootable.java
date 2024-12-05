package org.skriptlang.skript.bukkit.loottables.elements.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.block.Block;
import org.bukkit.loot.Lootable;
import org.skriptlang.skript.bukkit.loottables.LootTableUtils;

@Name("Is Lootable")
@Description("Checks whether an entity or block is lootable. Lootables are entities or blocks that can have a loot table.")
@Examples("if event-entity is lootable:")
@Since("INSERT VERSION")
public class CondIsLootable extends PropertyCondition<Object> {

	static {
		PropertyCondition.register(CondIsLootable.class, "lootable", "blocks/entities");
	}

	@Override
	public boolean check(Object object) {
		return LootTableUtils.isLootable(object);
	}

	@Override
	protected String getPropertyName() {
		return "lootable";
	}

}
