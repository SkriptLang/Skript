package ch.njol.skript.bukkitutil;

import ch.njol.skript.Skript;
import ch.njol.skript.entity.EntityData;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.jetbrains.annotations.ApiStatus.ScheduledForRemoval;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for quick {@link Entity} methods
 */
public class EntityUtils {

	private static final boolean HAS_PIGLINS = Skript.classExists("org.bukkit.entity.Piglin");

	/**
	 * Cache Skript EntityData -> Bukkit EntityType
	 */
	private static final BiMap<EntityData<?>, EntityType> SPAWNER_TYPES = HashBiMap.create();
	private static final Map<Class<? extends Entity>, EntityType> CLASS_ENTITY_TYPE_MAP = new HashMap<>();

	static {
		for (EntityType entityType : EntityType.values()) {
			Class<? extends Entity> entityClass = entityType.getEntityClass();
			if (entityClass != null)
				CLASS_ENTITY_TYPE_MAP.put(entityClass, entityType);
		}
	}

	/**
	 * Check if an entity is ageable.
	 * Some entities, such as zombies, do not have an age but can be a baby/adult.
	 *
	 * @param entity Entity to check
	 * @return True if entity is ageable
	 */
	public static boolean isAgeable(Entity entity) {
		if (entity instanceof Ageable || entity instanceof Zombie)
			return true;
		return HAS_PIGLINS && (entity instanceof Piglin || entity instanceof Zoglin);
	}

	/**
	 * Get the age of an ageable entity.
	 * Entities such as zombies do not have an age, this will return -1 if baby, 0 if adult.
	 *
	 * @param entity Entity to grab age for
	 * @return Age of entity (if zombie/piglin/zoglin -1 = baby, 0 = adult) (if not ageable, will return 0)
	 */
	public static int getAge(Entity entity) {
		if (entity instanceof Ageable)
			return ((Ageable) entity).getAge();
		else if (entity instanceof Zombie)
			return ((Zombie) entity).isBaby() ? -1 : 0;
		else if (HAS_PIGLINS) {
			if (entity instanceof Piglin)
				return ((Piglin) entity).isBaby() ? -1 : 0;
			else if (entity instanceof Zoglin)
				return ((Zoglin) entity).isBaby() ? -1 : 0;
		}
		return 0;
	}

	/**
	 * Set the age of an entity.
	 * Entities such as zombies do not have an age, setting below 0 will make them a baby otherwise adult.
	 *
	 * @param entity Entity to set age for
	 * @param age    Age to set
	 */
	public static void setAge(Entity entity, int age) {
		if (entity instanceof Ageable)
			((Ageable) entity).setAge(age);
		else if (entity instanceof Zombie)
			((Zombie) entity).setBaby(age < 0);
		else if (HAS_PIGLINS) {
			if (entity instanceof Piglin)
				((Piglin) entity).setBaby(age < 0);
			else if (entity instanceof Zoglin)
				((Zoglin) entity).setBaby(age < 0);
		}
	}

	/**
	 * Quick method for making an entity a baby.
	 * Ageable entities (such as sheep or pigs) will set their default baby age to -24000.
	 *
	 * @param entity Entity to make baby
	 */
	public static void setBaby(Entity entity) {
		setAge(entity, -24000);
	}

	/**
	 * Quick method for making an entity an adult.
	 *
	 * @param entity Entity to make adult
	 */
	public static void setAdult(Entity entity) {
		setAge(entity, 0);
	}

	/**
	 * Quick method to check if entity is an adult.
	 *
	 * @param entity Entity to check
	 * @return True if entity is an adult
	 */
	public static boolean isAdult(Entity entity) {
		return getAge(entity) >= 0;
	}

	private static void loadSpawnerTypes() {
		for (EntityType e : EntityType.values()) {
			Class<? extends Entity> c = e.getEntityClass();
			if (c != null)
				SPAWNER_TYPES.put(EntityData.fromClass(c), e);
		}
	}

	/**
	 * Convert from Skript's EntityData to Bukkit's EntityType
	 * @param e Skript's EntityData
	 * @return Bukkit's EntityType
	 */
	public static EntityType toBukkitEntityType(EntityData<?> e) {
		if (SPAWNER_TYPES.isEmpty())
			loadSpawnerTypes();
		EntityData<?> entityData = EntityData.fromClass(e.getType()); // Fix Comparison Issues
		if (SPAWNER_TYPES.containsKey(entityData))
			return SPAWNER_TYPES.get(entityData);
        return toBukkitEntityType(e.getType());
	}

	/**
	 * Attempts to get an {@link EntityType} from a {@link Class} extending {@link Entity}.
	 * Ensures at least one {@link EntityType} can represent an entity class through {@link Class#isAssignableFrom(Class)}.
	 * @param entityClass The {@link Class} extending {@link Entity}
	 * @return The exact or assignable {@link EntityType} or {@code null}
	 */
	public static @Nullable EntityType toBukkitEntityType(Class<? extends Entity> entityClass) {
		if (CLASS_ENTITY_TYPE_MAP.containsKey(entityClass)) {
			return CLASS_ENTITY_TYPE_MAP.get(entityClass);
		}
		EntityType closestEntityType = null;
		Class<? extends Entity> closestClass = null;
		for (EntityType entityType : EntityType.values()) {
			Class<? extends Entity> typeClass = entityType.getEntityClass();
			if (typeClass != null && typeClass.isAssignableFrom(entityClass)) {
				if (closestEntityType == null || closestClass.isAssignableFrom(typeClass)) {
					closestEntityType = entityType;
					closestClass = typeClass;
					if (typeClass.equals(entityClass))
						break;
				}
			}
		}
		CLASS_ENTITY_TYPE_MAP.put(entityClass, closestEntityType);
		return closestEntityType;
	}

	/**
	 * Convert from Bukkit's EntityType to Skript's EntityData
	 * @param e Bukkit's EntityType
	 * @return Skript's EntityData
	 */
	public static EntityData<?> toSkriptEntityData(EntityType e) {
		if (SPAWNER_TYPES.isEmpty())
			loadSpawnerTypes();
		return SPAWNER_TYPES.inverse().get(e);
	}

	/**
	 * Teleports the given entity to the given location.
	 * Teleports to the given location in the entity's world if the location's world is null.
	 * @deprecated this method is only used by EffTeleport, and with the recent additions of TeleportFlag, this method should be moved within that effect.
	 */
	@Deprecated(since = "2.10.0", forRemoval = true)
	public static void teleport(Entity entity, Location location) {
		if (location.getWorld() == null) {
			location = location.clone();
			location.setWorld(entity.getWorld());
		}

		entity.teleport(location);
	}

}
