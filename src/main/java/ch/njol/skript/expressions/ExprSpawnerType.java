package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.bukkitutil.EntityUtils;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.TrialSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Event;
import org.bukkit.spawner.TrialSpawnerConfiguration;
import org.jetbrains.annotations.Nullable;

@Name("Spawner Type")
@Description("""
	The entity type of a spawner (mob spawner).
	Change the entity type, reset it (pig) or clear it (Minecraft 1.20.0+).
	""")
@Example("""
	on right click:
		if event-block is a spawner:
			send "Spawner's type if %spawner type of event-block%" to player
	""")
@Example("set the creature type of {_spawner} to a trader llama")
@Example("reset {_spawner}'s entity type # Pig")
@Example("clear the spawner type of {_spawner} # Minecraft 1.20.0+")
@Since("2.4, 2.9.2 (trial spawner), 2.12 (delete)")
@RequiredPlugins("Minecraft 1.20.0+ (delete)")
public class ExprSpawnerType extends SimplePropertyExpression<Block, EntityData> {

	private static final boolean HAS_TRIAL_SPAWNER = Skript.classExists("org.bukkit.block.TrialSpawner");
	private static final boolean RUNNING_1_20_0 = Skript.isRunningMinecraft(1, 20, 0);

	static {
		register(ExprSpawnerType.class, EntityData.class, "(spawner|entity|creature) type[s]", "blocks");
	}

	@Nullable
	public EntityData convert(Block block) {
		if (block.getState() instanceof CreatureSpawner creatureSpawner) {
			EntityType type = creatureSpawner.getSpawnedType();
			if (type == null)
				return null;
			return EntityUtils.toSkriptEntityData(type);
		} else if (HAS_TRIAL_SPAWNER && block.getState() instanceof TrialSpawner trialSpawner) {
			EntityType type;
			if (trialSpawner.isOminous()) {
				type = trialSpawner.getOminousConfiguration().getSpawnedType();
			} else {
				type = trialSpawner.getNormalConfiguration().getSpawnedType();
			}
			if (type == null)
				return null;
			return EntityUtils.toSkriptEntityData(type);
		}
		return null;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET || mode == ChangeMode.RESET) {
			return CollectionUtils.array(EntityData.class);
		} else if (mode == ChangeMode.DELETE) {
			if (RUNNING_1_20_0) {
				return CollectionUtils.array(EntityData.class);
			} else {
				Skript.error("You can only delete the spawner type of a spawner on Minecraft 1.20.0 or newer");
			}
		}
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		EntityType entityType = null;
		if (delta != null) {
			//noinspection rawtypes
			entityType = EntityUtils.toBukkitEntityType(((EntityData) delta[0]));
		} else if (mode == ChangeMode.RESET) {
			entityType = EntityType.PIG;
		}

		for (Block block : getExpr().getArray(event)) {
			if (block.getState() instanceof CreatureSpawner creatureSpawner) {
				creatureSpawner.setSpawnedType(entityType);
				creatureSpawner.update(); // Actually trigger the spawner's update
			} else if (HAS_TRIAL_SPAWNER && block.getState() instanceof TrialSpawner trialSpawner) {
				TrialSpawnerConfiguration config;
				if (trialSpawner.isOminous()) {
					config = trialSpawner.getOminousConfiguration();
				} else {
					config = trialSpawner.getNormalConfiguration();
				}
				config.setSpawnedType(entityType);
				trialSpawner.update();
			}
		}
	}

	@Override
	public Class<EntityData> getReturnType() {
		return EntityData.class;
	}
	
	@Override
	protected String getPropertyName() {
		return "entity type";
	}
	
}
