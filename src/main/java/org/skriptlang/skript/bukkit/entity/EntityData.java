package org.skriptlang.skript.bukkit.entity;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxElement;
import ch.njol.skript.localization.Noun;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import ch.njol.yggdrasil.FieldHandler;
import ch.njol.yggdrasil.Fields;
import ch.njol.yggdrasil.Fields.FieldContext;
import ch.njol.yggdrasil.YggdrasilSerializable.YggdrasilExtendedSerializable;
import io.papermc.paper.world.flag.FeatureDependant;
import io.papermc.paper.world.flag.FeatureFlagSetHolder;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.RegionAccessor;
import org.bukkit.World;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.entity.EntityDataInfo.Builder;
import org.skriptlang.skript.bukkit.entity.data.PigData;
import org.skriptlang.skript.bukkit.entity.data.SimpleEntityData;
import org.skriptlang.skript.bukkit.entity.data.SimpleEntityData.SimpleEntityDataInfo;
import org.skriptlang.skript.localization.GeneralNoun;

import java.io.NotSerializableException;
import java.io.StreamCorruptedException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SequencedCollection;
import java.util.function.Consumer;

/**
 * Class representing and handling {@link Entity} for comparison, changing data, matching data, and spawning with initial data.
 * @param <E> The type of {@link Entity}.
 */
