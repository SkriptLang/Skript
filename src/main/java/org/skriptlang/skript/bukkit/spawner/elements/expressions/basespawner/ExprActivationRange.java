package org.skriptlang.skript.bukkit.spawner.elements.expressions.basespawner;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.spawner.BaseSpawner;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.spawner.trial.TrialSpawnerConfig;
import org.skriptlang.skript.bukkit.spawner.util.SpawnerUtils;

public class ExprActivationRange extends SimplePropertyExpression<Object, Integer> {

	static {
		register(ExprActivationRange.class, Integer.class, "spawner activation range", "entities/blocks/trialspawnerconfigs");
	}

	@Override
	public @Nullable Integer convert(Object object) {
		if (SpawnerUtils.isBaseSpawner(object))
			return SpawnerUtils.getAsBaseSpawner(object).getRequiredPlayerRange();
		return null;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, ADD, REMOVE, RESET -> CollectionUtils.array(Integer.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		int count = delta != null ? ((int) delta[0]) : 0;

		for (Object object : getExpr().getArray(event)) {
			if (SpawnerUtils.isBaseSpawner(object))
				continue;

			BaseSpawner spawner = SpawnerUtils.getAsBaseSpawner(object);

			switch (mode) {
				case SET -> spawner.setRequiredPlayerRange(count);
				case ADD -> spawner.setRequiredPlayerRange(spawner.getRequiredPlayerRange() + count);
				case REMOVE -> spawner.setRequiredPlayerRange(spawner.getRequiredPlayerRange() - count);
				case RESET -> spawner.setRequiredPlayerRange(16); // default value stated in the javadocs
			}

			if (object instanceof TrialSpawnerConfig config)
				SpawnerUtils.updateState(config.getState());
			else
				SpawnerUtils.updateState(spawner);
		}
	}

	@Override
	public Class<? extends Integer> getReturnType () {
		return Integer.class;
	}

	@Override
	protected String getPropertyName () {
		return "required player range";
	}

}
