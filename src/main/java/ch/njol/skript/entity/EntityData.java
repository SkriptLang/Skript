package ch.njol.skript.entity;

import ch.njol.skript.classes.Serializer;
import ch.njol.skript.lang.SyntaxElement;
import ch.njol.skript.localization.Adjective;
import ch.njol.skript.localization.Message;
import ch.njol.util.Kleenean;
import ch.njol.yggdrasil.Fields;
import ch.njol.yggdrasil.YggdrasilSerializable.YggdrasilExtendedSerializable;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.RegionAccessor;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.NotSerializableException;
import java.io.StreamCorruptedException;
import java.util.function.Consumer;

/**
 * @deprecated Use {@link org.skriptlang.skript.bukkit.entity.EntityData} instead.
 */
@Deprecated(forRemoval = true, since = "INSERT VERSION")
public abstract class EntityData<E extends Entity> implements SyntaxElement, YggdrasilExtendedSerializable {

	public static final String LANGUAGE_NODE = "entities";

	public static final Message m_age_pattern = org.skriptlang.skript.bukkit.entity.EntityData.m_age_pattern;
	public static final Adjective m_baby = org.skriptlang.skript.bukkit.entity.EntityData.m_baby,
			m_adult = org.skriptlang.skript.bukkit.entity.EntityData.m_adult;

	public static Serializer<org.skriptlang.skript.bukkit.entity.EntityData> serializer = org.skriptlang.skript.bukkit.entity.EntityData.serializer;

	@Deprecated(forRemoval = true, since = "INSERT VERSION")
	public static <Data extends EntityData<E>, E extends Entity> void register(
		Class<Data> dataClass,
		String name,
		Class<E> entityClass,
		String codeName
	) {
		register(dataClass, name, entityClass, 0, codeName);
	}

	@Deprecated(forRemoval = true, since = "INSERT VERSION")
	public static <Data extends EntityData<E>, E extends Entity> void register(
		Class<Data> dataClass,
		String name,
		Class<E> entityClass,
		int defaultName,
		String... codeNames
	) {
		//noinspection unchecked
		org.skriptlang.skript.bukkit.entity.EntityData.registerOld(
			(Class<? extends org.skriptlang.skript.bukkit.entity.EntityData<E>>) dataClass, name, entityClass, defaultName, codeNames);
	}

	/**
	 * Prints errors.
	 *
	 * @param string String with optional indefinite article at the beginning
	 * @return The parsed entity data
	 */
	public static @Nullable EntityData<?> parse(String string) {
		return org.skriptlang.skript.bukkit.entity.EntityData.parse(string);
	}

	/**
	 * Prints errors.
	 *
	 * @param string
	 * @return The parsed entity data
	 */
	public static @Nullable EntityData<?> parseWithoutIndefiniteArticle(String string) {
		return org.skriptlang.skript.bukkit.entity.EntityData.parseWithoutIndefiniteArticle(string);
	}

	/**
	 * @param types
	 * @param type
	 * @param worlds worlds or null for all
	 * @return All entities of this type in the given worlds
	 */
	@SuppressWarnings({"null", "unchecked"})
	public static <E extends Entity> E[] getAll(EntityData<?>[] types, Class<E> type, World @Nullable [] worlds) {
		return org.skriptlang.skript.bukkit.entity.EntityData.getAll(types, type, worlds);
	}

	@SuppressWarnings("unchecked")
	public static <E extends Entity> E[] getAll(EntityData<?>[] types, Class<E> type, Chunk[] chunks) {
		return org.skriptlang.skript.bukkit.entity.EntityData.getAll(types, type, chunks);
	}

	/**
	 * Creates an {@link EntityData} that represents the given entity class.
	 *
	 * @param entityClass The class of the entity (e.g. {@code Pig.class}).
	 * @return An {@link EntityData} representing the provided class.
	 */
	public static <E extends Entity> EntityData<? super E> fromClass(Class<E> entityClass) {
		return org.skriptlang.skript.bukkit.entity.EntityData.fromClass(entityClass);
	}

	/**
	 * Creates an {@link EntityData} that represents the given entity instance.
	 *
	 * @param entity The entity to represent.
	 * @return An {@link EntityData} representing the provided entity.
	 */
	public static <E extends Entity> EntityData<? super E> fromEntity(E entity) {
		return org.skriptlang.skript.bukkit.entity.EntityData.fromEntity(entity);
	}

	public static String toString(Entity entity) {
		return org.skriptlang.skript.bukkit.entity.EntityData.toString(entity);
	}

