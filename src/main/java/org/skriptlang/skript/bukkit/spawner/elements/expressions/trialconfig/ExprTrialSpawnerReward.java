package org.skriptlang.skript.bukkit.spawner.elements.expressions.trialconfig;

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
import org.skriptlang.skript.bukkit.spawner.util.TrialSpawnerReward;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxOrigin;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class ExprTrialSpawnerReward extends SimpleExpression<TrialSpawnerReward> {

	static {
		var info = SyntaxInfo.Expression.builder(ExprTrialSpawnerReward.class, TrialSpawnerReward.class)
			.origin(SyntaxOrigin.of(Skript.instance()))
			.supplier(ExprTrialSpawnerReward::new)
			.priority(SyntaxInfo.COMBINED)
			.addPatterns(
				"[trial] spawner reward [with weight %-number% [and]] with %loottable%",
				"[trial] spawner reward with %loottable% [[and] weight %-number%]")
			.build();

		SpawnerModule.SYNTAX_REGISTRY.register(SyntaxRegistry.EXPRESSION, info);
	}

	private Expression<Number> weight;
	private Expression<LootTable> lootTable;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (matchedPattern == 0) {
			weight = (Expression<Number>) exprs[0];
			lootTable = (Expression<LootTable>) exprs[1];
		} else {
			weight = (Expression<Number>) exprs[1];
			lootTable = (Expression<LootTable>) exprs[0];
		}
		return true;
	}

	@Override
	protected TrialSpawnerReward @Nullable [] get(Event event) {
		LootTable lootTable = this.lootTable.getSingle(event);
		if (lootTable == null)
			return new TrialSpawnerReward[0];

		int weight = 1;
		if (this.weight != null) {
			Number number = this.weight.getSingle(event);
			if (number != null)
				weight = number.intValue();
		}

		return new TrialSpawnerReward[]{new TrialSpawnerReward(lootTable, weight)};
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends TrialSpawnerReward> getReturnType() {
		return TrialSpawnerReward.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);

		builder.append("trial spawner reward with", lootTable);
		if (weight != null)
			builder.append("and weight", weight);

		return builder.toString();
	}

}
