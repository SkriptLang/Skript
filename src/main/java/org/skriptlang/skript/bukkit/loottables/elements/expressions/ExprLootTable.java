package org.skriptlang.skript.bukkit.loottables.elements.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.loot.LootTable;
import org.bukkit.loot.Lootable;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.loottables.LootTableUtils;

@Name("Loot Table")
@Description({
	"Returns the loot table of an entity or block.",
	"Setting the loot table of a block will update the block state, and once opened will "
		+ "generate loot of the specified loot table. Please note that doing so may cause "
		+ "warnings in the console due to over-filling the chest.",
	"Please note that resetting/deleting the loot table of an ENTITY will reset the entity's loot table to its default.",
})
@Examples({
	"set loot table of event-entity to \"minecraft:entities/ghast\"",
	"# this will set the loot table of the entity to a ghast's loot table, thus dropping ghast tears and gunpowder",
	"",
	"set loot table of event-block to \"minecraft:chests/simple_dungeon\"",
})
@Since("INSERT VERSION")
public class ExprLootTable extends SimplePropertyExpression<Object, LootTable> {

	static {
		register(ExprLootTable.class, LootTable.class, "loot[ ]table[s]", "entities/blocks");
	}

	@Override
	public @Nullable LootTable convert(Object object) {
		if (LootTableUtils.isLootable(object))
			return LootTableUtils.getLootTable(object);
		return null;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, DELETE, RESET -> CollectionUtils.array(LootTable.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		LootTable lootTable = delta != null ? ((LootTable) delta[0]) : null;

		for (Object object : getExpr().getArray(event)) {
			if (!LootTableUtils.isLootable(object))
				continue;

			Lootable lootable = LootTableUtils.getAsLootable(object);

			lootable.setLootTable(lootTable);
			LootTableUtils.updateState(lootable);
		}
	}

	@Override
	public Class<? extends LootTable> getReturnType() {
		return LootTable.class;
	}

	@Override
	protected String getPropertyName() {
		return "loot table";
	}

}
