package ch.njol.skript.entity;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.bukkitutil.EntityUtils;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.classes.Serializer;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxElement;
import ch.njol.skript.lang.SyntaxElementInfo;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.localization.Adjective;
import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.Language.LanguageListenerPriority;
import ch.njol.skript.localization.LanguageChangeListener;
import ch.njol.skript.localization.Message;
import ch.njol.skript.localization.Noun;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import ch.njol.util.coll.iterator.SingleItemIterator;
import ch.njol.yggdrasil.Fields;
import ch.njol.yggdrasil.YggdrasilSerializable.YggdrasilExtendedSerializable;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.RegionAccessor;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.NotSerializableException;
import java.io.StreamCorruptedException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@SuppressWarnings("rawtypes")
public abstract class EntityData<E extends Entity> implements SyntaxElement, YggdrasilExtendedSerializable {// TODO extended horse support, zombie villagers // REMIND unit

	/*
	 * In 1.20.2 Spigot deprecated org.bukkit.util.Consumer.
	 * From the class header: "API methods which use this consumer will be remapped to Java's consumer at runtime, resulting in an error."
	 * But in 1.13-1.16 the only way to use a consumer was World#spawn(Location, Class, org.bukkit.util.Consumer).
	 */
	protected static @Nullable Method WORLD_1_17_CONSUMER_METHOD;
	protected static boolean WORLD_1_17_CONSUMER;

	static {
		try {
			if (WORLD_1_17_CONSUMER = Skript.methodExists(RegionAccessor.class, "spawn", Location.class, Class.class, org.bukkit.util.Consumer.class))
				WORLD_1_17_CONSUMER_METHOD = RegionAccessor.class.getDeclaredMethod("spawn", Location.class, Class.class, org.bukkit.util.Consumer.class);
		} catch (NoSuchMethodException | SecurityException ignored) { /* We already checked if the method exists */ }
	}

	private static final boolean HAS_ENABLED_BY_FEATURE = Skript.methodExists(EntityType.class, "isEnabledByFeature", World.class);
	public final static String LANGUAGE_NODE = "entities";

	public final static Message m_age_pattern = new Message(LANGUAGE_NODE + ".age pattern");
	public final static Adjective m_baby = new Adjective(LANGUAGE_NODE + ".age adjectives.baby"),
			m_adult = new Adjective(LANGUAGE_NODE + ".age adjectives.adult");

	// must be here to be initialised before 'new SimpleLiteral' is called in the register block below
	private final static List<EntityDataInfo<EntityData<?>>> infos = new ArrayList<>();

	private static final List<EntityData> ALL_ENTITY_DATAS = new ArrayList<>();

	public static Serializer<EntityData> serializer = new Serializer<EntityData>() {
		@Override
		public Fields serialize(EntityData entityData) throws NotSerializableException {
			Fields fields = entityData.serialize();
			fields.putObject("codeName", entityData.info.codeName);
			return fields;
		}

		@Override
		public boolean canBeInstantiated() {
			return false;
		}

		@Override
		public void deserialize(EntityData entityData, Fields fields) throws StreamCorruptedException {
			assert false;
		}

		@Override
		protected EntityData deserialize(Fields fields) throws StreamCorruptedException, NotSerializableException {
			String codeName = fields.getAndRemoveObject("codeName", String.class);
			if (codeName == null)
				throw new StreamCorruptedException();
			EntityDataInfo<?> info = getInfo(codeName);
			if (info == null)
				throw new StreamCorruptedException("Invalid EntityData code name " + codeName);
			try {
				EntityData<?> entityData = info.getElementClass().newInstance();
				entityData.deserialize(fields);
				return entityData;
			} catch (InstantiationException | IllegalAccessException e) {
				Skript.exception(e);
			}
			throw new StreamCorruptedException();
		}

//		return getInfo((Class<? extends EntityData<?>>) d.getClass()).codeName + ":" + d.serialize();
		@SuppressWarnings("null")
		@Override
		@Deprecated(since = "2.3.0", forRemoval = true)
		public @Nullable EntityData deserialize(String string) {
			String[] split = string.split(":", 2);
			if (split.length != 2)
				return null;
			EntityDataInfo<?> entityDataInfo = getInfo(split[0]);
			if (entityDataInfo == null)
				return null;
			EntityData<?> entityData;
			try {
				entityData = entityDataInfo.getElementClass().newInstance();
			} catch (Exception e) {
				Skript.exception(e, "Can't create an instance of " + entityDataInfo.getElementClass().getCanonicalName());
				return null;
			}
			if (!entityData.deserialize(split[1]))
				return null;
			return entityData;
		}

		@Override
		public boolean mustSyncDeserialization() {
			return false;
		}
	};

