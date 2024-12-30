package org.skriptlang.skript.bukkit.spawner.elements.expressions.basespawner;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.block.spawner.SpawnerEntry;
import org.bukkit.entity.EntitySnapshot;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.spawner.BaseSpawner;
import org.bukkit.spawner.Spawner;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.spawner.SpawnerModule;
import org.skriptlang.skript.bukkit.spawner.util.SpawnerUtils;
import org.skriptlang.skript.bukkit.spawner.util.TrialSpawnerConfig;

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
			case SET, RESET, DELETE -> CollectionUtils.array(EntitySnapshot.class, SpawnerEntry.class, ItemStack.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		EntitySnapshot snapshot = null;
		SpawnerEntry entry = null;
		ItemStack item = null;
		if (delta != null) {
			if (delta[0] instanceof EntitySnapshot entity)
				snapshot = entity;
			else if (delta[0] instanceof SpawnerEntry spawner)
				entry = spawner;
			else if (delta[0] instanceof ItemStack stack)
				item = stack;
		}

		for (Object object : getExpr().getArray(event)) {
			if (!SpawnerUtils.isBaseSpawner(object))
				continue;

			BaseSpawner spawner = SpawnerUtils.getAsBaseSpawner(object);

			switch (mode) {
				case SET -> {
					if (snapshot != null)
						spawner.setSpawnedEntity(snapshot);
					else if (entry != null)
						spawner.setSpawnedEntity(entry);
					else if (item != null && spawner instanceof Spawner spawner1)
						spawner1.setSpawnedItem(item);
				} case RESET, DELETE -> {
					spawner.setSpawnedEntity((EntitySnapshot) null);
				}
			}

			if (object instanceof TrialSpawnerConfig config)
				SpawnerUtils.updateState(config.state());
			else
				SpawnerUtils.updateState(spawner);
		}
	}

	@Override
	public Class<? extends EntitySnapshot> getReturnType() {
		return EntitySnapshot.class;
	}

	@Override
	protected String getPropertyName() {
		return "spawner entity";
	}

}
