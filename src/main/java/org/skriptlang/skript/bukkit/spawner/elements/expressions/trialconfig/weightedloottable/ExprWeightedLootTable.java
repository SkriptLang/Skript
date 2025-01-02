package org.skriptlang.skript.bukkit.spawner.elements.expressions.trialconfig.weightedloottable;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.loot.LootTable;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.spawner.SpawnerModule;
import org.skriptlang.skript.bukkit.spawner.util.WeightedLootTable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxOrigin;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class ExprWeightedLootTable extends SimpleExpression<WeightedLootTable> {

	static {
		var info = SyntaxInfo.Expression.builder(ExprWeightedLootTable.class, WeightedLootTable.class)
			.origin(SyntaxOrigin.of(Skript.instance()))
			.supplier(ExprWeightedLootTable::new)
			.priority(SyntaxInfo.COMBINED)
			.addPattern("%loottable% with weight %integer%")
			.build();

		SpawnerModule.SYNTAX_REGISTRY.register(SyntaxRegistry.EXPRESSION, info);
	}

	private Expression<LootTable> lootTable;
	private Expression<Integer> weight;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		lootTable = (Expression<LootTable>) exprs[0];
		weight = (Expression<Integer>) exprs[1];
		return true;
	}

	@Override
	protected WeightedLootTable @Nullable [] get(Event event) {
		LootTable lootTable = this.lootTable.getSingle(event);
		if (lootTable == null)
			return new WeightedLootTable[0];

		Integer weight = this.weight.getSingle(event);
		if (weight == null)
			return new WeightedLootTable[0];

		return new WeightedLootTable[]{new WeightedLootTable(lootTable, weight)};
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends WeightedLootTable> getReturnType() {
		return WeightedLootTable.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);

		builder.append("weighted loot table", lootTable);
		if (weight != null)
			builder.append("with weight", weight);

		return builder.toString();
	}

}
