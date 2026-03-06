package ch.njol.skript.variables;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.ConfigurationSerializer;
import ch.njol.skript.config.Config;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.KeyedValue;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.*;
import ch.njol.util.coll.iterator.EmptyIterator;
import ch.njol.yggdrasil.ClassResolver;
import ch.njol.yggdrasil.Yggdrasil;
import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.errorprone.annotations.ThreadSafe;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import org.jetbrains.annotations.VisibleForTesting;
import org.skriptlang.skript.addon.SkriptAddon;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import java.util.regex.Pattern;

import org.skriptlang.skript.variables.storage.H2Storage;
import org.skriptlang.skript.variables.storage.InMemoryVariableStorage;
import org.skriptlang.skript.variables.storage.MySQLStorage;
import org.skriptlang.skript.variables.storage.SQLiteStorage;

/**
 * Factory class that handles things related to variables.
 * <p>
 * This includes the registration and management of variable
 * storage types and variable access.
 * <p>
 * All methods in this class are thread safe but some may be blocking, see documentation.
 *
 * @see #setVariable(String, Object, Event, boolean)
 * @see #getVariable(String, Event, boolean)
 */
@ThreadSafe
public final class Variables {

	/**
	 * The version of {@link Yggdrasil} this class is using.
	 */
	public static final short YGGDRASIL_VERSION = 1;

	/**
	 * The {@link Yggdrasil} instance used for (de)serialization.
	 */
	public static final Yggdrasil yggdrasil = new Yggdrasil(YGGDRASIL_VERSION);

	/**
	 * Whether variable names are case-sensitive.
	 */
	public static boolean caseInsensitiveVariables = true;

	// registered storages types
	private static final List<UnloadedStorage<?>> UNLOADED_STORAGES = Collections.synchronizedList(new ArrayList<>());
	// actually loaded storages types by the user
	@VisibleForTesting
	static final List<VariableStorage> STORAGES = new CopyOnWriteArrayList<>();

	/**
	 * Whether the storages specified by user has been loaded.
	 * <p>
	 * If true, it is safe to access variables.
	 */
	private static final AtomicBoolean loaded = new AtomicBoolean(false);

	/**
	 * Variable storage that is used as a backup.
	 * <p>
	 * Such variable storage must not have any variable pattern.
	 */
	private static VariableStorage defaultStorage;

	/**
	 * Variable storage that is used for ephemeral variables.
	 */
	private static VariableStorage ephemeralStorage;

	static {
		registerSkriptStorageTypes(Skript.instance());
		yggdrasil.registerSingleClass(Kleenean.class, "Kleenean");
		yggdrasil.registerClassResolver(new BukkitConfigurationSerializer());
	}

	/**
	 * Register a VariableStorage class for Skript to create if the user config value matches.
	 *
	 * @param source source of the storage type
	 * @param storage The class of the VariableStorage implementation.
	 * @param constructor provider of the storage instance
	 * @param names The names used in the config of Skript to select this VariableStorage.
	 * @return whether the storage type registration was successful
	 * @param <T> A class to extend VariableStorage.
	 * @throws SkriptAPIException if the operation was not successful because the storage class is already registered.
	 */
	public static <T extends VariableStorage> boolean registerStorage(SkriptAddon source, Class<T> storage,
			BiFunction<SkriptAddon, String, T> constructor, String... names) {
		Skript.checkAcceptRegistrations();
		for (UnloadedStorage<?> registered : UNLOADED_STORAGES) {
			if (registered.storage().isAssignableFrom(storage))
				throw new SkriptAPIException("Storage class '" + storage.getName() + "' cannot be registered because '"
					+ registered.storage().getName() + "' is a superclass or equal class");
			if (Arrays.stream(names).anyMatch(registered::matches))
				return false;
		}
		UNLOADED_STORAGES.add(new UnloadedStorage<>(source, storage, constructor, names));
		return true;
	}

