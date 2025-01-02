package org.skriptlang.skript.bukkit.spawner.elements.expressions.trialconfig.weightedloottable;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.loot.LootTable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.spawner.SpawnerModule;
import org.skriptlang.skript.bukkit.spawner.util.WeightedLootTable;

public class ExprLootTableOfWeightedLootTable extends SimplePropertyExpression<WeightedLootTable, LootTable> {

	static {
		register(SpawnerModule.SYNTAX_REGISTRY, ExprLootTableOfWeightedLootTable.class, LootTable.class,
			"loot[ ]table", "weightedloottables");
	}

	@Override
	public @NotNull LootTable convert(WeightedLootTable lootTable) {
		return lootTable.getLootTable();
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET)
			return CollectionUtils.array(LootTable.class);
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		assert delta != null;
		LootTable lootTable = (LootTable) delta[0];

		for (WeightedLootTable weightedLootTable : getExpr().getArray(event)) {
			weightedLootTable.setLootTable(lootTable);
		}
	}

	@Override
	public Class<? extends LootTable> getReturnType() {
		return LootTable.class;
	}

	@Override
	protected String getPropertyName() {
		return "weighted loot table";
	}

}