	static {
		Classes.registerClass(new ClassInfo<>(EntityData.class, "entitydata")
				.user("entity ?types?")
				.name("Entity Type")
				.description("The type of an <a href='#entity'>entity</a>, e.g. player, wolf, powered creeper, etc.")
				.usage("<i>Detailed usage will be added eventually</i>")
				.examples("victim is a cow",
						"spawn a creeper")
				.since("1.3")
				.defaultExpression(new SimpleLiteral<EntityData>(new SimpleEntityData(Entity.class), true))
				.before("entitytype")
				.supplier(ALL_ENTITY_DATAS::iterator)
				.parser(new Parser<EntityData>() {
					@Override
					public String toString(EntityData entityData, int flags) {
						return entityData.toString(flags);
					}

					@Override
					public @Nullable EntityData parse(String string, ParseContext context) {
						return EntityData.parse(string);
					}

					@Override
					public String toVariableNameString(EntityData entityData) {
						return "entitydata:" + entityData.toString();
					}
				}).serializer(serializer));
	}

	public static void onRegistrationStop() {
		infos.forEach(info -> {
			if (SimpleEntityData.class.equals(info.getElementClass())) {
				ALL_ENTITY_DATAS.addAll(Arrays.stream(info.codeNames)
					.map(input -> SkriptParser.parseStatic(input, new SingleItemIterator<>(info), null))
					.collect(Collectors.toList())
				);
			} else {
				ALL_ENTITY_DATAS.add(SkriptParser.parseStatic(info.codeName, new SingleItemIterator<>(info), null));
			}
		});
	}

	private final static class EntityDataInfo<T extends EntityData<?>> extends SyntaxElementInfo<T>
		implements LanguageChangeListener {

		final String codeName;
		final String[] codeNames;
		final int defaultName;
		final @Nullable EntityType entityType;
		final Class<? extends Entity> entityClass;
		final Noun[] names;

		public EntityDataInfo(Class<T> dataClass, String codeName, String[] codeNames, int defaultName, Class<? extends Entity> entityClass) {
			this(dataClass, codeName, codeNames, defaultName, EntityUtils.toBukkitEntityType(entityClass), entityClass);
		}

		public EntityDataInfo(
			Class<T> dataClass,
			String codeName,
			String[] codeNames,
			int defaultName,
			@Nullable EntityType entityType,
			Class<? extends Entity> entityClass
		) {
			super(new String[codeNames.length], dataClass, dataClass.getName());
			assert codeName != null && entityClass != null && codeNames.length > 0;
			this.codeName = codeName;
			this.codeNames = codeNames;
			this.defaultName = defaultName;
			this.entityType = entityType;
			this.entityClass = entityClass;
			this.names = new Noun[codeNames.length];
			for (int i = 0; i < codeNames.length; i++) {
				assert codeNames[i] != null;
				names[i] = new Noun(LANGUAGE_NODE + "." + codeNames[i] + ".name");
			}

			Language.addListener(this, LanguageListenerPriority.LATEST); // will initialise patterns, LATEST to make sure that m_age_pattern is updated before this
		}

		@Override
		public void onLanguageChange() {
			for (int i = 0; i < codeNames.length; i++)
				patterns[i] = Language.get(LANGUAGE_NODE + "." + codeNames[i] + ".pattern").replace("<age>", m_age_pattern.toString());
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(codeName);
		}

		@Override
		public boolean equals(@Nullable Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (!(obj instanceof EntityDataInfo other))
				return false;
			if (!codeName.equals(other.codeName))
				return false;
			assert Arrays.equals(codeNames, other.codeNames);
			assert defaultName == other.defaultName;
			assert entityClass == other.entityClass;
			return true;
		}

	}

