package org.skriptlang.skript.bukkit.spawner.elements.expressions.basespawner;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Timespan.TimePeriod;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.spawner.BaseSpawner;
import org.bukkit.spawner.Spawner;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.spawner.SpawnerModule;
import org.skriptlang.skript.bukkit.spawner.util.SpawnerUtils;

public class ExprSpawnerDelay extends SimplePropertyExpression<Object, Timespan> {

	static {
		register(SpawnerModule.SYNTAX_REGISTRY, ExprSpawnerDelay.class, Timespan.class,
			"spawn[er|ing] delay", "entities/blocks/trialspawnerconfigs");
	}

	@Override
	public @Nullable Timespan convert(Object object) {
		if (SpawnerUtils.isBaseSpawner(object))
			return new Timespan(TimePeriod.TICK, SpawnerUtils.getAsBaseSpawner(object).getDelay());
		return null;
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
			ticks = timespan.getAs(TimePeriod.TICK);
			if (ticks > Integer.MAX_VALUE)
				ticks = Integer.MAX_VALUE;
		}
		int ticksAsInt = (int) ticks;

		for (Object object : getExpr().getArray(event)) {
			if (!SpawnerUtils.isBaseSpawner(object))
				continue;

			BaseSpawner base = SpawnerUtils.getAsBaseSpawner(object);

			switch (mode) {
				case SET -> base.setDelay(ticksAsInt);
				case ADD -> base.setDelay(base.getDelay() + ticksAsInt);
				case REMOVE -> base.setDelay(base.getDelay() - ticksAsInt);
				case RESET -> {
					if (base instanceof Spawner spawner)
						spawner.setDelay(-1);
				}
			}

			SpawnerUtils.updateState(object);
		}
	}

	@Override
	public Class<? extends Timespan> getReturnType() {
		return Timespan.class;
	}

	@Override
	protected String getPropertyName() {
		return "spawner delay";
	}

}
