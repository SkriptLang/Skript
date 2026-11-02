package ch.njol.skript.variables;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.ConfigurationSerializer;
import ch.njol.skript.config.Config;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.variables.SerializedVariable.Value;
import ch.njol.util.Kleenean;
import ch.njol.util.NonNullPair;
import ch.njol.util.Pair;
import ch.njol.util.StringUtils;
import ch.njol.util.SynchronizedReference;
import ch.njol.util.coll.iterator.EmptyIterator;
import ch.njol.yggdrasil.Yggdrasil;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import org.skriptlang.skript.lang.converter.Converters;
import org.skriptlang.skript.variables.VariableMap;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Handles all things related to variables.
 *
 * @see #setVariable(String, Object, Event, boolean)
 * @see #getVariable(String, Event, boolean)
 */
public class Variables {

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

	/**
	 * The {@link ch.njol.yggdrasil.ClassResolver#getID(Class) ID} prefix
	 * for {@link ConfigurationSerializable} classes.
	 */
	private static final String CONFIGURATION_SERIALIZABLE_PREFIX = "ConfigurationSerializable_";

	private final static Multimap<Class<? extends VariablesStorage>, String> TYPES = HashMultimap.create();

	// Register some things with Yggdrasil
	static {
		registerStorage(FlatFileStorage.class, "csv", "file", "flatfile");
		registerStorage(SQLiteStorage.class, "sqlite");
		registerStorage(MySQLStorage.class, "mysql");
		yggdrasil.registerSingleClass(Kleenean.class, "Kleenean");
		// Register ConfigurationSerializable, Bukkit's serialization system
		yggdrasil.registerClassResolver(new ConfigurationSerializer<ConfigurationSerializable>() {
			{
				//noinspection unchecked
				info = (ClassInfo<? extends ConfigurationSerializable>) (ClassInfo<?>) Classes.getExactClassInfo(Object.class);
				// Info field is mostly unused in superclass, due to methods overridden below,
				//  so this illegal cast is fine
			}

			@Override
			@Nullable
			public String getID(@NotNull Class<?> c) {
				if (ConfigurationSerializable.class.isAssignableFrom(c)
						&& Classes.getSuperClassInfo(c) == Classes.getExactClassInfo(Object.class))
					return CONFIGURATION_SERIALIZABLE_PREFIX +
							ConfigurationSerialization.getAlias(c.asSubclass(ConfigurationSerializable.class));

				return null;
			}

			@Override
			@Nullable
			public Class<? extends ConfigurationSerializable> getClass(@NotNull String id) {
				if (id.startsWith(CONFIGURATION_SERIALIZABLE_PREFIX))
					return ConfigurationSerialization.getClassByAlias(
							id.substring(CONFIGURATION_SERIALIZABLE_PREFIX.length()));

				return null;
			}
		});
	}

	/**
	 * The variable storages configured.
	 */
	static final List<VariablesStorage> STORAGES = new ArrayList<>();

	/**
	 * @return a copy of the list of variable storage handlers
	 */
	public static @UnmodifiableView List<VariablesStorage> getStores() {
		return Collections.unmodifiableList(STORAGES);
	}

	/**
	 * Register a VariableStorage class for Skript to create if the user config value matches.
	 *
	 * @param <T> A class to extend VariableStorage.
	 * @param storage The class of the VariableStorage implementation.
	 * @param names The names used in the config of Skript to select this VariableStorage.
	 * @return if the operation was successful, or if it's already registered.
	 */
	public static <T extends VariablesStorage> boolean registerStorage(Class<T> storage, String... names) {
		if (TYPES.containsKey(storage))
			return false;
		for (String name : names) {
			if (TYPES.containsValue(name.toLowerCase(Locale.ENGLISH)))
				return false;
		}
		for (String name : names)
			TYPES.put(storage, name.toLowerCase(Locale.ENGLISH));
		return true;
	}