	/**
	 * Registers the default storage types provided by Skript.
	 *
	 * @param source source for the registration
	 */
	private static void registerSkriptStorageTypes(SkriptAddon source) {
		registerStorage(source, FlatFileStorage.class, FlatFileStorage::new, "csv", "file", "flatfile");
		if (Skript.classExists("com.zaxxer.hikari.HikariConfig")) {
			registerStorage(source, SQLiteStorage.class, SQLiteStorage::new, "sqlite");
			registerStorage(source, MySQLStorage.class, MySQLStorage::new, "mysql");
			registerStorage(source, H2Storage.class, H2Storage::new, "h2");
		} else {
			Skript.warning("SpigotLibraryLoader failed to load HikariCP. No JDBC databases were enabled.");
		}
	}

	/**
	 * @return a copy of the list of variable storage handlers
	 */
	public static @UnmodifiableView List<VariableStorage> getLoadedStorages() {
		return Collections.unmodifiableList(STORAGES);
	}

	/**
	 * Load the variables configuration and all variables.
	 * <p>
	 * May only be called once, when Skript is loading.
	 *
	 * @return whether the loading was successful.
	 */
	public static boolean load() {
		assert STORAGES.isEmpty();
		if (loaded.compareAndExchange(false, true))
			throw new SkriptAPIException("Variables already loaded");

		Config config = SkriptConfig.getConfig();
		if (config == null)
			throw new SkriptAPIException("Cannot load variables before the config");

		Node databases = config.getMainNode().get("databases");
		if (!(databases instanceof SectionNode)) {
			Skript.error("The config is missing the required 'databases' section that defines where the variables are saved");
			return false;
		}

		//noinspection removal
		Skript.closeOnDisable(Variables::close);

		boolean success = true;

		for (Node node : databases) {
			if (!(node instanceof SectionNode sectionNode)) {
				Skript.error("Invalid line in databases: databases must be defined as sections");
				success = false;
				continue;
			}
			// Databases must be loaded sequentially on this thread.
			// Skript calls this method on the main thread, loading the databases in parallel and wait
			// would cause a dead lock because deserialization of variables from databases must be synced
			// on the main thread for some types.
			if (!loadDatabase(sectionNode)) {
				Skript.error("Failed to load database from node: " + sectionNode);
				success = false;
			}
		}

		// TODO possibly migrate variables to according databases if the variable storage does
		//  not accept the name of the variable.
		//  Currently no migration happens and no variables are lost on shutdown but that
		//  will cause "rediscovery" of old variables when database patterns get changed.
		//  There should also exist a migration system to move from flat file storage to
		//  modern alternatives for users.

		try {
			if (STORAGES.isEmpty()) {
				Skript.error("No databases to store variables are defined. Please enable at least the default "
					+ "database, even if you don't use variables at all.");
				return false;
			}
			List<VariableStorage> missingPattern = STORAGES.stream()
				.filter(storage -> storage.getNamePattern() == null)
				.toList();
			if (missingPattern.size() > 1)
				Skript.warning("You have multiple databases with pattern accepting all variable names, "
					+ "Skript will attempt to save variables only to one of them.");
			if (missingPattern.isEmpty()) {
				Skript.warning("You have no database matching all variable names. Some of your variables "
					+ "will not save.");
			} else {
				defaultStorage = missingPattern.getFirst();
			}
			return success;
		} finally {
			// there must always be at least a default storage to store global variables somewhere
			if (defaultStorage == null)
				defaultStorage = new InMemoryVariableStorage(Skript.instance(), "in-memory");
			ephemeralStorage = new InMemoryVariableStorage(Skript.instance(), "in-memory ephemeral");
			SkriptLogger.setNode(null);
		}
	}

