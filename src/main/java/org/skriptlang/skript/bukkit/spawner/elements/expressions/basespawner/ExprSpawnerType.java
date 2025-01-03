package org.skriptlang.skript.bukkit.spawner.elements.expressions.basespawner;

import ch.njol.skript.bukkitutil.EntityUtils;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.*;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Event;
import org.bukkit.spawner.BaseSpawner;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.spawner.SpawnerModule;
import org.skriptlang.skript.bukkit.spawner.util.SpawnerUtils;

@Name("Spawner Type")
@Description("Retrieves, sets, or resets the spawner's entity type")
@Examples({
	"on right click:",
	"\tif event-block is spawner:",
	"\t\tsend \"Spawner's type is %target block's spawner type%\""
})
@Since("2.4, 2.9.2 (trial spawner), INSERT VERSION (trial spawner config, pattern change)")
@RequiredPlugins("Minecraft 1.21+ (since INSERT VERSION)")
public class ExprSpawnerType extends SimplePropertyExpression<Object, EntityData> {

	static {
		register(SpawnerModule.SYNTAX_REGISTRY, ExprSpawnerType.class, EntityData.class,
			"spawner [entity|spawned] type[s]", "entities/blocks/trialspawnerconfigs");
	}

	@Nullable
	public EntityData<?> convert(Object object) {
		if (SpawnerUtils.isBaseSpawner(object))
			return EntityUtils.toSkriptEntityData(SpawnerUtils.getAsBaseSpawner(object).getSpawnedType());
		return null;
	}

	@Nullable
	@Override
	public Class<?>[] acceptChange(Changer.ChangeMode mode) {
		return switch (mode) {
			case SET, DELETE -> CollectionUtils.array(EntityData.class);
			default -> null;
		};
	}

	@SuppressWarnings("null")
	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		EntityType type = delta != null ? EntityUtils.toBukkitEntityType((EntityData<?>) delta[0]) : null;

		for (Object object : getExpr().getArray(event)) {
			if (!SpawnerUtils.isBaseSpawner(object))
				continue;

			BaseSpawner spawner = SpawnerUtils.getAsBaseSpawner(object);

			switch (mode) {
				case SET -> spawner.setSpawnedType(type);
				case DELETE -> spawner.setSpawnedType(null);
			}

			SpawnerUtils.updateState(spawner);
		}
	}

	@Override
	public Class<EntityData> getReturnType() {
		return EntityData.class;
	}

	@Override
	protected String getPropertyName() {
		return "spawner entity type";
	}

}