	public static <E extends Entity, T extends EntityData<E>> void register(
		Class<T> dataClass,
		String name,
		Class<E> entityClass,
		String codeName
	) throws IllegalArgumentException {
		register(dataClass, name, entityClass, 0, codeName);
	}

	public static <E extends Entity, T extends EntityData<E>> void register(
		Class<T> dataClass,
		String name,
		Class<E> entityClass,
		int defaultName,
		String... codeNames
	) throws IllegalArgumentException {
		EntityType entityType = EntityUtils.toBukkitEntityType(entityClass);
		EntityDataInfo<T> entityDataInfo = new EntityDataInfo<>(dataClass, name, codeNames, defaultName, entityType, entityClass);
		for (int i = 0; i < infos.size(); i++) {
			if (infos.get(i).entityClass.isAssignableFrom(entityClass)) {
				//noinspection unchecked
				infos.add(i, (EntityDataInfo<EntityData<?>>) entityDataInfo);
				return;
			}
		}
		//noinspection unchecked
		infos.add((EntityDataInfo<EntityData<?>>) entityDataInfo);
	}

	transient EntityDataInfo<?> info;
	protected int matchedPattern = 0;
	private Kleenean plural = Kleenean.UNKNOWN;
	private Kleenean baby = Kleenean.UNKNOWN;

