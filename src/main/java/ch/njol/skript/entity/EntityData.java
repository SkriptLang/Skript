/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.entity;

import java.io.NotSerializableException;
import java.io.StreamCorruptedException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.RegionAccessor;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.eclipse.jdt.annotation.Nullable;

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

@SuppressWarnings("rawtypes")
public abstract class EntityData<E extends Entity> implements SyntaxElement, YggdrasilExtendedSerializable {// TODO extended horse support, zombie villagers // REMIND unit

	/*
	 * In 1.20.2 Spigot deprecated org.bukkit.util.Consumer.
	 * From the class header: "API methods which use this consumer will be remapped to Java's consumer at runtime, resulting in an error."
	 * But in 1.13-1.16 the only way to use a consumer was World#spawn(Location, Class, org.bukkit.util.Consumer).
	 */
	@Nullable
	protected static Method WORLD_1_13_CONSUMER_METHOD;
	protected static final boolean WORLD_1_13_CONSUMER = Skript.methodExists(World.class, "spawn", Location.class, Class.class, org.bukkit.util.Consumer.class);

	@Nullable
	protected static Method WORLD_1_17_CONSUMER_METHOD;
	protected static boolean WORLD_1_17_CONSUMER;

	static {
		try {
			if (WORLD_1_13_CONSUMER) {
				WORLD_1_13_CONSUMER_METHOD = World.class.getDeclaredMethod("spawn", Location.class, Class.class, org.bukkit.util.Consumer.class);
			} else if (Skript.classExists("org.bukkit.RegionAccessor")) {
				if (WORLD_1_17_CONSUMER = Skript.methodExists(RegionAccessor.class, "spawn", Location.class, Class.class, org.bukkit.util.Consumer.class))
					WORLD_1_17_CONSUMER_METHOD = RegionAccessor.class.getDeclaredMethod("spawn", Location.class, Class.class, org.bukkit.util.Consumer.class);
			}
		} catch (NoSuchMethodException | SecurityException ignored) { /* We already checked if the method exists */ }
	}

	public final static String LANGUAGE_NODE = "entities";

	public final static Message m_age_pattern = new Message(LANGUAGE_NODE + ".age pattern");
	public final static Adjective m_baby = new Adjective(LANGUAGE_NODE + ".age adjectives.baby"),
			m_adult = new Adjective(LANGUAGE_NODE + ".age adjectives.adult");

	// must be here to be initialised before 'new SimpleLiteral' is called in the register block below
	private final static List<EntityDataInfo<EntityData<?>>> infos = new ArrayList<>();

	private static final Pattern REGEX_PATTERN = Pattern.compile("[a-zA-Z -]+");

	private static final List<EntityData> ALL_ENTITY_DATAS = new ArrayList<>();

