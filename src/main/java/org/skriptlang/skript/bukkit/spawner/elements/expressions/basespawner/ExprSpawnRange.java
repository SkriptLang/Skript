package org.skriptlang.skript.bukkit.spawner.elements.expressions.basespawner;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.spawner.BaseSpawner;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.spawner.SpawnerModule;
import org.skriptlang.skript.bukkit.spawner.util.SpawnerUtils;

public class ExprSpawnRange extends SimplePropertyExpression<Object, Integer> {

	static {
		register(SpawnerModule.SYNTAX_REGISTRY, ExprSpawnRange.class, Integer.class,
			"spawn (radius|range)", "entities/blocks/trialspawnerconfigs");
	}

	@Override
	public @Nullable Integer convert(Object object) {
		if (SpawnerUtils.isBaseSpawner(object))
			return SpawnerUtils.getAsBaseSpawner(object).getSpawnRange();
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
		int range = delta != null ? ((Number) delta[0]).intValue() : 0;

		for (Object object : getExpr().getArray(event)) {
			if (!SpawnerUtils.isBaseSpawner(object))
				continue;

			BaseSpawner spawner = SpawnerUtils.getAsBaseSpawner(object);

			switch (mode) {
				case SET -> spawner.setSpawnRange(range);
				case ADD -> spawner.setSpawnRange(spawner.getSpawnRange() + range);
				case REMOVE -> spawner.setSpawnRange(spawner.getSpawnRange() - range);
				case RESET -> spawner.setSpawnRange(4); // default value
			}

			SpawnerUtils.updateState(spawner);
		}
	}

	@Override
	public Class<? extends Integer> getReturnType() {
		return Integer.class;
	}

	@Override
	protected String getPropertyName() {
		return "spawn range";
	}

}
