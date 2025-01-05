package org.skriptlang.skript.bukkit.spawner.elements.expressions.spawner;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.spawner.Spawner;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.spawner.SpawnerModule;
import org.skriptlang.skript.bukkit.spawner.util.SpawnerUtils;

@Name("Spawner - Spawn Count")
@Description({
	"Returns the spawn count. The spawn count is the number of entities "
		+ "that the spawner will attempt to spawn each spawn attempt. By default, the value is 4.",
	"If the spawner entity is an item, the spawn count is the number of stacks of items to spawn.",
	"",
	"Spawners are creature spawners and spawner minecarts."
})
@Examples({
	"set spawn count of target block to 5",
	"add 2 to spawn count of target block",
	"remove 1 from spawn count of target block",
	"reset spawn count of target block"
})
@Since("INSERT VERSION")
@RequiredPlugins("Minecraft 1.21+")
public class ExprSpawnCount extends SimplePropertyExpression<Object, Integer> {

	static {
		register(SpawnerModule.SYNTAX_REGISTRY, ExprSpawnCount.class, Integer.class, "spawn count", "entities/blocks");
	}

	@Override
	public @Nullable Integer convert(Object object) {
		if (SpawnerUtils.isSpawner(object))
			return SpawnerUtils.getAsSpawner(object).getSpawnCount();
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
			if (!SpawnerUtils.isSpawner(object))
				continue;

			Spawner spawner = SpawnerUtils.getAsSpawner(object);

			switch (mode) {
				case SET -> spawner.setSpawnCount(count);
				case ADD -> spawner.setSpawnCount(spawner.getSpawnCount() + count);
				case REMOVE -> spawner.setSpawnCount(spawner.getSpawnCount() - count);
				case RESET -> spawner.setSpawnCount(4); // default value
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
		return "spawn count";
	}

}