	/**
	 * Loads a database from a section node.
	 *
	 * @param node section node in the database config
	 * @return result
	 */
	private static boolean loadDatabase(SectionNode node) {
		String type = node.getValue("type");
		if (type == null) {
			Skript.error("Missing entry 'type' in database definition");
			return false;
		}

		String name = node.getKey();
		assert name != null;

		Optional<UnloadedStorage<?>> optional = UNLOADED_STORAGES.stream()
			.filter(registered -> registered.matches(type))
			.findFirst();

		if (optional.isEmpty()) {
			if (type.equalsIgnoreCase("disabled") || type.equalsIgnoreCase("none"))
				return true;
			Skript.error("Invalid database type '" + type + "'");
			return false;
		}

		UnloadedStorage<?> unloadedStorage = optional.get();
		VariableStorage variablesStorage = unloadedStorage.create(unloadedStorage.source(), type);

		if (Skript.logVeryHigh())
			Skript.info("Loading database '" + name + "'...");

		boolean result = variablesStorage.loadConfig(node);
		if (result)
			STORAGES.add(variablesStorage);
		return result;
	}

	/**
	 * A pattern to split variable names using {@link Variable#SEPARATOR}.
	 */
	private static final Pattern VARIABLE_NAME_SPLIT_PATTERN = Pattern.compile(Pattern.quote(Variable.SEPARATOR));

	/**
	 * Splits the given variable name into its parts,
	 * separated by {@link Variable#SEPARATOR}.
	 *
	 * @param name the variable name.
	 * @return the parts.
	 */
	public static String[] splitVariableName(String name) {
		return VARIABLE_NAME_SPLIT_PATTERN.split(name);
	}

	/**
	 * A cache storing all local variables,
	 * indexed by their {@link Event}.
	 * <p>
	 * We use weak key cache because:
	 * <ul>
	 *     <li>variables are not kept in memory for expired events</li>
	 *     <li>variables map are looked up from event instance identity</li>
	 * </ul>
	 */
	private static final LoadingCache<Event, VariablesMap> localVariables = CacheBuilder.newBuilder()
		.weakKeys()
		.build(CacheLoader.from(() -> new VariablesMap()));

	/**
	 * Removes local variables associated with given event and returns them,
	 * if they exist.
	 *
	 * @param event the event.
	 * @return the local variables from the event,
	 * or {@code null} if the event had no local variables.
	 */
	public static VariablesMap removeLocals(Event event) {
		return localVariables.asMap().remove(event);
	}

	/**
	 * Returns the variable map for given event.
	 * <p>
	 * This never returns null and provides new variables map if there is none for
	 * the given event.
	 *
	 * @param event event
	 * @return variables map of given event
	 */
	public static VariablesMap getLocalVariables(Event event) {
		return localVariables.getUnchecked(event);
	}

	/**
	 * Sets local variables associated with given event.
	 * <p>
	 * If the given map is {@code null}, local variables for this event
	 * will be <b>removed</b>.
	 * <p>
	 * Warning: this can overwrite local variables!
	 *
	 * @param event the event.
	 * @param map the new local variables.
	 */
	public static void setLocalVariables(Event event, @Nullable VariablesMap map) {
		if (map != null) {
			localVariables.put(event, map);
		} else {
			removeLocals(event);
		}
	}

	/**
	 * Creates a copy of the {@link VariablesMap} for local variables
	 * in an event.
	 *
	 * @param event the event to copy local variables from.
	 * @return the copy.
	 */
	public static VariablesMap copyLocalVariables(Event event) {
		return getLocalVariables(event).copy();
	}

	/**
	 * Copies local variables from provider to user, runs action, then copies variables back to provider.
	 * Removes local variables from user after action is finished.
	 *
	 * @param provider The originator of the local variables.
	 * @param user The event to copy the variables to and back from.
	 * @param action The code to run while the variables are copied.
	 */
	public static void withLocalVariables(Event provider, Event user, Runnable action) {
		Variables.setLocalVariables(user, Variables.copyLocalVariables(provider));
		action.run();
		Variables.setLocalVariables(provider, Variables.copyLocalVariables(user));
		Variables.removeLocals(user);
	}