	public static Serializer<EntityData> serializer = new Serializer<EntityData>() {
		@Override
		public Fields serialize(final EntityData o) throws NotSerializableException {
			final Fields f = o.serialize();
			f.putObject("codeName", o.info.codeName);
			return f;
		}
		
		@Override
		public boolean canBeInstantiated() {
			return false;
		}
		
		@Override
		public void deserialize(final EntityData o, final Fields f) throws StreamCorruptedException {
			assert false;
		}
		
		@Override
		protected EntityData deserialize(final Fields fields) throws StreamCorruptedException, NotSerializableException {
			final String codeName = fields.getAndRemoveObject("codeName", String.class);
			if (codeName == null)
				throw new StreamCorruptedException();
			final EntityDataInfo<?> info = getInfo(codeName);
			if (info == null)
				throw new StreamCorruptedException("Invalid EntityData code name " + codeName);
			try {
				final EntityData<?> d = info.getElementClass().newInstance();
				d.deserialize(fields);
				return d;
			} catch (final InstantiationException e) {
				Skript.exception(e);
			} catch (final IllegalAccessException e) {
				Skript.exception(e);
			}
			throw new StreamCorruptedException();
		}
		
//		return getInfo((Class<? extends EntityData<?>>) d.getClass()).codeName + ":" + d.serialize();
		@SuppressWarnings("null")
		@Override
		@Deprecated
		@Nullable
		public EntityData deserialize(final String s) {
			final String[] split = s.split(":", 2);
			if (split.length != 2)
				return null;
			final EntityDataInfo<?> i = getInfo(split[0]);
			if (i == null)
				return null;
			EntityData<?> d;
			try {
				d = i.getElementClass().newInstance();
			} catch (final Exception e) {
				Skript.exception(e, "Can't create an instance of " + i.getElementClass().getCanonicalName());
				return null;
			}
			if (!d.deserialize(split[1]))
				return null;
			return d;
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
					public String toString(final EntityData d, final int flags) {
						return d.toString(flags);
					}
					
					@Override
					@Nullable
					public EntityData parse(final String s, final ParseContext context) {
						return EntityData.parse(s);
					}
					
					@Override
					public String toVariableNameString(final EntityData o) {
						return "entitydata:" + o.toString();
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

	private final static class EntityDataInfo<T extends EntityData<?>> extends SyntaxElementInfo<T> implements LanguageChangeListener {
		final String codeName;
		final String[] codeNames;
		final int defaultName;
		final Class<? extends Entity> entityClass;
		final Noun[] names;
		
		public EntityDataInfo(final Class<T> dataClass, final String codeName, final String[] codeNames, final int defaultName, final Class<? extends Entity> entityClass) throws IllegalArgumentException {
			super(new String[codeNames.length], dataClass, dataClass.getName());
			assert codeName != null && entityClass != null && codeNames.length > 0;
			this.codeName = codeName;
			this.codeNames = codeNames;
			this.defaultName = defaultName;
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
			final int prime = 31;
			int result = 1;
			result = prime * result + codeName.hashCode();
			return result;
		}
		
		@Override
		public boolean equals(final @Nullable Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (!(obj instanceof EntityDataInfo))
				return false;
			final EntityDataInfo other = (EntityDataInfo) obj;
			if (!codeName.equals(other.codeName))
				return false;
			assert Arrays.equals(codeNames, other.codeNames);
			assert defaultName == other.defaultName;
			assert entityClass == other.entityClass;
			return true;
		}
		
	}
	
	public static <E extends Entity, T extends EntityData<E>> void register(final Class<T> dataClass, final String name, final Class<E> entityClass, final String codeName) throws IllegalArgumentException {
		register(dataClass, name, entityClass, 0, codeName);
	}
	
	@SuppressWarnings("unchecked")
	public static <E extends Entity, T extends EntityData<E>> void register(final Class<T> dataClass, final String name, final Class<E> entityClass, final int defaultName, final String... codeNames) throws IllegalArgumentException {
		final EntityDataInfo<T> info = new EntityDataInfo<>(dataClass, name, codeNames, defaultName, entityClass);
		for (int i = 0; i < infos.size(); i++) {
			if (infos.get(i).entityClass.isAssignableFrom(entityClass)) {
				infos.add(i, (EntityDataInfo<EntityData<?>>) info);
				return;
			}
		}
		infos.add((EntityDataInfo<EntityData<?>>) info);
	}
	
	transient EntityDataInfo<?> info;
	protected int matchedPattern = 0;
	private Kleenean plural = Kleenean.UNKNOWN;
	private Kleenean baby = Kleenean.UNKNOWN;
	
	public EntityData() {
		for (final EntityDataInfo<?> i : infos) {
			if (getClass() == i.getElementClass()) {
				info = i;
				matchedPattern = i.defaultName;
				return;
			}
		}
		throw new IllegalStateException();
	}
	
	@SuppressWarnings("null")
	@Override
	public final boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		this.matchedPattern = matchedPattern;
		// plural bits (0x3): 0 = singular, 1 = plural, 2 = unknown
		final int pluralBits = parseResult.mark & 0x3;
		this.plural = pluralBits == 1 ? Kleenean.TRUE : pluralBits == 0 ? Kleenean.FALSE : Kleenean.UNKNOWN;
		// age bits (0xC): 0 = unknown, 4 = baby, 8 = adult
		final int ageBits = parseResult.mark & 0xC;
		this.baby = ageBits == 4 ? Kleenean.TRUE : ageBits == 8 ? Kleenean.FALSE : Kleenean.UNKNOWN;
		return init(Arrays.copyOf(exprs, exprs.length, Literal[].class), matchedPattern, parseResult);
	}
	
	protected abstract boolean init(final Literal<?>[] exprs, final int matchedPattern, final ParseResult parseResult);
	
	/**
	 * @param c An entity's class, e.g. Player
	 * @param e An actual entity, or null to get an entity data for an entity class
	 * @return Whether initialisation was successful
	 */
	protected abstract boolean init(@Nullable Class<? extends E> c, @Nullable E e);
	
	public abstract void set(E entity);
	
	protected abstract boolean match(E entity);
	
	public abstract Class<? extends E> getType();
	
	/**
	 * Returns the super type of this entity data, e.g. 'wolf' for 'angry wolf'.
	 *
	 * @return The supertype of this entity data. Must not be null.
	 */
	public abstract EntityData getSuperType();
	
	@Override
	public final String toString() {
		return toString(0);
	}
	
	@SuppressWarnings("null")
	protected Noun getName() {
		return info.names[matchedPattern];
	}
	
	@Nullable
	protected Adjective getAgeAdjective() {
		return baby.isTrue() ? m_baby : baby.isFalse() ? m_adult : null;
	}
	
	@SuppressWarnings("null")
	public String toString(final int flags) {
		final Noun name = info.names[matchedPattern];
		return baby.isTrue() ? m_baby.toString(name, flags) : baby.isFalse() ? m_adult.toString(name, flags) : name.toString(flags);
	}
	
	public Kleenean isPlural() {
		return plural;
	}
	
	public Kleenean isBaby() {
		return baby;
	}
	
	protected abstract int hashCode_i();
	
	@Override
	public final int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + baby.hashCode();
		result = prime * result + plural.hashCode();
		result = prime * result + matchedPattern;
		result = prime * result + info.hashCode();
		result = prime * result + hashCode_i();
		return result;
	}
	
	protected abstract boolean equals_i(EntityData<?> obj);
	
	@Override
	public final boolean equals(final @Nullable Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof EntityData))
			return false;
		final EntityData other = (EntityData) obj;
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
	
