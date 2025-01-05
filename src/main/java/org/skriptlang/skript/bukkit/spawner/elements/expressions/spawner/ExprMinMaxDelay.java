package org.skriptlang.skript.bukkit.spawner.elements.expressions.spawner;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Timespan.TimePeriod;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.spawner.Spawner;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.spawner.SpawnerModule;
import org.skriptlang.skript.bukkit.spawner.util.SpawnerUtils;

@Name("Spawner - Min/Max Spawn Delay")
@Description({
	"Get, set the maximum or minimum spawn delay of a spawner.",
	"Each reset of a spawner, the spawner chooses a new delay between its' "
		+ "minimum and maximum delays to use for the delay.",
	"By default, he maximum value is 40 seconds (800 ticks) and the minimum value is 10 seconds (200 ticks).",
	"Setting the minimum delay higher than the maximum delay and so on does nothing.",
	"",
	"Spawners are creature spawners and spawner minecarts."
})
@Examples({
	"set {_timespan} to minimum spawn delay of target block",
	"set max spawn delay of target block to 500 ticks"
})
@Since("INSERT VERSION")
public class ExprMinMaxDelay extends SimplePropertyExpression<Object, Timespan> {

	static {
		register(SpawnerModule.SYNTAX_REGISTRY, ExprMinMaxDelay.class, Timespan.class,
			"(1:max[imum]|min[imum]) spawn[er|ing] delay", "entities/blocks");
	}

	private boolean isMax;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		isMax = parseResult.mark == 1;
		return super.init(expressions, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public @Nullable Timespan convert(Object object) {
		if (!SpawnerUtils.isSpawner(object))
			return null;

		Spawner spawner = SpawnerUtils.getAsSpawner(object);

		if (isMax)
			return new Timespan(TimePeriod.TICK, spawner.getMaxSpawnDelay());
		return new Timespan(TimePeriod.TICK, spawner.getMinSpawnDelay());
	}

	@Override
	public Class<?>[] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, ADD, REMOVE, RESET -> CollectionUtils.array(Timespan.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		Timespan timespan = delta != null ? (Timespan) delta[0] : null;

		long ticks = 0;
		if (timespan != null) {
			ticks = timespan.getAs(Timespan.TimePeriod.TICK);
			if (ticks > Integer.MAX_VALUE)
				ticks = Integer.MAX_VALUE;
		}
		int ticksAsInt = (int) ticks;

		for (Object object : getExpr().getArray(event)) {
			if (!SpawnerUtils.isSpawner(object))
				continue;

			Spawner spawner = SpawnerUtils.getAsSpawner(object);

			int minMax;
			if (isMax) {
				minMax = spawner.getMaxSpawnDelay();
			} else {
				minMax = spawner.getMinSpawnDelay();
			}

			int value = switch (mode) {
				case SET -> ticksAsInt;
				case ADD -> minMax + ticksAsInt;
				case REMOVE -> minMax - ticksAsInt;
				case RESET -> isMax ? 800 : 200;
				default -> 0; // should never happen
			};

			if (isMax) {
				spawner.setMaxSpawnDelay(value);
			} else {
				spawner.setMinSpawnDelay(value);
			}

			SpawnerUtils.updateState(spawner);

			if (isMax && value < spawner.getMinSpawnDelay()) {
				warning("The maximum spawn delay cannot be lower than the minimum spawn delay, "
					+ "thus setting it to a value lower than the minimum spawn delay will do nothing.");
			} else if (!isMax && value > spawner.getMaxSpawnDelay()) {
				warning("The minimum spawn delay cannot be lower than the maximum spawn delay, "
					+ "thus setting it to a value lower than the maximum spawn delay will do nothing.");
			}
		}
	}

	@Override
	public Class<? extends Timespan> getReturnType() {
		return Timespan.class;
	}

	@Override
	protected String getPropertyName() {
		if (isMax)
			return "max spawn delay";
		return "min spawn delay";
	}

}