	/**
	 * Finds appropriate variable storage for given variable name.
	 *
	 * @param name name of the variable
	 * @return its preffered variable storage
	 * @see VariableStorage#accept(String)
	 */
	private static VariableStorage findStorage(String name) {
		assert defaultStorage != null;
		assert ephemeralStorage != null;
		if (name.startsWith(Variable.EPHEMERAL_VARIABLE_TOKEN))
			return ephemeralStorage;
		var all = STORAGES.stream()
			.filter(storage -> storage != defaultStorage) // default storage is fallback
			.filter(storage -> storage.accept(name))
			.toList();
		if (all.size() == 1)
			return all.getFirst();
		if (!all.isEmpty()) {
			VariableStorage first = all.getFirst();
			Skript.warning("Found multiple databases for variable '" + name + "'. Resolve the database pattern " +
				"conflicts in the config. Saving to '" + first.getUserConfigurationName() + "' database");
			return first;
		}
		return defaultStorage;
	}

	/**
	 * Returns the value of the requested variable.
	 * <p>
	 * In case of list variables, the returned map is unmodifiable view of the variables map.
	 * <p>
	 * If map is returned, it is sorted using a comparator that matches the variable name sorting.
	 * <p>
	 * If map is returned the structure is as following:
	 * <ul>
	 *     <li>
	 *         If value is present for the variable and
	 *         <ul>
	 *             <li>the variable has no children, it is mapped directly to the key</li>
	 *             <li>the variable has children it is mapped to a map, that maps {@code null} to its value and its
	 *             children are mapped using the same strategy</li>
	 *         </ul>
	 *     </li>
	 *     <li>If value is not present for the variable, it is mapped to a map with its children mapped using the same
	 *     strategy</li>
	 * </ul>
	 * <p>
	 * This does not take into consideration default variables. You must use get methods from {@link Variable}
	 *
	 * @param name The name of the variable.
	 * @param event If {@code local} is {@code true}, this is the event the local variable resides in.
	 * @param local If this variable is a local or global variable
	 * @return an {@link Object} for a regular variable
	 * or a {@code Map<String, Object>} for a list variable,
	 * or {@code null} if the variable is not set.
	 */
	public static @Nullable Object getVariable(String name, @Nullable Event event, boolean local) {
		String fixedName = caseInsensitiveVariables
			? name.toLowerCase(Locale.ENGLISH)
			: name;

		if (local)
			return getLocalVariables(event).getVariable(fixedName);

		//noinspection resource
		VariableStorage foundStorage = findStorage(fixedName);
		return foundStorage.getVariable(fixedName);
	}

	/**
	 * Returns an iterator over the values of this list variable.
	 *
	 * @param name the variable's name. This must be the name of a list variable, ie. it must end in *.
	 * @param event if {@code local} is {@code true}, this is the event the local variable resides in.
	 * @param local if this variable is a local or global variable.
	 * @return an {@link Iterator} of {@link KeyedValue}, containing the {@link String} index and {@link Object} value
	 * of the elements of the list. An empty iterator is returned if the variable does not exist.
	 */
	public static Iterator<KeyedValue<Object>> getVariableIterator(String name, boolean local, @Nullable Event event) {
		assert name.endsWith(Variable.SEPARATOR + "*");
		Object val = getVariable(name, event, local);
		String subName = StringUtils.substring(name, 0, -1);
		if (val == null)
			return new EmptyIterator<>();
		if (!(val instanceof Map<?,?> map))
			throw new SkriptAPIException("Expected list variable");

		Iterator<String> keys = map.keySet().stream().map(String.class::cast).iterator();

		return new Iterator<>() {

			private @Nullable String key;
			private @Nullable Object next = null;

			@Override
			public boolean hasNext() {
				if (next != null)
					return true;
				while (keys.hasNext()) {
					key = keys.next();
					if (key == null)
						continue;
					next = Variable.convertIfOldPlayer(subName + key, local, event,
						Variables.getVariable(subName + key, event, local));
					if (next != null && !(next instanceof TreeMap))
						return true;
				}
				next = null;
				return false;
			}

			@Override
			public KeyedValue<Object> next() {
				if (!hasNext())
					throw new NoSuchElementException();
				assert key != null;
				assert next != null;
				KeyedValue<Object> value = new KeyedValue<>(key, next);
				next = null;
				return value;
			}

			@Override
			public void remove() {
				Preconditions.checkState(key != null, "Expected non null key");
				Variables.deleteVariable(subName + key, event, local);
			}
		};
	}