	public EntityData() {
		for (EntityDataInfo<?> info : infos) {
			if (getClass() == info.getElementClass()) {
				this.info = info;
				matchedPattern = info.defaultName;
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
		this.matchedPattern = matchedPattern;
		this.plural = parseResult.hasTag("unknown_plural") ? Kleenean.UNKNOWN : Kleenean.get(parseResult.hasTag("plural"));
		this.baby = parseResult.hasTag("unknown_age") ? Kleenean.UNKNOWN : Kleenean.get(parseResult.hasTag("baby"));
		return init(Arrays.copyOf(exprs, exprs.length, Literal[].class), matchedPattern, parseResult);
	}

	/**
	 * Initializes this {@link EntityData} from the matched pattern and its associated literals.
	 * <p>
	 *     This is used when parsing entity data from user-written patterns such as "a saddled pig".
	 * </p>
	 * @param exprs An array of {@link Literal} expressions from the matched pattern, in the order they appear.
	 *              If an optional value was omitted by the user, it will still be present in the array
	 *              with a value of {@code null}.
	 * @param matchedPattern The index of the pattern which matched.
	 * @param parseResult Additional information from the parser.
	 * @return {@code true} if initialization was successful, otherwise {@code false}.
	 */
	protected abstract boolean init(Literal<?>[] exprs, int matchedPattern, ParseResult parseResult);

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
	 *     For example, calling this on {@code "a saddled pig"} would return {@code "a pig"}.
	 *     This is typically used to obtain the base entity type without any modifiers or traits.
	 * </p>
	 *
	 * @return A generalized {@link EntityData} representing the base entity type.
	 */
	public abstract @NotNull EntityData getSuperType();

	@Override
	public final String toString() {
		return toString(0);
	}

	@SuppressWarnings("null")
	protected Noun getName() {
		return info.names[matchedPattern];
	}

	protected @Nullable Adjective getAgeAdjective() {
		return baby.isTrue() ? m_baby : baby.isFalse() ? m_adult : null;
	}

	@SuppressWarnings("null")
	public String toString(int flags) {
		Noun name = info.names[matchedPattern];
		return baby.isTrue() ? m_baby.toString(name, flags) : baby.isFalse() ? m_adult.toString(name, flags) : name.toString(flags);
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
		result = prime * result + matchedPattern;
		result = prime * result + info.hashCode();
		result = prime * result + hashCode_i();
		return result;
	}

	/**
	 * Internal helper for {@link #equals(Object)} to compare the specific data
	 * of this {@link EntityData} with another.
	 *
	 * @param obj The {@link EntityData} to compare with.
	 * @return {@code true} if the data is considered equal, otherwise {@code false}.
	 */
	protected abstract boolean equals_i(EntityData<?> obj);

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
		if (matchedPattern != other.matchedPattern)
			return false;
		if (!info.equals(other.info))
			return false;
		return equals_i(other);
	}

	/**
	 * Retrieves the {@link EntityDataInfo} registered for the given {@code entityDataClass}.
	 *
	 * @param entityDataClass The {@link EntityData} class to look up.
	 * @return The corresponding {@link EntityDataInfo} instance.
	 * @throws SkriptAPIException if the class has not been registered.
	 */
	public static EntityDataInfo<?> getInfo(Class<? extends EntityData<?>> entityDataClass) {
		for (EntityDataInfo<?> info : infos) {
			if (info.getElementClass() == entityDataClass)
				return info;
		}
		throw new SkriptAPIException("Unregistered EntityData class " + entityDataClass.getName());
	}

	/**
	 * Retrieves the {@link EntityDataInfo} associated with the given {@code codeName}.
	 *
	 * @param codeName The code name used to register the entity data.
	 * @return The corresponding {@link EntityDataInfo}, or {@code null} if not found.
	 */
	public static @Nullable EntityDataInfo<?> getInfo(String codeName) {
		for (EntityDataInfo<?> info : infos) {
			if (info.codeName.equals(codeName))
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
	@SuppressWarnings("null")
	public static @Nullable EntityData<?> parse(String string) {
		Iterator<EntityDataInfo<EntityData<?>>> it = new ArrayList<>(infos).iterator();
		return SkriptParser.parseStatic(Noun.stripIndefiniteArticle(string), it, null);
	}

	/**
	 * Prints errors.
	 *
	 * @param string
	 * @return The parsed entity data
	 */
	public static @Nullable EntityData<?> parseWithoutIndefiniteArticle(String string) {
		Iterator<EntityDataInfo<EntityData<?>>> it = new ArrayList<>(infos).iterator();
		return SkriptParser.parseStatic(string, it, null);
	}

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
	@SuppressWarnings({"removal"})
	public boolean canSpawn(@Nullable World world) {
		if (world == null)
			return false;
		EntityType bukkitEntityType = info.entityType != null ? info.entityType : EntityUtils.toBukkitEntityType(this);
		if (bukkitEntityType == null)
			return false;
		if (HAS_ENABLED_BY_FEATURE) {
			// Check if the entity can actually be spawned
			// Some entity types may be restricted by experimental datapacks
			return bukkitEntityType.isEnabledByFeature(world) && bukkitEntityType.isSpawnable();
		}
		return bukkitEntityType.isSpawnable();
	}

	/**
	 * Spawn this entity data at a location.
	 *
	 * @param location The {@link Location} to spawn the entity at.
	 * @return The Entity object that is spawned.
	 */
	public final @Nullable E spawn(Location location) {
		return spawn(location, (Consumer<E>) null);
	}

	/**
	 * Spawn this entity data at a location.
	 * The consumer allows for modification to the entity before it actually gets spawned.
	 * <p>
	 * Bukkit's own {@link org.bukkit.util.Consumer} is deprecated.
	 * Use {@link #spawn(Location, Consumer)}
	 *
	 * @param location The {@link Location} to spawn the entity at.
	 * @param consumer A {@link Consumer} to apply the entity changes to.
	 * @return The Entity object that is spawned.
	 */
	@Deprecated(since = "2.8.0", forRemoval = true)
	@SuppressWarnings("deprecation")
	public @Nullable E spawn(Location location, org.bukkit.util.@Nullable Consumer<E> consumer) {
		return spawn(location, (Consumer<E>) e -> consumer.accept(e));
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
	 * @param types
	 * @param type
	 * @param worlds worlds or null for all
	 * @return All entities of this type in the given worlds
	 */
	@SuppressWarnings({"null", "unchecked"})
	public static <E extends Entity> E[] getAll(EntityData<?>[] types, Class<E> type, World @Nullable [] worlds) {
		assert types.length > 0;
		if (type == Player.class) {
			if (worlds == null)
				return (E[]) Bukkit.getOnlinePlayers().toArray(new Player[0]);
			List<Player> list = new ArrayList<>();
			for (Player player : Bukkit.getOnlinePlayers()) {
				if (CollectionUtils.contains(worlds, player.getWorld()))
					list.add(player);
			}
			return (E[]) list.toArray(new Player[list.size()]);
		}
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
		EntityDataInfo<?> closestInfo = null;
		EntityData<E> closestData = null;
		for (EntityDataInfo<?> info : infos) {
			if (info.entityClass == Entity.class)
				continue;
			if (entity == null ? info.entityClass.isAssignableFrom(entityClass) : info.entityClass.isInstance(entity)) {
				EntityData<E> entityData = null;
				try {
					//noinspection unchecked
					entityData = (EntityData<E>) info.getElementClass().newInstance();
				} catch (Exception ignored) {}
				if (entityData != null && entityData.init(entityClass, entity)) {
					if (closestInfo == null || closestInfo.entityClass.isAssignableFrom(info.entityClass)) {
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
	};

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

	public static String toString(Entity entity) {
		return fromEntity(entity).getSuperType().toString();
	}

	public static String toString(Class<? extends Entity> entityClass) {
		return fromClass(entityClass).getSuperType().toString();
	}

	public static String toString(Entity entity, int flags) {
		return fromEntity(entity).getSuperType().toString(flags);
	}

	public static String toString(Class<? extends Entity> entityClass, int flags) {
		return fromClass(entityClass).getSuperType().toString(flags);
	}

	@SuppressWarnings("unchecked")
	public final boolean isInstance(@Nullable Entity entity) {
		if (entity == null)
			return false;
		if (!baby.isUnknown() && EntityUtils.isAgeable(entity) && EntityUtils.isAdult(entity) != baby.isFalse())
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

	@Override
	public Fields serialize() throws NotSerializableException {
		return new Fields(this);
	}

	@Override
	public void deserialize(Fields fields) throws StreamCorruptedException, NotSerializableException {
		fields.setFields(this);
	}

	@Deprecated(since = "2.3.0", forRemoval = true)
	protected boolean deserialize(String string) {
		return false;
	}

	@Override
	public @NotNull String getSyntaxTypeName() {
		return "entity data";
	}

	@SuppressWarnings({"unchecked", "deprecation"})
	protected static <E extends Entity> @Nullable E spawn(Location location, Class<E> type, Consumer<E> consumer) {
		World world = location.getWorld();
		if (world == null)
			return null;
		try {
			if (WORLD_1_17_CONSUMER) {
				return (@Nullable E) WORLD_1_17_CONSUMER_METHOD.invoke(world, location, type,
					(org.bukkit.util.Consumer<E>) consumer::accept);
			}
		} catch (InvocationTargetException | IllegalAccessException e) {
			if (Skript.testing())
				Skript.exception(e, "Can't spawn " + type.getName());
			return null;
		}
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

}
