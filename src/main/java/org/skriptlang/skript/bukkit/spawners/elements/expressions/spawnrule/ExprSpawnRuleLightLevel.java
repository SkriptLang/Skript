package org.skriptlang.skript.bukkit.spawners.elements.expressions.spawnrule;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.Math2;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.block.spawner.SpawnRule;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Spawn Rule Light Level")
@Description("""
    Returns the minimum or maximum block or sky light levels of a spawn rule. \
    Block light refers to the light level emitted by blocks, while sky light refers to sunlight.

    Valid values range from 0 to 15. The minimum value must be less than or equal to the maximum value.
    """)
@Example("""
	set {_entry} to a spawner entry of a zombie:
		set the spawn rule to a spawn rule:
			set the maximum block light spawn level to 15
			set the minimum block light spawn level to 10
			set the maximum sky light spawn level to 15
			set the minimum sky light spawn level to 5
	""")
@Since("INSERT VERSION")
public class ExprSpawnRuleLightLevel extends SimplePropertyExpression<SpawnRule, Integer> {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.EXPRESSION, infoBuilder(ExprSpawnRuleLightLevel.class, Integer.class,
				"(:max|min)[imum] (block|:sky) light [entity] spawn (level|value)[s]", "spawnrules", true)
				.supplier(ExprSpawnRuleLightLevel::new)
				.build()
		);
	}

	private boolean max;
	private boolean skyLight;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		max = parseResult.hasTag("max");
		skyLight = parseResult.hasTag("sky");
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public Integer convert(SpawnRule rule) {
		return getLightLevel(rule, max);
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
		int light = Math2.fit(0, (int) delta[0], 15);

		for (SpawnRule rule : getExpr().getArray(event)) {
			int minMax = getLightLevel(rule, max);

			int value = switch (mode) {
				case SET -> light;
				case ADD -> minMax + light;
				case REMOVE -> minMax - light;
				default -> 0;
			};

			value = Math2.fit(0, value, 15);

			String error = getErrorMessage(value, getLightLevel(rule, !max));
			if (error != null) {
				error(error);
				continue;
			}

			if (skyLight) {
				if (max) {
					rule.setMaxSkyLight(value);
				} else {
					rule.setMinSkyLight(value);
				}
			} else {
				if (max) {
					rule.setMaxBlockLight(value);
				} else {
					rule.setMinBlockLight(value);
				}
			}
		}
	}

	private String getErrorMessage(int value, int compare) {
		if (max && value < compare) {
			return "The maximum block light level cannot be less than the minimum block light level, "
				+ " thus setting it to a value less than the minimum block light level will do nothing.";
		} else if (!max && value > compare) {
			return "The minimum block light spawn level cannot be greater than the maximum block light spawn level, "
				+ "thus setting it to a value greater than the maximum block light spawn level will do nothing.";
		}

		return null;
	}

	private int getLightLevel(SpawnRule rule, boolean max) {
		if (skyLight)
			return max ? rule.getMaxSkyLight() : rule.getMinSkyLight();
		return max ? rule.getMaxBlockLight() : rule.getMinBlockLight();
	}

	@Override
	public Class<? extends Integer> getReturnType() {
		return Integer.class;
	}

	@Override
	protected String getPropertyName() {
		return (max ? "maximum" : "minimum") + " block light spawn level";
	}

}