	/**
	 * Deletes a variable.
	 *
	 * @param name the variable's name.
	 * @param event if {@code local} is {@code true}, this is the event the local variable resides in.
	 * @param local if this variable is a local or global variable.
	 */
	public static void deleteVariable(String name, @Nullable Event event, boolean local) {
		setVariable(name, null, event, local);
	}

	/**
	 * Sets a variable.
	 *
	 * @param name the variable's name. Can be a "list variable::*",but {@code value} must be {@code null} in this case.
	 * @param value The variable's value. Use {@code null} to delete the variable.
	 * @param event if {@code local} is {@code true}, this is the event the local variable resides in.
	 * @param local if this variable is a local or global variable.
	 */
	public static void setVariable(String name, @Nullable Object value, @Nullable Event event, boolean local) {
		String fixedName = caseInsensitiveVariables
			? name.toLowerCase(Locale.ENGLISH)
			: name;

		if (local) {
			assert event != null : fixedName;

			// Get the variables map and set the variable in it
			getLocalVariables(event).setVariable(fixedName, value);
			return;
		}

		//noinspection resource
		VariableStorage foundStorage = findStorage(fixedName);
		foundStorage.setVariable(fixedName, value);
	}

	/**
	 * Closes all loaded variable storages.
	 */
	public static void close() {
		STORAGES.forEach(VariableStorage::close);
	}

	/**
	 * Returns the number of currently loaded variables in memory.
	 * <p>
	 * This number may not be fully accurate.
	 *
	 * @return number of loaded variables
	 */
	public static long numVariables() {
		return STORAGES.stream()
			.mapToLong(VariableStorage::loadedVariables)
			.reduce(0, Long::sum);
	}

	private Variables() {
		throw new UnsupportedOperationException();
	}

	private static final class BukkitConfigurationSerializer extends ConfigurationSerializer<ConfigurationSerializable> {

		/**
		 * The {@link ClassResolver#getID(Class) ID} prefix
		 * for {@link ConfigurationSerializable} classes.
		 */
		static final String CONFIGURATION_SERIALIZABLE_PREFIX = "ConfigurationSerializable_";

		BukkitConfigurationSerializer() {
			// Info field is mostly unused in superclass, due to methods overridden below,
			// so this illegal cast is fine
			//noinspection unchecked
			info = (ClassInfo<? extends ConfigurationSerializable>) (ClassInfo<?>) Classes.getExactClassInfo(Object.class);
		}

		@Override
		public @Nullable String getID(@NotNull Class<?> c) {
			if (ConfigurationSerializable.class.isAssignableFrom(c)
				&& Classes.getSuperClassInfo(c) == Classes.getExactClassInfo(Object.class))
				return CONFIGURATION_SERIALIZABLE_PREFIX +
					ConfigurationSerialization.getAlias(c.asSubclass(ConfigurationSerializable.class));
			return null;
		}

		@Override
		public @Nullable Class<? extends ConfigurationSerializable> getClass(@NotNull String id) {
			if (id.startsWith(CONFIGURATION_SERIALIZABLE_PREFIX))
				return ConfigurationSerialization.getClassByAlias(
					id.substring(CONFIGURATION_SERIALIZABLE_PREFIX.length()));
			return null;
		}

	}

}