	public static String toString(Class<? extends Entity> entityClass) {
		return org.skriptlang.skript.bukkit.entity.EntityData.toString(entityClass);
	}

	public static String toString(Entity entity, int flags) {
		return org.skriptlang.skript.bukkit.entity.EntityData.toString(entity, flags);
	}

	public static String toString(Class<? extends Entity> entityClass, int flags) {
		return org.skriptlang.skript.bukkit.entity.EntityData.toString(entityClass, flags);
	}

	/**
	 * Applies this {@link EntityData} to a newly spawned {@link Entity}.
	 * <p>
	 *     This is used during entity spawning to set additional data, such as a saddled pig.
	 * </p>
	 * @param entity The spawned entity.
	 */
	public abstract void set(E entity);

	/**
	 * Returns the {@link Class} of the {@link Entity} that this {@link EntityData} represents or handles.
	 *
	 * @return The entity's {@link Class}, such as {@code Pig.class}.
	 */
	public abstract Class<? extends E> getType();

	/**
	 * Returns a more general version of this {@link EntityData} with specific data removed.
	 * <p>
	 *     For example, calling this on {@code "a saddled pig"} should return {@code "a pig"}.
	 *     This is typically used to obtain the base entity type without any modifiers or traits.
	 * </p>
	 *
	 * @return A generalized {@link EntityData} representing the base entity type.
	 */
	public abstract @NotNull EntityData<?> getSuperType();

	@Override
	public abstract String toString();

	public abstract String toString(int flags);

	/**
	 * @return {@link Kleenean} determining whether this {@link EntityData} is representing plurality.
	 */
	public abstract Kleenean isPlural();

	/**
	 * @return {@link Kleenean} determining whether this {@link EntityData} is representing baby type.
	 */
	public abstract Kleenean isBaby();

	@Override
	public abstract int hashCode();

	@Override
	public abstract boolean equals(@Nullable Object obj);

	/**
	 * Checks whether this entity type is allowed to spawn in the given {@link World}.
	 * <p>
	 *     Some entity types may be restricted from spawning due to experimental datapacks.
	 * </p>
	 *
	 * @param world The world to check spawning permissions in.
	 * @return {@code true} if the entity can be spawned in the given world, or in general if world is {@code null}; otherwise {@code false}.
	 */
	public abstract boolean canSpawn(@Nullable World world);

	/**
	 * Spawn this entity data at a location.
	 *
	 * @param location The {@link Location} to spawn the entity at.
	 * @return The Entity object that is spawned.
	 */
	public abstract @Nullable E spawn(Location location);

	/**
	 * Spawn this entity data at a location.
	 * The consumer allows for modification to the entity before it actually gets spawned.
	 *
	 * @param location The {@link Location} to spawn the entity at.
	 * @param consumer A {@link Consumer} to apply the entity changes to.
	 * @return The Entity object that is spawned.
	 */
	public abstract @Nullable E spawn(Location location, @Nullable Consumer<E> consumer);

	public abstract E[] getAll(World... worlds);

	public abstract boolean isInstance(@Nullable Entity entity);

	/**
	 * Determines whether this {@link EntityData} is a supertype of the given {@code entityData}.
	 * <p>
	 *     This is used to check whether the current entity data represents a broader category than another.
	 *     For example:
	 *     <pre>
	 *         <code>
	 *             if a zombie is a monster:    # passes: "monster" is a supertype of "zombie"
	 *             if a monster is a zombie:    # fails: "zombie" is not a supertype of "monster"
	 *         </code>
	 *     </pre>
	 * </p>
	 *
	 * @param entityData The {@link EntityData} to compare against.
	 * @return {@code true} if this is a supertype of the given entity data, otherwise {@code false}.
	 */
	public abstract boolean isSupertypeOf(EntityData<?> entityData);

	public abstract Fields serialize() throws NotSerializableException;

	@Override
	public abstract void deserialize(Fields fields) throws StreamCorruptedException, NotSerializableException;

	@Override
	public abstract @NotNull String getSyntaxTypeName();

	/**
	 * Creates an entity in the server but does not spawn it
	 *
	 * @return The created entity
	 */
	public abstract @Nullable E create();

	/**
	 * Creates an entity at the provided location, but does not spawn it
	 * NOTE: If {@link RegionAccessor#createEntity(Location, Class)} does not exist, will return {@link #spawn(Location)}
	 * @param location The {@link Location} to create the entity at
	 * @return The created entity
	 */
	public abstract @Nullable E create(Location location);

}
