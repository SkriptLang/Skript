package org.skriptlang.skript.bukkit.spawner.elements.expressions.trialconfig;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.loot.LootTable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.spawner.SpawnerModule;
import org.skriptlang.skript.bukkit.spawner.util.TrialSpawnerReward;

public class ExprRewardLootTable extends SimplePropertyExpression<TrialSpawnerReward, LootTable> {

	static {
		register(SpawnerModule.SYNTAX_REGISTRY, ExprRewardLootTable.class, LootTable.class,
			"[trial] spawner reward loot[ ]table", "trialspawnerrewards");
	}

	@Override
	public @NotNull LootTable convert(TrialSpawnerReward reward) {
		return reward.getLootTable();
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

		for (TrialSpawnerReward reward : getExpr().getArray(event)) {
			reward.setLootTable(lootTable);
		}
	}

	@Override
	public Class<? extends LootTable> getReturnType() {
		return LootTable.class;
	}

	@Override
	protected String getPropertyName() {
		return "spawner reward loot table";
	}

}