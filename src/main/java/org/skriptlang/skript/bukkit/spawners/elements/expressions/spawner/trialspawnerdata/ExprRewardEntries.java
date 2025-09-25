package org.skriptlang.skript.bukkit.spawners.elements.expressions.spawner.trialspawnerdata;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.loot.LootTable;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.spawners.util.spawnerdata.SkriptTrialSpawnerData;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Name("Reward Entries")
@Description("""
    Returns the reward entries of the trial spawner data. Reward entries are loot tables that the trial spawner \
    can select from when ejecting rewards. Each entry has a weight that determines its chance of being chosen. \
    By default, all reward entries have a weight of 1.
    """)
@Example("""
	set {_data} to the trial spawner data of event-block
	set reward entry of {_data} to loot table "minecraft:chests/simple_dungeon"
	set the reward weight for loot table "minecraft:chests/simple_dungeon" of {_data} to 5
	add loot table "minecraft:spawners/trial_chamber/items_to_drop_when_ominous" to the reward entries of {_data}
	delete the reward entries of {_data}
	""")
@Example("""
	modify the trial spawner data of event-block:
		add loot table "minecraft:chests/simple_dungeon" to the reward entries
		set the reward weight for loot table "minecraft:chests/simple_dungeon" to 5
		remove loot table "minecraft:chests/simple_dungeon" from the reward entries
	""")
@Since("INSERT VERSION")
public class ExprRewardEntries extends PropertyExpression<SkriptTrialSpawnerData, LootTable> {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(SyntaxRegistry.EXPRESSION, SyntaxInfo.Expression.builder(ExprRewardEntries.class, LootTable.class)
			.supplier(ExprRewardEntries::new)
			.priority(DEFAULT_PRIORITY)
			.addPatterns(getDefaultPatterns("reward entr(y|ies)", "trialspawnerdatas"))
			.build()
		);
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		setExpr((Expression<? extends SkriptTrialSpawnerData>) exprs[0]);
		return true;
	}

	@Override
	protected LootTable[] get(Event event, SkriptTrialSpawnerData[] source) {
		return Arrays.stream(source)
			.flatMap(data -> data.getRewardEntries().keySet().stream())
			.toArray(LootTable[]::new);
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, ADD, REMOVE, DELETE -> CollectionUtils.array(LootTable[].class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		Map<LootTable, Integer> lootTables = new HashMap<>();
		if (delta != null) {
			for (Object object : delta) {
				lootTables.put((LootTable) object, 1);
			}
		}

		for (SkriptTrialSpawnerData data : getExpr().getArray(event)) {
			if (mode == ChangeMode.DELETE) {
				data.clearRewardEntries();
				continue;
			}

			Map<LootTable, Integer> currentEntries = data.getRewardEntries();
			switch (mode) {
				case SET -> currentEntries = lootTables;
				case ADD -> currentEntries.putAll(lootTables);
				case REMOVE -> lootTables.keySet().forEach(currentEntries::remove);
			}

			data.setRewardEntries(currentEntries);
		}
	}

	@Override
	public Class<? extends LootTable> getReturnType() {
		return LootTable.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "reward entries of " + getExpr().toString(event, debug);
	}

}