	public static EntityDataInfo<?> getInfo(final Class<? extends EntityData<?>> c) {
		for (final EntityDataInfo<?> i : infos) {
			if (i.getElementClass() == c)
				return i;
		}
		throw new SkriptAPIException("Unregistered EntityData class " + c.getName());
	}
	
	@Nullable
	public static EntityDataInfo<?> getInfo(final String codeName) {
		for (final EntityDataInfo<?> i : infos) {
			if (i.codeName.equals(codeName))
				return i;
		}
		return null;
	}

	/**
	 * Prints errors.
	 *
	 * @param s String with optional indefinite article at the beginning
	 * @return The parsed entity data
	 */
	@SuppressWarnings("null")
	@Nullable
	public static EntityData<?> parse(String s) {
		if (!REGEX_PATTERN.matcher(s).matches())
			return null;
		Iterator<EntityDataInfo<EntityData<?>>> it = infos.iterator();
		return SkriptParser.parseStatic(Noun.stripIndefiniteArticle(s), it, null);
	}
	
	/**
	 * Prints errors.
	 *
	 * @param s
	 * @return The parsed entity data
	 */
	@Nullable
	public static EntityData<?> parseWithoutIndefiniteArticle(String s) {
		if (!REGEX_PATTERN.matcher(s).matches())
			return null;
		Iterator<EntityDataInfo<EntityData<?>>> it = infos.iterator();
		return SkriptParser.parseStatic(s, it, null);
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
	 * Spawn this entity data at a location.
	 *
	 * @param location The {@link Location} to spawn the entity at.
	 * @return The Entity object that is spawned.
	 */
	@Nullable
	public final E spawn(Location location) {
		return spawn(location, (Consumer<E>) null);
	}

	/**
	 * Spawn this entity data at a location.
	 * The consumer allows for modiciation to the entity before it actually gets spawned.
	 * <p>
	 * Bukkit's own {@link org.bukkit.util.Consumer} is deprecated.
	 * Use {@link #spawn(Location, Consumer)}
	 *
	 * @param location The {@link Location} to spawn the entity at.
	 * @param consumer A {@link Consumer} to apply the entity changes to.
	 * @return The Entity object that is spawned.
	 */
	@Nullable
	@Deprecated
	@SuppressWarnings("deprecation")
	public E spawn(Location location, org.bukkit.util.@Nullable Consumer<E> consumer) {
		return spawn(location, (Consumer<E>) e -> consumer.accept(e));
	}

	/**
	 * Spawn this entity data at a location.
	 * The consumer allows for modiciation to the entity before it actually gets spawned.
	 *
	 * @param location The {@link Location} to spawn the entity at.
	 * @param consumer A {@link Consumer} to apply the entity changes to.
	 * @return The Entity object that is spawned.
	 */
	@Nullable
	public E spawn(Location location, @Nullable Consumer<E> consumer) {
		assert location != null;
		if (consumer != null) {
			return EntityData.spawn(location, getType(), e -> consumer.accept(this.apply(e)));
		} else {
			return apply(location.getWorld().spawn(location, getType()));
		}
	}

	@SuppressWarnings("unchecked")
	public E[] getAll(final World... worlds) {
		assert worlds != null && worlds.length > 0 : Arrays.toString(worlds);
		final List<E> list = new ArrayList<>();
		for (final World w : worlds) {
			for (final E e : w.getEntitiesByClass(getType()))
				if (match(e))
					list.add(e);
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
	public static <E extends Entity> E[] getAll(final EntityData<?>[] types, final Class<E> type, @Nullable World[] worlds) {
		assert types.length > 0;
		if (type == Player.class) {
			if (worlds == null)
				return (E[]) Bukkit.getOnlinePlayers().toArray(new Player[0]);
			List<Player> list = new ArrayList<>();
			for (Player p : Bukkit.getOnlinePlayers()) {
				if (CollectionUtils.contains(worlds, p.getWorld()))
					list.add(p);
			}
			return (E[]) list.toArray(new Player[list.size()]);
		}
		final List<E> list = new ArrayList<>();
		if (worlds == null)
			worlds = Bukkit.getWorlds().toArray(new World[0]);
		for (final World w : worlds) {
			for (final E e : w.getEntitiesByClass(type)) {
				for (final EntityData<?> t : types) {
					if (t.isInstance(e)) {
						list.add(e);
						break;
					}
				}
			}
		}
		return list.toArray((E[]) Array.newInstance(type, list.size()));
	}
	
	@SuppressWarnings("unchecked")
	public static <E extends Entity> E[] getAll(final EntityData<?>[] types, final Class<E> type, Chunk[] chunks) {
		assert types.length > 0;
		final List<E> list = new ArrayList<>();
		for (Chunk chunk : chunks) {
			for (Entity entity : chunk.getEntities()) {
				for (EntityData<?> t : types) {
					if (t.isInstance(entity)) {
						list.add(((E) entity));
						break;
					}
				}
			}
		}
		return list.toArray((E[]) Array.newInstance(type, list.size()));
	}
	
	private static <E extends Entity> EntityData<? super E> getData(final @Nullable Class<E> c, final @Nullable E e) {
		assert c == null ^ e == null;
		assert c == null || c.isInterface();
		for (final EntityDataInfo<?> info : infos) {
			if (info.entityClass != Entity.class && (e == null ? info.entityClass.isAssignableFrom(c) : info.entityClass.isInstance(e))) {
				try {
					@SuppressWarnings("unchecked")
					final EntityData<E> d = (EntityData<E>) info.getElementClass().newInstance();
					if (d.init(c, e))
						return d;
				} catch (final Exception ex) {
					throw Skript.exception(ex);
				}
			}
		}
		if (e != null) {
			return new SimpleEntityData(e);
		} else {
			assert c != null;
			return new SimpleEntityData(c);
		}
	}
	
	public static <E extends Entity> EntityData<? super E> fromClass(final Class<E> c) {
		return getData(c, null);
	}
	
	public static <E extends Entity> EntityData<? super E> fromEntity(final E e) {
		return getData(null, e);
	}
	
	public static String toString(final Entity e) {
		return fromEntity(e).getSuperType().toString();
	}
	
	public static String toString(final Class<? extends Entity> c) {
		return fromClass(c).getSuperType().toString();
	}
	
	public static String toString(final Entity e, final int flags) {
		return fromEntity(e).getSuperType().toString(flags);
	}
	
	public static String toString(final Class<? extends Entity> c, final int flags) {
		return fromClass(c).getSuperType().toString(flags);
	}
	
	@SuppressWarnings("unchecked")
	public final boolean isInstance(final @Nullable Entity e) {
		if (e == null)
			return false;
		if (!baby.isUnknown() && EntityUtils.isAgeable(e) && EntityUtils.isAdult(e) != baby.isFalse())
			return false;
		return getType().isInstance(e) && match((E) e);
	}
	
	public abstract boolean isSupertypeOf(EntityData<?> e);
	
	@Override
	public Fields serialize() throws NotSerializableException {
		return new Fields(this);
	}
	
	@Override
	public void deserialize(final Fields fields) throws StreamCorruptedException, NotSerializableException {
		fields.setFields(this);
	}
	
	@Deprecated
	protected boolean deserialize(final String s) {
		return false;
	}
	
	@SuppressWarnings({"unchecked", "deprecation"})
	protected static <E extends Entity> @Nullable E spawn(Location location, Class<E> type, Consumer<E> consumer) {
		try {
			if (WORLD_1_17_CONSUMER) {
				return (@Nullable E) WORLD_1_17_CONSUMER_METHOD.invoke(location.getWorld(), location, type,
					(org.bukkit.util.Consumer<E>) consumer::accept);
			} else if (WORLD_1_13_CONSUMER) {
				return (@Nullable E) WORLD_1_13_CONSUMER_METHOD.invoke(location.getWorld(), location, type,
					(org.bukkit.util.Consumer<E>) consumer::accept);
			}
		} catch (InvocationTargetException | IllegalAccessException e) {
			if (Skript.testing())
				Skript.exception(e, "Can't spawn " + type.getName());
			return null;
        }
        return location.getWorld().spawn(location, type, consumer);
	}
	
}
