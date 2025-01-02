package org.skriptlang.skript.bukkit.spawner.elements.expressions.trialconfig;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.loot.LootTable;
import org.bukkit.spawner.TrialSpawnerConfiguration;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.spawner.SpawnerModule;
import org.skriptlang.skript.bukkit.spawner.util.SpawnerUtils;
import org.skriptlang.skript.bukkit.spawner.util.TrialSpawnerConfig;
import org.skriptlang.skript.bukkit.spawner.util.WeightedLootTable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxOrigin;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExprTrialConfigLootTables extends PropertyExpression<TrialSpawnerConfig, WeightedLootTable> {

	static {
		var info = SyntaxInfo.Expression.builder(ExprTrialConfigLootTables.class, WeightedLootTable.class)
			.origin(SyntaxOrigin.of(Skript.instance()))
			.supplier(ExprTrialConfigLootTables::new)
			.priority(PropertyExpression.DEFAULT_PRIORITY)
			.addPatterns(
				"[the] weighted loot table[s] of %trialspawnerconfigs%",
				"%trialspawnerconfigs%'[s] weighted loot table[s]")
			.build();

		SpawnerModule.SYNTAX_REGISTRY.register(SyntaxRegistry.EXPRESSION, info);
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		setExpr((Expression<? extends TrialSpawnerConfig>) exprs[0]);
		return true;
	}

	@Override
	protected WeightedLootTable[] get(Event event, TrialSpawnerConfig[] source) {
		List<WeightedLootTable> rewards = new ArrayList<>();
		for (TrialSpawnerConfig config : source) {
			for (Map.Entry<LootTable, Integer> entrySet : config.config().getPossibleRewards().entrySet()) {
				rewards.add(new WeightedLootTable(entrySet.getKey(), entrySet.getValue()));
			}
		}

		return rewards.toArray(new WeightedLootTable[0]);
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, ADD, REMOVE -> CollectionUtils.array(WeightedLootTable[].class, LootTable[].class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		assert delta != null;

		for (TrialSpawnerConfig trialConfig : getExpr().getArray(event)) {
			Map<LootTable, Integer> possibleRewards = null;
			if (mode == ChangeMode.SET)
				possibleRewards = new HashMap<>();

			TrialSpawnerConfiguration config = trialConfig.config();

			for (var object : delta) {
				if (object instanceof LootTable lootTable) {
					switch (mode) {
						case SET -> possibleRewards.put(lootTable, 1);
						case ADD -> config.addPossibleReward(lootTable, 1);
						case REMOVE -> config.removePossibleReward(lootTable);
					}
				} else if (object instanceof WeightedLootTable weightedTable) {
					switch (mode) {
						case SET -> possibleRewards.put(weightedTable.getLootTable(), weightedTable.weight());
						case ADD -> config.addPossibleReward(weightedTable.getLootTable(), weightedTable.weight());
						case REMOVE -> config.removePossibleReward(weightedTable.getLootTable());
					}
				}
			}

			if (mode == ChangeMode.SET)
				config.setPossibleRewards(possibleRewards);

			SpawnerUtils.updateState(trialConfig);
		}
	}

	@Override
	public Class<? extends WeightedLootTable> getReturnType() {
		return WeightedLootTable.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "possible trial spawner rewards of" + getExpr().toString(event, debug);
	}

}
