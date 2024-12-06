package org.skriptlang.skript.bukkit.loottables.elements.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.skriptlang.skript.bukkit.loottables.LootTableUtils;

@Name("Has Loot Table")
@Description("Checks whether an entity or block has a loot table. The loot tables of chests will be deleted when the chest is opened or broken.")
@Examples("if block has a loot table:")
@Since("INSERT VERSION")
public class CondHasLootTable extends PropertyCondition<Object> {

	static {
		register(CondHasLootTable.class, PropertyType.HAVE, "[a] loot[ ]table", "blocks/entities");
	}

	@Override
	public boolean check(Object object) {
		if (LootTableUtils.isLootable(object))
			return LootTableUtils.getLootable(object).hasLootTable();
		return false;
	}

	@Override
	protected String getPropertyName() {
		return "loot table";
	}

}
