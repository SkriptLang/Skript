package org.skriptlang.skript.bukkit.spawner.elements.expressions.spawnrule;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.block.spawner.SpawnRule;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.spawner.SpawnerModule;

public class ExprSpawnRuleBlockLight extends SimplePropertyExpression<SpawnRule, Integer> {

	static {
		registerDefault(SpawnerModule.SYNTAX_REGISTRY, ExprSpawnRuleBlockLight.class, Integer.class,
				"(1:max[imum]|min[imum]) block light [entity] spawn [rule] (level|value)", "spawnrules"
		);
	}

	private boolean max;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		max = parseResult.mark == 1;
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public @Nullable Integer convert(SpawnRule rule) {
		if (max)
			return rule.getMaxBlockLight();
		return rule.getMinBlockLight();
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, ADD, REMOVE -> CollectionUtils.array(Integer.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		int light = delta != null ? ((int) delta[0]) : 0;

		for (SpawnRule rule : getExpr().getArray(event)) {
			if (max) {
				switch (mode) {
					case SET -> rule.setMaxBlockLight(light);
					case ADD -> rule.setMaxBlockLight(rule.getMaxBlockLight() + light);
					case REMOVE -> rule.setMaxBlockLight(rule.getMaxBlockLight() - light);
				}
			} else {
				switch (mode) {
					case SET -> rule.setMinBlockLight(light);
					case ADD -> rule.setMinBlockLight(rule.getMinBlockLight() + light);
					case REMOVE -> rule.setMinBlockLight(rule.getMinBlockLight() - light);
				}
			}
		}
	}

	@Override
	public Class<? extends Integer> getReturnType() {
		return Integer.class;
	}

	@Override
	protected String getPropertyName() {
		return (max ? "max" : "min") + " block light level";
	}

}