	/**
	 * Load the variables configuration and all variables.
	 * <p>
	 * May only be called once, when Skript is loading.
	 *
	 * @return whether the loading was successful.
	 */
	public static boolean load() {
		assert variables.isEmpty();
		assert STORAGES.isEmpty();

		Config config = SkriptConfig.getConfig();
		if (config == null)
			throw new SkriptAPIException("Cannot load variables before the config");

		Node databases = config.getMainNode().get("databases");
		if (!(databases instanceof SectionNode)) {
			Skript.error("The config is missing the required 'databases' section that defines where the variables are saved");
			return false;
		}

		Skript.closeOnDisable(Variables::close);

		// reports once per second how many variables were loaded. Useful to make clear that Skript is still doing something if it's loading many variables
		Thread loadingLoggerThread = new Thread(() -> {
			while (true) {
				try {
					Thread.sleep(Skript.logNormal() ? 1000 : 5000); // low verbosity won't disable these messages, but makes them more rare
				} catch (InterruptedException ignored) {}

				synchronized (TEMP_VARIABLES) {
					Map<String, NonNullPair<Object, VariablesStorage>> tvs = TEMP_VARIABLES.get();
					if (tvs != null)
						Skript.info("Loaded " + tvs.size() + " variables so far...");
					else
						break; // variables loaded, exit thread
				}
			}
		});
		loadingLoggerThread.start();

		try {
			boolean successful = true;

			for (Node node : (SectionNode) databases) {
				if (node instanceof SectionNode) {
					SectionNode sectionNode = (SectionNode) node;

					String type = sectionNode.getValue("type");
					if (type == null) {
						Skript.error("Missing entry 'type' in database definition");
						successful = false;
						continue;
					}

					String name = sectionNode.getKey();
					assert name != null;

					// Initiate the right VariablesStorage class
					VariablesStorage variablesStorage;
					Optional<?> optional = TYPES.entries().stream()
							.filter(entry -> entry.getValue().equalsIgnoreCase(type))
							.map(Entry::getKey)
							.findFirst();
					if (!optional.isPresent()) {
						if (!type.equalsIgnoreCase("disabled") && !type.equalsIgnoreCase("none")) {
							Skript.error("Invalid database type '" + type + "'");
							successful = false;
						}
						continue;
					}

					try {
						@SuppressWarnings("unchecked")
						Class<? extends VariablesStorage> storageClass = (Class<? extends VariablesStorage>) optional.get();
						Constructor<?> constructor = storageClass.getDeclaredConstructor(String.class);
						constructor.setAccessible(true);
						variablesStorage = (VariablesStorage) constructor.newInstance(type);
					} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
						Skript.error("Failed to initialize database `" + name + "`");
						successful = false;
						continue;
					}

					// Get the amount of variables currently loaded
					int totalVariablesLoaded;
					synchronized (TEMP_VARIABLES) {
						Map<String, NonNullPair<Object, VariablesStorage>> tvs = TEMP_VARIABLES.get();
						assert tvs != null;
						totalVariablesLoaded = tvs.size();
					}

					long start = System.currentTimeMillis();
					if (Skript.logVeryHigh())
						Skript.info("Loading database '" + node.getKey() + "'...");

					// Load the variables
					if (variablesStorage.load(sectionNode))
						STORAGES.add(variablesStorage);
					else
						successful = false;

					// Get the amount of variables loaded by this variables storage object
					int newVariablesLoaded;
					synchronized (TEMP_VARIABLES) {
						Map<String, NonNullPair<Object, VariablesStorage>> tvs = TEMP_VARIABLES.get();
						assert tvs != null;
						newVariablesLoaded = tvs.size() - totalVariablesLoaded;
					}

					if (Skript.logVeryHigh()) {
						Skript.info("Loaded " + newVariablesLoaded + " variables from the database " +
							"'" + sectionNode.getKey() + "' in " +
							((System.currentTimeMillis() - start) / 100) / 10.0 + " seconds");
					}
				} else {
					Skript.error("Invalid line in databases: databases must be defined as sections");
					successful = false;
				}
			}
			if (!successful)
				return false;

			if (STORAGES.isEmpty()) {
				Skript.error("No databases to store variables are defined. Please enable at least the default database, even if you don't use variables at all.");
				return false;
			}
		} finally {
			SkriptLogger.setNode(null);

			// make sure to put the loaded variables into the variables map
			int notStoredVariablesCount = onStoragesLoaded();
			if (notStoredVariablesCount != 0) {
				Skript.warning(notStoredVariablesCount + " variables were possibly discarded due to not belonging to any database " +
						"(SQL databases keep such variables and will continue to generate this warning, " +
						"while CSV discards them).");
			}

			// Interrupt the loading logger thread to make it exit earlier
			loadingLoggerThread.interrupt();

			saveThread.start();
		}
		return true;
	}

	/**
	 * Splits the given variable name into its parts,
	 * separated by {@link Variable#SEPARATOR}.
	 *
	 * @param name the variable name.
	 * @return the parts.
	 */
	public static String[] splitVariableName(String name) {
		String sep = Variable.SEPARATOR;
		int sepLen = sep.length();
		// Fast path for 0 or 1 separators — covers the vast majority of cases
		// and avoids ArrayList allocation entirely.
		int first = name.indexOf(sep);
		if (first == -1)
			return new String[]{name};
		int second = name.indexOf(sep, first + sepLen);
		if (second == -1)
			return new String[]{name.substring(0, first), name.substring(first + sepLen)};
		// 3+ parts — use a list for the remainder
		List<String> parts = new ArrayList<>();
		parts.add(name.substring(0, first));
		parts.add(name.substring(first + sepLen, second));
		int start = second + sepLen;
		int index;
		while ((index = name.indexOf(sep, start)) != -1) {
			parts.add(name.substring(start, index));
			start = index + sepLen;
		}
		parts.add(name.substring(start));
		return parts.toArray(String[]::new);
	}

	/**
	 * Keeps {@link #saveQueue} in the same order as the changes made to {@link #variables}, by being held across
	 * both, so that storages end up with the same final value as the map. It's not needed to read or write the map.
	 * <p>
	 * This is held by every writer of a saved variable, so nothing slow should happen under it.
	 * That includes serializing the value. Variables that don't get saved have no order to keep, so they skip it.
	 */
	static final ReentrantLock saveOrderLock = new ReentrantLock();

	/**
	 * The {@link VariableMap} storing global variables. This is thread-safe, so reading it doesn't need a lock.
	 * Writers should hold {@link #saveOrderLock} if the change has to be saved.
	 */
	static final VariableMap variables = VariableMap.concurrent();

	/**
	 * A map storing all local variables,
	 * indexed by their {@link Event}.
	 */
	private static final Map<Event, VariableMap> localVariables = new ConcurrentHashMap<>();

	/**
	 * Gets a sorted snapshot of all global variables by their full names.
	 * This copies every variable, so it's only meant for things like saving.
	 *
	 * @return An unmodifiable copy of the global variables.
	 * @see VariableMap#getAll()
	 */
	static Map<String, Object> getVariables() {
		return variables.getAll();
	}

	/**
	 * Removes local variables associated with given event and returns them,
	 * if they exist.
	 *
	 * @param event the event.
	 * @return the local variables from the event,
	 * or {@code null} if the event had no local variables.
	 */
	@Nullable
	public static Object removeLocals(Event event) {
		if (event instanceof LocalVariableHoldingEvent localVariableHoldingEvent) {
			VariableMap map = localVariableHoldingEvent.getLocals();
			localVariableHoldingEvent.setLocals(null);
			return map;
		}
		return localVariables.remove(event);
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
	public static void setLocalVariables(Event event, @Nullable Object map) {
		if (map != null) {
			if (event instanceof LocalVariableHoldingEvent lvEvent) {
				lvEvent.setLocals((VariableMap) map);
			} else {
				localVariables.put(event, (VariableMap) map);
			}
		} else {
			removeLocals(event);
		}
	}

	/**
	 * Creates a copy of the {@link VariableMap} for local variables
	 * in an event.
	 *
	 * @param event the event to copy local variables from.
	 * @return the copy.
	 */
	public static @Nullable Object copyLocalVariables(Event event) {
		VariableMap from = event instanceof LocalVariableHoldingEvent lvEvent ? lvEvent.getLocals() : localVariables.get(event);
		if (from == null)
			return null;

		return from.copy();
	}

	/**
	 * Copies local variables from provider to user, runs action, then moves the variables back to provider.
	 * This leaves user with no local variables once action is finished.
	 * @param provider The originator of the local variables.
	 * @param user The event to copy the variables to and back from.
	 * @param action The code to run while the variables are copied.
	 */
	public static void withLocalVariables(Event provider, Event user, @NotNull Runnable action) {
		Variables.setLocalVariables(user, Variables.copyLocalVariables(provider));
		action.run();
		Variables.setLocalVariables(provider, Variables.removeLocals(user));
	}

	/**
	 * Returns the internal value of the requested variable.
	 * <p>
	 * <b>Do not modify the returned value!</b>
	 * <p>
	 * This does not take into consideration default variables. You must use get methods from {@link ch.njol.skript.lang.Variable}
	 *
	 * @param name the variable's name.
	 * @param event if {@code local} is {@code true}, this is the event
	 *                 the local variable resides in.
	 * @param local if this variable is a local or global variable.
	 * @return an {@link Object} for a normal variable
	 * or a {@code Map<String, Object>} for a list variable,
	 * or {@code null} if the variable is not set.
	 */
	// TODO don't expose the internal value, bad API
	@Nullable
	public static Object getVariable(String name, @Nullable Event event, boolean local) {
		String n;
		if (caseInsensitiveVariables) {
			n = name.toLowerCase(Locale.ENGLISH);
		} else {
			n = name;
		}

		if (local) {
			VariableMap map = event instanceof LocalVariableHoldingEvent lvEvent ? lvEvent.getLocals() : localVariables.get(event);
			if (map == null)
				return null;

			return map.getVariable(n);
		} else {
			return variables.getVariable(n);
		}
	}

	/**
	 * Returns an iterator over the values of this list variable.
	 *
	 * @param name the variable's name. This must be the name of a list variable, ie. it must end in *.
	 * @param event if {@code local} is {@code true}, this is the event
	 *                 the local variable resides in.
	 * @param local if this variable is a local or global variable.
	 * @return an {@link Iterator} of {@link Pair}s, containing the {@link String} index and {@link Object} value of the
	 * 			elements of the list. An empty iterator is returned if the variable does not exist.
	 */
	public static Iterator<Pair<String, Object>> getVariableIterator(String name, boolean local, @Nullable Event event) {
		assert name.endsWith("*");
		Object val = getVariable(name, event, local);
		String subName = StringUtils.substring(name, 0, -1);

		if (val == null)
			return new EmptyIterator<>();
		assert val instanceof Map;
		// temporary list to prevent CMEs
		@SuppressWarnings("unchecked")
		Iterator<String> keys = new ArrayList<>(((Map<String, Object>) val).keySet()).iterator();
		return new Iterator<>() {
			@Nullable
			private String key;
			@Nullable
			private Object next = null;

			@Override
			public boolean hasNext() {
				if (next != null)
					return true;
				while (keys.hasNext()) {
					key = keys.next();
					if (key != null) {
						next = Variable.convertIfOldPlayer(subName + key, local, event, Variables.getVariable(subName + key, event, local));
						if (next != null && !(next instanceof Map))
							return true;
					}
				}
				next = null;
				return false;
			}

			@Override
			public Pair<String, Object> next() {
				if (!hasNext())
					throw new NoSuchElementException();
				Pair<String, Object> n = new Pair<>(key, next);
				next = null;
				return n;
			}

			@Override
			public void remove() {
				if (key == null)
					throw new IllegalStateException();
				Variables.deleteVariable(key, event, local);
			}
		};
	}

	/**
	 * Deletes a variable.
	 *
	 * @param name the variable's name.
	 * @param event if {@code local} is {@code true}, this is the event
	 *                 the local variable resides in.
	 * @param local if this variable is a local or global variable.
	 */
	public static void deleteVariable(String name, @Nullable Event event, boolean local) {
		setVariable(name, null, event, local);
	}

	/**
	 * Sets a variable.
	 *
	 * @param name the variable's name.
	 *                Can be a "list variable::*", but only to delete it.
	 *                Setting one to a value does nothing.
	 * @param value The variable's value. Use {@code null}
	 *                 to delete the variable.
	 * @param event if {@code local} is {@code true}, this is the event
	 *                 the local variable resides in.
	 * @param local if this variable is a local or global variable.
	 */
	public static void setVariable(String name, @Nullable Object value, @Nullable Event event, boolean local) {
		if (caseInsensitiveVariables) {
			name = name.toLowerCase(Locale.ENGLISH);
		}

		// Check if conversion is needed due to ClassInfo#getSerializeAs
		// we do this even for locals because of conversions like Slot->Itemstack
		if (value != null) {
			// names are computed at runtime, so {a::%arg%} can end up as a list name
			if (name.endsWith(Variable.SEPARATOR + "*"))
				return;

			ClassInfo<?> ci = Classes.getSuperClassInfo(value.getClass());
			Class<?> sas = ci.getSerializeAs();

			if (sas != null) {
				value = Converters.convert(value, sas);
				assert value != null : ci + ", " + sas;
			}
		}

		if (local) {
			assert event != null : name;

			// Get the variables map and set the variable in it
			VariableMap map = getOrCreateLocals(event);
			map.setVariable(name, value);
		} else {
			setVariable(name, value);
		}
	}

	/**
	 * Sets the given global variable name to the given value.
	 *
	 * @param name the variable name.
	 * @param value the value, or {@code null} to delete the variable.
	 */
	static void setVariable(String name, @Nullable Object value) {
		SerializedVariable serialized = serializeChange(name, value);
		if (serialized == null) {
			variables.setVariable(name, value);
			return;
		}

		saveOrderLock.lock();
		try {
			variables.setVariable(name, value);
			saveQueue.add(serialized);
		} finally {
			saveOrderLock.unlock();
		}
	}

	/**
	 * Sets a list to the following keys and values, replacing the whole list at once.
	 * This is two to three times as fast as deleting the list and calling
	 * {@link #setVariable(String, Object, Event, boolean)} for each element.
	 * For global variables, readers on other threads can briefly see a mix of the old list and the new one.
	 *
	 * @param listName The full name of the list, without ::* on the end.
	 * @param keys     The keys to use for the list's values, in any order. If a key repeats, the last value wins.
	 * @param values   The values to use in the list, one per key. This array is not modified.
	 */
	public static void setList(String listName, @NotNull String @NotNull [] keys, @NotNull Object @NotNull [] values, @Nullable Event event, boolean local) {
		assert keys.length == values.length : keys.length + " keys, " + values.length + " values";
		// keys like a::b (from keyed recursive lists) make nested lists, which the bulk path can't build.
		// list names ending in : are the same, as a:: + b splits into a and :b, so that's really a different list.
		// a key of * would be a value named like the list, which setVariable drops.
		if (hasUnsupportedKey(keys) || listName.endsWith(":")) {
			setVariable(listName + Variable.SEPARATOR + "*", null, event, local);
			for (int i = 0; i < keys.length; i++)
				setVariable(listName + Variable.SEPARATOR + keys[i], values[i], event, local);
			return;
		}
		if (caseInsensitiveVariables) {
			listName = listName.toLowerCase(Locale.ENGLISH);
			keys = lowercased(keys); // same array back if no key changed
		}

		ClassInfo<?> lastCI = null;
		Class<?> lastClass = null;
		boolean copied = false;
		for (int i = 0; i < values.length; i++) {
			assert values[i] != null : "null value for key " + keys[i];
			// Check if conversion is needed due to ClassInfo#getSerializeAs
			// we do this even for locals because of conversions like Slot->ItemStack
			Class<?> valueClass = values[i].getClass();
			ClassInfo<?> ci = valueClass == lastClass ? lastCI : Classes.getSuperClassInfo(valueClass);
			Class<?> sas = ci.getSerializeAs();

			if (sas != null) {
				Object newValue = Converters.convert(values[i], sas);
				assert newValue != null : ci + ", " + sas;
				if (!copied) { // don't modify the caller's array
					values = values.clone();
					copied = true;
				}
				values[i] = newValue;
			}
			lastClass = valueClass;
			lastCI = ci;
		}

		if (local) {
			assert event != null;
			getOrCreateLocals(event).setList(listName, keys, values);
		} else {
			setGlobalList(listName, keys, values);
		}
	}

	/**
	 * Sets the given global list to the given keys and values, taking {@link #saveOrderLock} once for the whole list.
	 * The changes saved are the same as deleting the list and setting each element would save.
	 *
	 * @param listName the list's name, without ::* on the end, already lowercased if needed.
	 * @param keys the keys, already lowercased if needed and without separators.
	 * @param values the values, already converted with {@link ClassInfo#getSerializeAs()}.
	 */
	private static void setGlobalList(String listName, String[] keys, Object[] values) {
		String prefix = listName + Variable.SEPARATOR;
		SerializedVariable clear = serializeChange(prefix + "*", null);
		if (clear == null) { // every name here starts the same way, so none of them get saved
			variables.setList(listName, keys, values);
			return;
		}

		SerializedVariable[] elements = new SerializedVariable[keys.length];
		for (int i = 0; i < keys.length; i++)
			elements[i] = serializeChange(prefix + keys[i], values[i]);

		saveOrderLock.lock();
		try {
			variables.setList(listName, keys, values);
			saveQueue.add(clear);
			for (SerializedVariable element : elements)
				saveQueue.add(element);
		} finally {
			saveOrderLock.unlock();
		}
	}

	private static boolean hasUnsupportedKey(String[] keys) {
		for (String key : keys) {
			if (key.equals("*") || key.contains(Variable.SEPARATOR))
				return true;
		}
		return false;
	}

	/**
	 * Lowercases each key, returning the same array if none of them changed.
	 *
	 * @param keys the strings to lowercase
	 * @return the lowercased keys array (a new array if any were uppercase, or the existing array)
	 */
	private static String[] lowercased(String[] keys) {
		String[] result = keys;
		for (int i = 0; i < keys.length; i++) {
			String lower = keys[i].toLowerCase(Locale.ENGLISH);
			//noinspection StringEquality
			if (lower != keys[i]) { // toLowerCase returns the same instance when nothing changes
				if (result == keys)
					result = keys.clone(); // first change: copy, so the caller's array is untouched
				result[i] = lower;
			}
		}
		return result;
	}

	// helper to get/create a local variable map
	private static VariableMap getOrCreateLocals(Event event) {
		VariableMap map;
		if (event instanceof LocalVariableHoldingEvent lvEvent) {
			map = lvEvent.getLocals();
			if (map == null) {
				map = VariableMap.local();
				lvEvent.setLocals(map);
			}
		} else {
			map = localVariables.computeIfAbsent(event, e -> VariableMap.local());
		}
		return map;
	}

	/**
	 * Stores loaded variables while variable storages are being loaded.
	 * <p>
	 * Access must be synchronised.
	 */
	private static final SynchronizedReference<Map<String, NonNullPair<Object, VariablesStorage>>> TEMP_VARIABLES =
			new SynchronizedReference<>(new HashMap<>());

	/**
	 * The amount of variable conflicts between variable storages where
	 * a warning will be given, with any conflicts than this value, no more
	 * warnings will be given.
	 *
	 * @see #loadConflicts
	 */
	private static final int MAX_CONFLICT_WARNINGS = 50;

	/**
	 * Keeps track of the amount of variable conflicts between variable storages
	 * while loading.
	 */
	private static int loadConflicts = 0;

	/**
	 * Sets a variable and moves it to the appropriate database
	 * if the config was changed.
	 * <p>
	 * Must only be used while variables are loaded
	 * when Skript is starting. Must be called on Bukkit's main thread.
	 * This method directly invokes
	 * {@link VariablesStorage#save(String, String, byte[])},
	 * i.e. you should not be holding any database locks or such
	 * when calling this!
	 *
	 * @param name the variable name.
	 * @param value the variable value.
	 * @param source the storage the variable came from.
	 * @return Whether the variable was stored somewhere. Not valid while storages are loading.
	 */
	static boolean variableLoaded(String name, @Nullable Object value, VariablesStorage source) {
		assert Bukkit.isPrimaryThread(); // required by serialisation

		if (value == null)
			return false;

		// older versions could save values under list names, which can't exist
		if (name.endsWith(Variable.SEPARATOR + "*")) {
			Skript.warning("Skipping the stored variable '" + name + "', since variables can't have the same name as a list.");
			return false;
		}

		synchronized (TEMP_VARIABLES) {
			Map<String, NonNullPair<Object, VariablesStorage>> tvs = TEMP_VARIABLES.get();
			if (tvs != null) {
				NonNullPair<Object, VariablesStorage> existingVariable = tvs.get(name);

				// Check for conflicts with other storages
				conflict: if (existingVariable != null) {
					VariablesStorage existingVariableStorage = existingVariable.getSecond();

					if (existingVariableStorage == source) {
						// No conflict if from the same storage
						break conflict;
					}

					// Variable already loaded from another database, conflict
					loadConflicts++;

					// Warn if needed
					if (loadConflicts <= MAX_CONFLICT_WARNINGS) {
						Skript.warning("The variable {" + name + "} was loaded twice from different databases (" +
							existingVariableStorage.getUserConfigurationName() + " and " + source.getUserConfigurationName() +
							"), only the one from " + source.getUserConfigurationName() + " will be kept.");
					} else if (loadConflicts == MAX_CONFLICT_WARNINGS + 1) {
						Skript.warning("[!] More than " + MAX_CONFLICT_WARNINGS +
							" variables were loaded more than once from different databases, " +
							"no more warnings will be printed.");
					}

					// Remove the value from the existing variable's storage
					existingVariableStorage.save(name, null, null);
				}

				// Add to the loaded variables
				tvs.put(name, new NonNullPair<>(value, source));

				return false;
			}
		}

		saveOrderLock.lock();
		try {
			variables.setVariable(name, value);
		} finally {
			saveOrderLock.unlock();
		}

		// Move the variable to the right storage
		try {
			for (VariablesStorage variablesStorage : STORAGES) {
				if (variablesStorage.accept(name)) {
					if (variablesStorage != source) {
						// Serialize and set value in new storage
						Value serializedValue = serialize(value);
						if (serializedValue == null) {
							variablesStorage.save(name, null, null);
						} else {
							variablesStorage.save(name, serializedValue.type, serializedValue.data);
						}

						// Remove from old storage
						if (value != null)
							source.save(name, null, null);
					}
					return true;
				}
			}
		} catch (Exception e) {
			//noinspection ThrowableNotThrown
			Skript.exception(e, "Error saving variable named " + name);
		}

		return false;
	}

	/**
	 * Stores loaded variables into the variables map
	 * and the appropriate databases.
	 *
	 * @return the amount of variables
	 * that don't have a storage that accepts them.
	 */
	@SuppressWarnings("null")
	private static int onStoragesLoaded() {
		if (loadConflicts > MAX_CONFLICT_WARNINGS)
			Skript.warning("A total of " + loadConflicts + " variables were loaded more than once from different databases");

		Skript.debug("Databases loaded, setting variables...");

		synchronized (TEMP_VARIABLES) {
			Map<String, NonNullPair<Object, VariablesStorage>> tvs = TEMP_VARIABLES.get();
			TEMP_VARIABLES.set(null);
			assert tvs != null;

			saveOrderLock.lock();
			try {
				// Calculate the amount of variables that don't have a storage
				int unstoredVariables = 0;
				for (Entry<String, NonNullPair<Object, VariablesStorage>> tv : tvs.entrySet()) {
					if (!variableLoaded(tv.getKey(), tv.getValue().getFirst(), tv.getValue().getSecond()))
						unstoredVariables++;
				}

				for (VariablesStorage variablesStorage : STORAGES)
					variablesStorage.allLoaded();

				Skript.debug("Variables set. Queue size = " + saveQueue.size());

				return unstoredVariables;
			} finally {
				saveOrderLock.unlock();
			}
		}
	}

	/**
	 * Creates a {@link SerializedVariable} from the given variable name
	 * and value.
	 * <p>
	 * Must be called from Bukkit's main thread.
	 *
	 * @param name the variable name.
	 * @param value the value.
	 * @return the serialized variable.
	 */
	public static SerializedVariable serialize(String name, @Nullable Object value) {
		assert Bukkit.isPrimaryThread();

		// First, serialize the variable.
		SerializedVariable.Value var;
		try {
			var = serialize(value);
		} catch (Exception e) {
			throw Skript.exception(e, "Error saving variable named " + name);
		}

		return new SerializedVariable(name, var);
	}

	/**
	 * Serializes the given value.
	 * <p>
	 * Must be called from Bukkit's main thread.
	 *
	 * @param value the value to serialize.
	 * @return the serialized value.
	 */
	public static SerializedVariable.@Nullable Value serialize(@Nullable Object value) {
		assert Bukkit.isPrimaryThread();

		return Classes.serialize(value);
	}

	/**
	 * Serializes a variable change, ready for the {@link #saveQueue}.
	 * This should be done before taking {@link #saveOrderLock}, as it can be slow.
	 *
	 * @param name the variable name.
	 * @param value the value of the variable.
	 * @return the serialized change, or {@code null} if this variable doesn't get saved.
	 */
	private static @Nullable SerializedVariable serializeChange(String name, @Nullable Object value) {
		if (name.startsWith(Variable.EPHEMERAL_VARIABLE_TOKEN))
			return null;
		return serialize(name, value);
	}

	/**
	 * The queue of serialized variables that have not yet been written
	 * to the storage.
	 */
	static final BlockingQueue<SerializedVariable> saveQueue = new LinkedBlockingQueue<>();

	/**
	 * Whether the {@link #saveThread} should be stopped.
	 */
	private static volatile boolean closed = false;

	/**
	 * The thread that saves variables, i.e. stores in the appropriate storage.
	 */
	private static final Thread saveThread = Skript.newThread(() -> {
		while (!closed) {
			try {
				// Save one variable change
				SerializedVariable variable = saveQueue.take();

				for (VariablesStorage variablesStorage : STORAGES) {
					if (variablesStorage.accept(variable.name)) {
						variablesStorage.save(variable);

						break;
					}
				}
			} catch (InterruptedException ignored) {}
		}
	}, "Skript variable save thread");

	/**
	 * Closes the variable systems, by stopping the {@link #saveThread} once all changes have been passed on to storages.
	 */
	public static void close() {
		// First, make sure all variables are saved
		while (saveQueue.size() > 0) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException ignored) {}
		}

		// Then we can safely interrupt and stop the thread
		closed = true;
		saveThread.interrupt();
	}

	/**
	 * Gets the amount of variables currently on the server.
	 *
	 * @return the amount of variables.
	 */
	public static int numVariables() {
		return (int) variables.size();
	}

}
