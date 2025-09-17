package org.skriptlang.skript.bukkit.spawners.elements.expressions.spawner.spawnerdata;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.spawners.util.SpawnerUtils;
import org.skriptlang.skript.bukkit.spawners.util.spawnerdata.SkriptSpawnerData;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Minimum/Maximum Spawn Delay")
@Description("""
	Returns the minimum or maximum spawn delay of a spawner.

	On each spawn attempt, the spawner selects a delay until the next attempt. \
	This delay is always within the defined range between the minimum and maximum values. \
	The minimum delay cannot exceed the maximum delay, and vice versa.

	Default values for mob spawners:
	  - Minimum spawn delay: 10 seconds (200 ticks)
	  - Maximum spawn delay: 40 seconds (800 ticks)

	Though, trial spawners behave differently. Their minimum and maximum spawn delays are fixed to the same value. \
	By default, both are set to 2 seconds (40 ticks).
	""")
@Example("""
	set {_data} to spawner data of event-block
	set maximum spawn delay of {_data} to 30 seconds
	reset maximum spawn delay of {_data}
	""")
@Example("""
	modify the spawner data of event-block:
		set the minimum spawn delay to 2 seconds
		add 1 second to the minimum spawn delay
		remove 1.5 seconds from the minimum spawn delay
	""")
@Since("INSERT VERSION")
public class ExprMinMaxDelay extends SimplePropertyExpression<SkriptSpawnerData, Timespan> {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.EXPRESSION, infoBuilder(ExprMinMaxDelay.class, Timespan.class,
			"(:max|min)[imum] spawn delay[s]", "spawnerdatas", true)
				.supplier(ExprMinMaxDelay::new)
				.build()
		);
	}

	private boolean max;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		max = parseResult.hasTag("max");
		return super.init(expressions, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public Timespan convert(SkriptSpawnerData data) {
		if (max)
			return data.getMaxSpawnDelay();
		return data.getMinSpawnDelay();
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, ADD, REMOVE, RESET -> CollectionUtils.array(Timespan.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		Timespan timespan = delta != null ? (Timespan) delta[0] : null;

		for (SkriptSpawnerData data : getExpr().getArray(event)) {
			Timespan minMax = getSpawnDelay(data, max);

			Timespan value = switch (mode) {
				case SET -> timespan;
				case ADD -> minMax.add(timespan);
				case REMOVE -> minMax.subtract(timespan);
				case RESET -> max ? SpawnerUtils.DEFAULT_MAX_SPAWN_DELAY : SpawnerUtils.DEFAULT_MIN_SPAWN_DELAY;
				default -> new Timespan();
			};

			assert value != null;

			String error = getErrorMessage(value, getSpawnDelay(data, !max));
			if (error != null) {
				error(error);
				continue;
			}

			if (max) {
				data.setMaxSpawnDelay(value);
			} else {
				data.setMinSpawnDelay(value);
			}
		}
	}

	private String getErrorMessage(Timespan value, Timespan compare) {
		if (max && value.compareTo(compare) < 0) {
			return "The maximum spawn delay cannot be lower than the minimum spawn delay, "
				+ "thus setting it to a value lower than the minimum spawn delay will do nothing.";
		} else if (!max && value.compareTo(compare) > 0) {
			return "The minimum spawn delay cannot be greater than the maximum spawn delay, "
				+ "thus setting it to a value higher than the maximum spawn delay will do nothing.";
		}

		return null;
	}

	private Timespan getSpawnDelay(SkriptSpawnerData data, boolean max) {
		return max ? data.getMaxSpawnDelay() : data.getMinSpawnDelay();
	}

	@Override
	public Class<? extends Timespan> getReturnType() {
		return Timespan.class;
	}

	@Override
	protected String getPropertyName() {
		if (max)
			return "maximum spawn delay";
		return "minimum spawn delay";
	}

}
