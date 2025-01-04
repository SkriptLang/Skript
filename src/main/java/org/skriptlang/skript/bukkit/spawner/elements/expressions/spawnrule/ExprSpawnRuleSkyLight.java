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

public class ExprSpawnRuleSkyLight extends SimplePropertyExpression<SpawnRule, Integer> {

	static {
		registerDefault(SpawnerModule.SYNTAX_REGISTRY, ExprSpawnRuleSkyLight.class, Integer.class,
				"(1:max[imum]|min[imum]) sky[ ]light [entity] spawn [rule] (level|value)", "spawnrules"
		);
	}

	private boolean max;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		max = parseResult.mark == 1;
		return super.init(expressions, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public @Nullable Integer convert(SpawnRule rule) {
		if (max)
			return rule.getMaxSkyLight();
		return rule.getMinSkyLight();
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
		assert delta != null;
		int light = ((int) delta[0]);

		if (light > 15) {
			warning("The sky light spawn level cannot be greater than 15, thus setting it to a value larger than 15 will do nothing.");
			return;
		} else if (light < 0) {
			warning("The sky light spawn level cannot be less than 0, thus setting it to a value less than 0 will do nothing.");
			return;
		}

		for (SpawnRule rule : getExpr().getArray(event)) {
			int minMax;
			if (max) {
				minMax = rule.getMaxSkyLight();
			} else {
				minMax = rule.getMinSkyLight();
			}

			int value = switch (mode) {
				case SET -> light;
				case ADD -> minMax + light;
				case REMOVE -> minMax - light;
				default -> 0; // should never happen
			};

			if (max) {
				rule.setMaxSkyLight(value);
			} else {
				rule.setMinSkyLight(value);
			}

			String warning = getWarningMessage(value, max ? rule.getMinSkyLight() : rule.getMaxSkyLight());
			if (!warning.isEmpty())
				warning(warning);
		}
	}

	private String getWarningMessage(int value, int compare) {
		if (value > 15) {
			return "The sky light spawn level cannot be greater than 15, thus setting it to a value larger than 15 will do nothing.";
		} else if (value < 0) {
			return "The sky light spawn level cannot be less than 0, thus setting it to a value less than 0 will do nothing.";
		}

		if (max && value < compare) {
			return "The maximum sky light spawn level cannot be less than the minimum sky light spawn level, "
				+ " thus setting it to a value less than the minimum sky light spawn level will do nothing.";
		} else if (!max && value > compare) {
			return "The minimum sky light spawn level cannot be greater than the maximum sky light spawn level, "
				+ "thus setting it to a value greater than the maximum sky light spawn level will do nothing.";
		}

		return "";
	}

	@Override
	public Class<? extends Integer> getReturnType() {
		return Integer.class;
	}

	@Override
	protected String getPropertyName() {
		return (max ? "max" : "min") + " sky light level";
	}

}
