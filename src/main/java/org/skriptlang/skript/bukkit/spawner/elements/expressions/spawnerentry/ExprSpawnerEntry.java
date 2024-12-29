package org.skriptlang.skript.bukkit.spawner.elements.expressions.spawnerentry;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.block.spawner.SpawnRule;
import org.bukkit.block.spawner.SpawnerEntry;
import org.bukkit.entity.EntitySnapshot;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

public class ExprSpawnerEntry extends SimpleExpression<SpawnerEntry> {

	static {
		Skript.registerExpression(ExprSpawnerEntry.class, SpawnerEntry.class, ExpressionType.COMBINED,
			"[a] spawner entry (using|with) %entitysnapshot% [(using|with) %-spawnrule%] [with weight %-integer%]"
		);
	}

	private Expression<EntitySnapshot> snapshot;
	private Expression<SpawnRule> spawnRule;
	private Expression<Integer> weight;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		snapshot = (Expression<EntitySnapshot>) exprs[0];
		spawnRule = (Expression<SpawnRule>) exprs[1];
		weight = (Expression<Integer>) exprs[2];
		return true;
	}

	@Override
	protected SpawnerEntry @Nullable [] get(Event event) {
		EntitySnapshot snapshot = this.snapshot.getSingle(event);
		if (snapshot == null)
			return new SpawnerEntry[0];

		SpawnRule spawnRule = null;
		if (this.spawnRule != null)
			spawnRule = this.spawnRule.getSingle(event);

		int weight = 1;
		if (this.weight != null)
			weight = this.weight.getSingle(event).intValue();

		return new SpawnerEntry[]{new SpawnerEntry(snapshot, weight, spawnRule)};
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends SpawnerEntry> getReturnType() {
		return SpawnerEntry.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);

		builder.append("spawner entry using")
			.append(this.snapshot)
			.append("with")
			.append(this.spawnRule);

		if (this.weight != null)
			builder.append("and weight")
				.append(this.weight);

		return builder.toString();
	}

}
