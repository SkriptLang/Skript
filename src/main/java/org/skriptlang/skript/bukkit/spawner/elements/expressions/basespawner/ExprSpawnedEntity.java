package org.skriptlang.skript.bukkit.spawner.elements.expressions.basespawner;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.block.spawner.SpawnerEntry;
import org.bukkit.entity.EntitySnapshot;
import org.bukkit.event.Event;
import org.bukkit.spawner.BaseSpawner;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.spawner.SpawnerModule;
import org.skriptlang.skript.bukkit.spawner.util.SpawnerUtils;

public class ExprSpawnedEntity extends SimplePropertyExpression<Object, EntitySnapshot> {

	static {
		register(SpawnerModule.SYNTAX_REGISTRY, ExprSpawnedEntity.class, EntitySnapshot.class,
			"spawner [spawned] entity", "entities/blocks/trialspawnerconfigs");
	}

	@Override
	public @Nullable EntitySnapshot convert(Object object) {
		if (SpawnerUtils.isBaseSpawner(object))
			return SpawnerUtils.getAsBaseSpawner(object).getSpawnedEntity();
		return null;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, RESET, DELETE -> CollectionUtils.array(SpawnerEntry.class, EntitySnapshot.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		Object value = delta != null ? delta[0] : null;

		for (Object object : getExpr().getArray(event)) {
			if (!SpawnerUtils.isBaseSpawner(object))
				continue;

			BaseSpawner spawner = SpawnerUtils.getAsBaseSpawner(object);

			switch (mode) {
				case SET -> {
					if (value instanceof SpawnerEntry entry) {
						spawner.setSpawnedEntity(entry);
					} else if (value instanceof EntitySnapshot snapshot) {
						spawner.setSpawnedEntity(snapshot);
					}
				}
				case RESET, DELETE -> spawner.setSpawnedEntity((EntitySnapshot) null);
			}

			SpawnerUtils.updateState(spawner);
		}
	}

	@Override
	public Class<? extends EntitySnapshot> getReturnType() {
		return EntitySnapshot.class;
	}

	@Override
	protected String getPropertyName() {
		return "spawner spawned entity";
	}

}