@SuppressWarnings("rawtypes")
public abstract class EntityData<E extends Entity>
	extends ch.njol.skript.entity.EntityData<E>
	implements SyntaxElement, YggdrasilExtendedSerializable {

	// Removed in 1.21.9 in favor of 'FeatureFlagSetHolder'
	private static final boolean HAS_ENABLED_BY_FEATURE = Skript.methodExists(EntityType.class, "isEnabledByFeature", World.class);
	private static final @Nullable Method ENABLED_BY_FEATURE_METHOD;

	// Added in 1.21.9, replaces 'isEnabledByFeature'
	private static final @Nullable Method IS_ENABLED_METHOD;

	static {
		if (HAS_ENABLED_BY_FEATURE) {
			IS_ENABLED_METHOD = null;
			try {
				ENABLED_BY_FEATURE_METHOD = EntityType.class.getDeclaredMethod("isEnabledByFeature", World.class);
			} catch (NoSuchMethodException e) {
				throw new RuntimeException(e);
			}
		} else {
			ENABLED_BY_FEATURE_METHOD = null;
			try {
				IS_ENABLED_METHOD = FeatureFlagSetHolder.class.getDeclaredMethod("isEnabled", FeatureDependant.class);
			} catch (NoSuchMethodException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public static final String AGE_PATTERN = "[baby:(baby|young)|adult:(adult|grown(-| )up)]";
	public static final GeneralNoun AGE_BABY = new GeneralNoun("baby");
	public static final GeneralNoun AGE_ADULT = new GeneralNoun("adult");

	// must be here to be initialized before 'new SimpleLiteral' is called in the register block below
	private static final List<EntityDataInfo<EntityData<?>, ?>> INFOS = new ArrayList<>();

	static final List<EntityData> ALL_ENTITY_DATAS = new ArrayList<>();

	static void register() {
		Variables.yggdrasil.registerFieldHandler(new FieldHandler() {
			@Override
			public boolean excessiveField(Object object, FieldContext field) throws StreamCorruptedException {
				if (!(object instanceof EntityData<?> entityData))
					return false;
				if (field.getID().equals("matchedPattern")) {
					//noinspection DataFlowIssue
					entityData.groupIndex = (int) field.getObject();
					return true;
				}
				// Typically do not experience any drastic changes in field names or types
				// If this becomes a recurrent issue, should create a method that takes FieldContext
				//		and override on any necessary classes
				return false;
			}

			@Override
			public boolean missingField(Object object, Field field) {
                return object instanceof EntityData<?>;
            }

			@Override
			public boolean incompatibleField(Object object, Field field, FieldContext context) {
				return false;
			}
		});
	}

	//<editor-fold desc="Static Methods" defaultstate="collapsed">
	@Internal
	public static void onRegistrationStop() {
		INFOS.forEach(info -> {
			if (SimpleEntityData.class.equals(info.type())) {
				ALL_ENTITY_DATAS.addAll(
					info.dataPatterns().getPatternGroups().stream().map(group ->
						new SimpleEntityData((SimpleEntityDataInfo) group.data())
					).toList()
				);
			} else {
				ALL_ENTITY_DATAS.add(info.instance());
			}
		});
	}

	/**
	 * Whether the {@code entityClass} is already registered.
	 * @param entityClass The entity class to check.
	 * @return {@code true} if registered, otherwise {@code false}.
	 */
	public static boolean isRegistered(Class<? extends Entity> entityClass) {
		for (EntityDataInfo<?, ?> info : INFOS) {
			if (info.entityClass().equals(entityClass))
				return true;
		}
		return false;
	}

	/**
	 * Retrieves the {@link EntityDataInfo} registered for the given {@code entityDataClass}.
	 *
	 * @param entityDataClass The {@link EntityData} class to look up.
	 * @return The corresponding {@link EntityDataInfo} instance.
	 * @throws SkriptAPIException if the class has not been registered.
	 */
	public static EntityDataInfo<?, ?> getInfo(Class<? extends EntityData<?>> entityDataClass) {
		return INFOS.stream()
			.filter(dataInfo -> dataInfo.type() == entityDataClass)
			.findFirst()
			.orElseThrow(() -> new SkriptAPIException("Unregistered EntityData class " + entityDataClass.getName()));
	}

	/**
	 * Retrieves the {@link EntityDataInfo} associated with the given {@code codeName}.
	 *
	 * @param dataName The code name used to register the entity data.
	 * @return The corresponding {@link EntityDataInfo}, or {@code null} if not found.
	 */
	public static @Nullable EntityDataInfo<?, ?> getInfo(String dataName) {
		for (EntityDataInfo<?, ?> info : INFOS) {
			if (info.dataName().equals(dataName))
				return info;
		}
		return null;
	}

	/**
	 * Prints errors.
	 *
	 * @param string String with optional indefinite article at the beginning
	 * @return The parsed entity data
	 */
	public static @Nullable EntityData<?> parse(String string) {
		return SkriptParser.parseStatic(Noun.stripIndefiniteArticle(string), INFOS.iterator(), null);
	}

	/**
	 * Parses the provided {@code string} to an {@link EntityData}.
	 * Using this method implies that {@code string} does not contain an indefinite article.
	 *
	 * @param string The pattern to parse.
	 * @return The parsed entity data.
	 */
	public static @Nullable EntityData<?> parseWithoutIndefiniteArticle(String string) {
		Iterator<EntityDataInfo<EntityData<?>, ?>> it = new ArrayList<>(INFOS).iterator();
		return SkriptParser.parseStatic(string, it, null);
	}

	/**
	 * Gets all entities that match any of the provided {@code types} and is a subclass of {@code type}.
	 * Providing {@code worlds} only gets entities in those worlds, otherwise gets entities in all worlds.
	 * @param types The types of entities to retrieve.
	 * @param type The super type of the entities wanting to retrieve.
	 * @param worlds The worlds to get from, or null for all worlds.
	 * @return All entities of this type in the given worlds.
	 */
	@SuppressWarnings({"null", "unchecked"})
	public static <E extends Entity> E[] getAll(EntityData<?>[] types, Class<E> type, World @Nullable [] worlds) {
		assert types.length > 0;
		List<E> list = new ArrayList<>();
		if (worlds == null)
			worlds = Bukkit.getWorlds().toArray(new World[0]);
		for (World world : worlds) {
			for (E entity : world.getEntitiesByClass(type)) {
				for (EntityData<?> entityData : types) {
					if (entityData.isInstance(entity)) {
						list.add(entity);
						break;
					}
				}
			}
		}
		return list.toArray((E[]) Array.newInstance(type, list.size()));
	}

	/**
	 * Gets all entities that match any of the provided {@code types} and is a subclass of {@code type},
	 * within the provided {@code chunks}.
	 * @param types The types of entities to retrieve.
	 * @param type The super type of the entities wanting to retrieve.
	 * @param chunks The chunks to get from.
	 * @return All entities of this type in the given chunks.
	 */
	@SuppressWarnings("unchecked")
	public static <E extends Entity> E[] getAll(EntityData<?>[] types, Class<E> type, Chunk[] chunks) {
		assert types.length > 0;
		List<E> list = new ArrayList<>();
		for (Chunk chunk : chunks) {
			for (Entity entity : chunk.getEntities()) {
				for (EntityData<?> entityData : types) {
					if (entityData.isInstance(entity)) {
						list.add(((E) entity));
						break;
					}
				}
			}
		}
		return list.toArray((E[]) Array.newInstance(type, list.size()));
	}

	/**
	 * Internally resolves and returns an {@link EntityData} instance that best represents either
	 * a given {@link Entity} instance or its {@link Class}.
	 * <p>
	 *     Only one of {@code entityClass} or {@code entity} must be non-null.
	 *     This method looks through all registered {@link EntityDataInfo}s and selects the closest matching one
	 *     that successfully initializes from the provided input.
	 * </p>
	 *
	 * @param entityClass The class of the entity to represent, or {@code null} if using an instance.
	 * @param entity      The entity instance to represent, of {@code null} is using a class.
	 * @return An appropriate {@link EntityData} representing the input class or entity.
	 * 			If no registered data matches, a {@link SimpleEntityData} is returned as fallback.
	 */
	private static <E extends Entity> EntityData<? super E> getData(@Nullable Class<E> entityClass, @Nullable E entity) {
		assert entityClass == null ^ entity == null;
		assert entityClass == null || entityClass.isInterface();
		EntityDataInfo<?, ?> closestInfo = null;
		EntityData<E> closestData = null;
		for (EntityDataInfo<?, ?> info : INFOS) {
			if (info.entityClass() == Entity.class)
				continue;
			if (entity == null ? info.entityClass().isAssignableFrom(entityClass) : info.entityClass().isInstance(entity)) {
				EntityData<E> entityData = null;
				try {
					//noinspection unchecked
					entityData = (EntityData<E>) info.instance();
				} catch (Exception ignored) {}
				if (entityData != null && entityData.init(entityClass, entity)) {
					if (closestInfo == null || closestInfo.entityClass().isAssignableFrom(info.entityClass())) {
						closestInfo = info;
						closestData = entityData;
					}
				}
			}
		}
		if (closestInfo == null) {
			if (entity != null)
				return new SimpleEntityData(entity);
			return new SimpleEntityData(entityClass);
		}
		return closestData;
	}

	/**
	 * Creates an {@link EntityData} that represents the given entity class.
	 *
	 * @param entityClass The class of the entity (e.g. {@code Pig.class}).
	 * @return An {@link EntityData} representing the provided class.
	 */
	public static <E extends Entity> EntityData<? super E> fromClass(Class<E> entityClass) {
		return getData(entityClass, null);
	}

	/**
	 * Creates an {@link EntityData} that represents the given entity instance.
	 *
	 * @param entity The entity to represent.
	 * @return An {@link EntityData} representing the provided entity.
	 */
	public static <E extends Entity> EntityData<? super E> fromEntity(E entity) {
		return getData(null, entity);
	}

	/**
	 * Gets the string representation from the {@link EntityData} that handles the type of {@code entity}.
	 * @param entity The {@link Entity} to get the string representation of.
	 * @return The string representation.
	 */
	public static String toString(Entity entity) {
		return fromEntity(entity).getSuperType().toString();
	}

	/**
	 * Gets the string representation from the {@link EntityData} that handles the {@code entityclass}.
	 * @param entityClass The {@link Entity} class to get the string representation of.
	 * @return The string representation.
	 */
	public static String toString(Class<? extends Entity> entityClass) {
		return fromClass(entityClass).getSuperType().toString();
	}

	/**
	 * Gets the string representation from the {@link EntityData} that handles the type of {@code entity}.
	 * @param entity The {@link Entity} to get the string representation of.
	 * @param flags The flag to determine singular or plural.
	 * @return The string representation.
	 */
	public static String toString(Entity entity, int flags) {
		return fromEntity(entity).getSuperType().toString(flags);
	}

	/**
	 * Gets the string representation from the {@link EntityData} that handles the {@code entityclass}.
	 * @param entityClass The {@link Entity} class to get the string representation of.
	 * @param flags The flag to determine singular or plural.
	 * @return The string representation.
	 */
	public static String toString(Class<? extends Entity> entityClass, int flags) {
		return fromClass(entityClass).getSuperType().toString(flags);
	}
	//</editor-fold>

	/**
	 * Helper method for getting a newly constructed {@link Builder}.
	 * @param dataClass The class extending {@link EntityData}.
	 * @param dataName The name {@code dataClass} will be identified as.
	 * @return The constructed {@link Builder}.
	 * @param <Data> The entity data class being used.
	 * @param <E> The entity class {@code Data} is assigned to.
	 */
	protected static <Data extends EntityData<E>, E extends Entity> Builder<? extends Builder<?, Data, E>, Data, E> infoBuilder(
		Class<Data> dataClass,
		String dataName
	) {
		return new EntityDataInfoImpl.BuilderImpl<>(dataClass, dataName);
	}

	/**
	 * Register a {@link EntityDataInfo}.
	 * @param info The {@link EntityDataInfo} to register.
	 * @param <Data> The entity data class being used.
	 * @param <E> The entity class {@code Data} is assigned to.
	 */
	@SuppressWarnings("unchecked")
	protected static <Data extends EntityData<E>, E extends Entity> void registerInfo(EntityDataInfo<Data, E> info) {
		for (int i = 0; i < INFOS.size(); i++) {
			if (INFOS.get(i).entityClass().isAssignableFrom(info.entityClass())) {
				INFOS.add(i, (EntityDataInfo<EntityData<?>, ?>) info);
				return;
			}
		}
		INFOS.add((EntityDataInfo<EntityData<?>, ?>) info);
	}

	transient EntityDataInfo<?, ?> info;

	/**
	 * References the corresponding group in the order they're registered.
	 */
	protected int groupIndex;
	private Kleenean plural = Kleenean.UNKNOWN;
	private Kleenean baby = Kleenean.UNKNOWN;

	public EntityData() {
		for (EntityDataInfo<?, ?> info : INFOS) {
			if (getClass() == info.type()) {
				this.info = info;
				groupIndex = info.defaultGroupIndex();
				return;
			}
		}
		throw new IllegalStateException();
	}

	/**
	 * Performs initial setup for this {@link EntityData} before passing control to the more specific {@link #init(Expression[], int, Kleenean, ParseResult)}.
	 * <p>
	 *     This method handles common behaviors such as tracking plurality (e.g. "a pig" vs "all pigs")
	 *     and entity age (e.g. "baby zombie") based on the {@link ParseResult}'s marker value.
	 * </p>
	 *
	 * {@inheritDoc}
	 */
	@Override
	public final boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		this.plural = parseResult.hasTag("unknown_plural") ? Kleenean.UNKNOWN : Kleenean.get(parseResult.hasTag("plural"));
		if (parseResult.hasTag("baby")) {
			this.baby = Kleenean.TRUE;
		} else if (parseResult.hasTag("adult")) {
			this.baby = Kleenean.FALSE;
		} else {
			this.baby = Kleenean.UNKNOWN;
		}
		int matchedGroup = info.groupFromMatchedPattern(matchedPattern).index();
		int patternInGroup = info.matchedGroupPattern(matchedPattern);
		this.groupIndex = matchedGroup;
		return init(Arrays.copyOf(exprs, exprs.length, Literal[].class), matchedGroup, patternInGroup, parseResult);
	}

	/**
	 * Initializes this {@link EntityData}.
	 * <p>
	 *     As of Skript 2.13, code names can have multiple patterns.
	 *     {@code matchedGroup} will be the index of the code name the matched pattern is linked to.
	 *     		(e.g. {@link PigData} "unsaddled pig" = 0, "pig" = 1, "saddled pig" = 2)
	 *     {@code matchedPattern} will be the index of the pattern used from the patterns of the group.
	 * </p>
	 *
	 * @param exprs An array of {@link Literal} expressions from the matched pattern, in the order they appear.
	 *              If an optional value was omitted by the user, it will still be present in the array
	 *              with a value of {@code null}.
	 * @param matchedGroup The index of the group which matched.
	 * @param matchedPattern The index of the pattern of the group which matched.
	 * @param parseResult Additional information from the parser.
	 * @return {@code true} if initialization was successful, otherwise {@code false}.
	 */
	protected abstract boolean init(
		Literal<?>[] exprs,
		int matchedGroup,
		int matchedPattern,
		ParseResult parseResult
	);

	/**
	 * Initializes this {@link EntityData} from either an entity class or a specific {@link Entity}.
	 * <p>
	 *     Example usage:
	 *     	<pre>
	 *     	    <code>
	 *     	        spawn a pig at location(0, 0, 0):
	 *     	        	set {_entity} to event-entity
	 *     	        spawn {_entity} at location(0, 0, 0)
	 *     	    </code>
	 *     	</pre>
	 * </p>
	 * @param entityClass An entity's class, e.g. Player
	 * @param entity An actual entity, or null to get an entity data for an entity class
	 * @return {@code true} if initialization was successful, otherwise {@code false}.
	 */
	protected abstract boolean init(@Nullable Class<? extends E> entityClass, @Nullable E entity);

	/**
	 * Applies this {@link EntityData} to a newly spawned {@link Entity}.
	 * <p>
	 *     This is used during entity spawning to set additional data, such as a saddled pig.
	 * </p>
	 * @param entity The spawned entity.
	 */
	public abstract void set(E entity);

	/**
	 * Determines whether the given {@link Entity} matches this {@link EntityData} data.
	 * <p>
	 *     For example:
	 *     <pre>
	 *         <code>
	 *             spawn a pig at location(0, 0, 0):
	 *             		set {_entity} to event-entity
	 *             	if {_entity} is a pig:          # will pass
	 *             	if {_entity} is a saddled pig:  # will not pass
	 *         </code>
	 *     </pre>
	 * </p>
	 * @param entity The {@link Entity} to match.
	 * @return {@code true} if the entity matches, otherwise {@code false}.
	 */
	protected abstract boolean match(E entity);

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
	public final String toString() {
		return toString(0);
	}

	/**
	 * @return The name registered for this {@link EntityData}.
	 */
	protected GeneralNoun getName() {
		return info.dataPatterns().getName(groupIndex);
	}

	/**
	 * @return The age this {@link EntityData} represents.
	 */
	protected @Nullable GeneralNoun getAgeNoun() {
		if (baby.isTrue()) {
			return AGE_BABY;
		} else if (baby.isFalse()) {
			return AGE_ADULT;
		}
		return null;
	}

	/**
	 * Gets the string representation of this {@link EntityData}.
	 * @param flags The flag to determine singular or plural.
	 * @return The string representation.
	 */
	public String toString(int flags) {
		GeneralNoun name = getName();
		if (baby.isTrue()) {
			return AGE_BABY.toString(name, flags);
		} else if (baby.isFalse()) {
			return AGE_ADULT.toString(name, flags);
		}
		return name.toString(flags);
	}

	/**
	 * @return {@link Kleenean} determining whether this {@link EntityData} is representing plurality.
	 */
	public Kleenean isPlural() {
		return plural;
	}

	/**
	 * @return {@link Kleenean} determining whether this {@link EntityData} is representing baby type.
	 */
	public Kleenean isBaby() {
		return baby;
	}

	/**
	 * Internal method used by {@link #hashCode()} to include subclass-specific fields in the hash calculation
	 * for this {@link EntityData}.
	 *
	 * @return A hash code representing subclass-specific data.
	 */
	protected abstract int hashCode_i();

	@Override
	public final int hashCode() {
		int prime = 31;
		int result = 1;
		result = prime * result + baby.hashCode();
		result = prime * result + plural.hashCode();
		result = prime * result + groupIndex;
		result = prime * result + info.hashCode();
		result = prime * result + hashCode_i();
		return result;
	}

	/**
	 * Internal helper for {@link #equals(Object)} to compare the specific data
	 * of this {@link EntityData} with another.
	 *
	 * @param entityData The {@link EntityData} to compare with.
	 * @return {@code true} if the data is considered equal, otherwise {@code false}.
	 */
	protected abstract boolean equals_i(EntityData<?> entityData);

	@Override
	public final boolean equals(@Nullable Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof EntityData other))
			return false;
		if (baby != other.baby)
			return false;
		if (plural != other.plural)
			return false;
		if (groupIndex != other.groupIndex)
			return false;
		if (!info.equals(other.info))
			return false;
		return equals_i(other);
	}

	/**
	 * Applies common data of this {@link EntityData} to a newly spawned {@link Entity}.
	 * <p>
	 *     This is used during entity spawning to set additional data, such as a baby pig.
	 *     Proceeded by {@link #set(Entity)} for other data determined by each implementation.
	 * </p>
	 * @param entity The spawned entity.
	 */
	private E apply(E entity) {
		if (baby.isTrue()) {
			EntityUtils.setBaby(entity);
		} else if (baby.isFalse()) {
			EntityUtils.setAdult(entity);
		}
		set(entity);
		return entity;
	}

	/**
	 * Checks whether this entity type is allowed to spawn in the given {@link World}.
	 * <p>
	 *     Some entity types may be restricted from spawning due to experimental datapacks.
	 * </p>
	 *
	 * @param world The world to check spawning permissions in.
	 * @return {@code true} if the entity can be spawned in the given world, or in general if world is {@code null}; otherwise {@code false}.
	 */
	public boolean canSpawn(@Nullable World world) {
		if (world == null)
			return false;
		EntityType bukkitEntityType = info.entityType() != null ? info.entityType() : EntityUtils.toBukkitEntityType(this);
		if (bukkitEntityType == null || !bukkitEntityType.isSpawnable())
			return false;
		if (HAS_ENABLED_BY_FEATURE) {
			// Check if the entity can actually be spawned
			// Some entity types may be restricted by experimental datapacks
			assert ENABLED_BY_FEATURE_METHOD != null;
			try {
				return (boolean) ENABLED_BY_FEATURE_METHOD.invoke(bukkitEntityType, world);
			} catch (IllegalAccessException | InvocationTargetException e) {
				return false;
			}
		}
		assert IS_ENABLED_METHOD != null;
		try {
			return (boolean) IS_ENABLED_METHOD.invoke(world, bukkitEntityType);
		} catch (IllegalAccessException | InvocationTargetException e) {
			return false;
		}
	}

	/**
	 * Spawn this entity data at a location.
	 *
	 * @param location The {@link Location} to spawn the entity at.
	 * @return The Entity object that is spawned.
	 */
	public final @Nullable E spawn(Location location) {
		return spawn(location, null);
	}

	/**
	 * Spawn this entity data at a location.
	 * The consumer allows for modification to the entity before it actually gets spawned.
	 *
	 * @param location The {@link Location} to spawn the entity at.
	 * @param consumer A {@link Consumer} to apply the entity changes to.
	 * @return The Entity object that is spawned.
	 */
	public @Nullable E spawn(Location location, @Nullable Consumer<E> consumer) {
		assert location != null;
		World world = location.getWorld();
		if (!canSpawn(world))
			return null;
		if (consumer != null) {
			return EntityData.spawn(location, getType(), e -> consumer.accept(this.apply(e)));
		} else {
			return apply(world.spawn(location, getType()));
		}
	}

	/**
	 * Gets all entities that match this {@link EntityData} via {@link #match(Entity)}, in the provided {@code worlds}.
	 * @param worlds The {@link World}s to get entities from.
	 * @return The entities found.
	 */
	@SuppressWarnings("unchecked")
	public E[] getAll(World... worlds) {
		assert worlds != null && worlds.length > 0 : Arrays.toString(worlds);
		List<E> list = new ArrayList<>();
		for (World world : worlds) {
			for (E entity : world.getEntitiesByClass(getType())) {
				if (match(entity))
					list.add(entity);
			}
		}
		return list.toArray((E[]) Array.newInstance(getType(), list.size()));
	}

	/**
	 * Whether the provided {@code entity} is an instance of the entity class and matches,
	 * @param entity The entity to check.
	 * @return {@code true} if instance and matches, otherwise {@code false}.
	 */
	@SuppressWarnings("unchecked")
	public final boolean isInstance(@Nullable Entity entity) {
		if (entity == null)
			return false;
		if (!baby.isUnknown() && entity instanceof Ageable && EntityUtils.isAdult(entity) != baby.isFalse())
			return false;
		return getType().isInstance(entity) && match((E) entity);
	}

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

	@Internal
	@Override
	@Deprecated(forRemoval = true, since = "INSERT VERSION")
	public boolean isSupertypeOf(ch.njol.skript.entity.EntityData<?> entityData) {
		return isSupertypeOf((EntityData<?>) entityData);
	}

	@Override
	public Fields serialize() throws NotSerializableException {
		return new Fields(this);
	}

	@Override
	public void deserialize(Fields fields) throws StreamCorruptedException, NotSerializableException {
		fields.setFields(this);
	}

	@Override
	public @NotNull String getSyntaxTypeName() {
		return "entity data";
	}

	protected static <E extends Entity> @Nullable E spawn(Location location, Class<E> type, Consumer<E> consumer) {
		World world = location.getWorld();
		if (world == null)
			return null;
		return world.spawn(location, type, consumer);
	}

	/**
	 * Creates an entity in the server but does not spawn it
	 *
	 * @return The created entity
	 */
	public @Nullable E create() {
		Location location = Bukkit.getWorlds().get(0).getSpawnLocation();
		return create(location);
	}

	/**
	 * Creates an entity at the provided location, but does not spawn it
	 * NOTE: If {@link RegionAccessor#createEntity(Location, Class)} does not exist, will return {@link #spawn(Location)}
	 * @param location The {@link Location} to create the entity at
	 * @return The created entity
	 */
	public @Nullable E create(Location location) {
		if (!Skript.methodExists(RegionAccessor.class, "createEntity"))
			return spawn(location);
		return create(location, getType());
	}

	protected static <E extends Entity> @Nullable E create(Location location, Class<E> type) {
		World world = location.getWorld();
		if (world == null)
			return null;
		return world.createEntity(location, type);
	}

	/**
	 * Checks if {@code from} is {@link Kleenean#UNKNOWN} or is equal to {@code to}.
	 *
	 * @param from The {@link Kleenean} to compare to.
	 * @param to The {@link boolean} to compare against.
	 * @return {@code true} if {@code from} is {@link Kleenean#UNKNOWN} or is equal to {@code to}, otherwise {@code false}.
	 */
	protected boolean kleeneanMatch(Kleenean from, boolean to) {
		return kleeneanMatch(from, Kleenean.get(to));
	}

	/**
	 * Checks if {@code from} is {@link Kleenean#UNKNOWN} or is equal to {@code to}.
	 *
	 * @param from The {@link Kleenean} to compare to.
	 * @param to The {@link Kleenean} to compare against.
	 * @return {@code true} if {@code from} is {@link Kleenean#UNKNOWN} or is equal to {@code to}, otherwise {@code false}.
	 */
	protected boolean kleeneanMatch(Kleenean from, Kleenean to) {
		if (from.isUnknown())
			return true;
		return from == to;
	}

	/**
	 * Checks if {@code from} is {@code null} or is equal to {@code to}.
	 *
	 * @param from The object to compare to.
	 * @param to The object to compare against.
	 * @return {@code true} if {@code from} is {@code null} or is equal to {@code to}, otherwise {@code false}.
	 */
	protected <T> boolean dataMatch(@Nullable T from, T to) {
		if (from == null)
			return true;
		return from == to;
	}

	/**
	 * Collection of {@link PatternGroup}s to be parsed and retrieved.
	 * @param <Data> The type of object representing the state(s) of the entities in the {@link PatternGroup}s.
	 */
	public static class EntityDataPatterns<Data> {

		/**
		 * Create a simple {@link EntityDataPatterns} with only one {@link PatternGroup} from the provided parameters.
		 * @param name The name of the entity.
		 * @param patterns The patterns that refer to the entity.
		 * @return The constructed {@link EntityDataPatterns}.
		 */
		public static EntityDataPatterns<?> single(String name, String... patterns) {
			return single(name, null, patterns);
		}

		/**
		 * Create a simple {@link EntityDataPatterns} with only one {@link PatternGroup} from the provided parameters.
		 * @param name The name of the entity.
		 * @param data The data correlating to the entity.
		 * @param patterns The patterns that refer to the entity.
		 * @return The constructed {@link EntityDataPatterns}.
		 * @param <Data> The type of data correlating to the entity.
		 */
		public static <Data> EntityDataPatterns<Data> single(String name, @Nullable Data data, String... patterns) {
			return new EntityDataPatterns<>(
				new PatternGroup<>(0, name, data, patterns)
			);
		}

		private final SequencedCollection<PatternGroup<Data>> patternGroups;
		private final Map<Integer, PatternGroup<Data>> groupMap = new HashMap<>();
		private final Map<Data, PatternGroup<Data>> dataMap = new HashMap<>();
		private PatternGroup<Data> genericGroup;
		private final SequencedCollection<GeneralNoun> names = new ArrayList<>();

		@SafeVarargs
		public EntityDataPatterns(PatternGroup<Data>... patternGroups) {
			this.patternGroups = List.of(patternGroups);
			for (PatternGroup<Data> group : patternGroups) {
				groupMap.put(group.index(), group);
				dataMap.put(group.data(), group);
				names.add(group.name());
				if (group.data() == null)
					genericGroup = group;
			}
		}

		/**
		 * Gets the generic group. A group is considered generic when it does not correlate to any {@code Data}.
		 * @return The generic group.
		 */
		public PatternGroup<Data> getGenericGroup() {
			return genericGroup;
		}

		/**
		 * Gets the {@link GeneralNoun}s from all the {@link PatternGroup}s.
		 * @return The {@link GeneralNoun}s.
		 */
		public SequencedCollection<GeneralNoun> getNames() {
			return names;
		}

		/**
		 * Gets the {@link PatternGroup}s provided when constructed.
		 * @return The {@link PatternGroup}s.
		 */
		public SequencedCollection<PatternGroup<Data>> getPatternGroups() {
			return patternGroups;
		}

		/**
		 * Gets the {@link Map} that correlates index to a {@link PatternGroup}.
		 * @return The {@link Map}.
		 */
		protected Map<Integer, PatternGroup<Data>> getGroupMap() {
			return groupMap;
		}

		/**
		 * Gets the {@link Map} that correlates {@code Data} to a {@link PatternGroup}.
		 * @return The {@link Map}.
		 */
		protected Map<Data, PatternGroup<Data>> getDataMap() {
			return dataMap;
		}

		/**
		 * Gets the {@link PatternGroup} correlating to {@code index}.
		 * @param index The index of the {@link PatternGroup} to grab.
		 * @return The {@link PatternGroup} if found, otherwise the generic group.
		 */
		public PatternGroup<Data> getPatternGroup(int index) {
			if (!getGroupMap().containsKey(index))
				return getGenericGroup();
			return getGroupMap().get(index);
		}

		/**
		 * Gets the {@link PatternGroup} correlating to {@code data}.
		 * @param data The data of the {@link PatternGroup} to grab.
		 * @return The {@link PatternGroup} if found, otherwise the generic group.
		 */
		public PatternGroup<Data> getPatternGroup(@Nullable Data data) {
			if (!getDataMap().containsKey(data))
				return getGenericGroup();
			return getDataMap().get(data);
		}

		/**
		 * Gets the index that the {@link PatternGroup} with {@code data}.
		 * @param data The data of the {@link PatternGroup} to grab the index from.
		 * @return The index.
		 */
		public int getIndex(@Nullable Data data) {
			return getPatternGroup(data).index();
		}

		/**
		 * Gets the data of the {@link PatternGroup} at the {@code index}.
		 * @param index The index of the {@link PatternGroup} to grab the data from.
		 * @return The data.
		 */
		public @Nullable Data getData(int index) {
			return getPatternGroup(index).data();
		}

		/**
		 * Gets the {@link GeneralNoun} of the {@link PatternGroup} at the {@code index}.
		 * @param index The index of {@link PatternGroup} to grab the {@link GeneralNoun} from.
		 * @return The {@link GeneralNoun}.
		 */
		public GeneralNoun getName(int index) {
			return getPatternGroup(index).name();
		}

		/**
		 * Gets the {@link GeneralNoun} of the {@link PatternGroup} with {@code data}.
		 * @param data The data of the {@link PatternGroup} to grab the {@link GeneralNoun} from.
		 * @return The {@link GeneralNoun}.
		 */
		public GeneralNoun getName(@Nullable Data data) {
			return getPatternGroup(data).name();
		}

	}

	/**
	 * Grouping of data for an entity that is to be parsed and retrieved.
	 * @param index The index of the entity/this {@link PatternGroup}.
	 * @param name The name of the entity.
	 * @param data The object representing a state of the entity.
	 * @param patterns The patterns that could be used to refer to the entity/this {@link PatternGroup}.
	 * @param <Data> The object for representing a state of the entity.
	 */
	public record PatternGroup<Data>(int index, GeneralNoun name, @Nullable Data data, String... patterns) {

		public PatternGroup(int index, GeneralNoun name, String... patterns) {
			this(index, name, null, patterns);
		}

		public PatternGroup(int index, String name, @Nullable Data data, String... patterns) {
			this(index, new GeneralNoun(name), data, patterns);
		}

		public PatternGroup(int index, String name, String... patterns) {
			this(index, name, null, patterns);
		}

	}

}
