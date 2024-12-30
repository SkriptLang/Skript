package org.skriptlang.skript.bukkit.spawner.elements.expressions.spawnerentry;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.block.spawner.SpawnerEntry;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.spawner.SpawnerModule;

public class ExprSpawnerEntryWeight extends SimplePropertyExpression<SpawnerEntry, Integer> {

	static {
		registerDefault(SpawnerModule.SYNTAX_REGISTRY, ExprSpawnerEntryWeight.class, Integer.class,
			"spawner entry weight", "spawnerentries");
	}

	@Override
	public @NotNull Integer convert(SpawnerEntry entry) {
		return entry.getSpawnWeight();
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
		int weight = delta != null ? ((int) delta[0]) : 0;

		for (SpawnerEntry entry : getExpr().getArray(event)) {
			switch (mode) {
				case SET -> entry.setSpawnWeight(weight);
				case ADD -> entry.setSpawnWeight(entry.getSpawnWeight() + weight);
				case REMOVE -> entry.setSpawnWeight(entry.getSpawnWeight() - weight);
			}
		}
	}

	@Override
	public Class<? extends Integer> getReturnType() {
		return Integer.class;
	}

	@Override
	protected String getPropertyName() {
		return "spawner entry weight";
	}

}
