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
import ch.njol.skript.registrations.Classes;
import ch.njol.util.Kleenean;
import ch.njol.util.Pair;
import ch.njol.util.StringUtils;
import ch.njol.util.coll.iterator.EmptyIterator;
import ch.njol.yggdrasil.Yggdrasil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.event.Event;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.lang.converter.Converters;
import org.skriptlang.skript.variables.VariableMap;
import org.skriptlang.skript.variables.storage.VariableStorage;
import org.skriptlang.skript.variables.storage.VariableStorageManager;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

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

	// Register some things with Yggdrasil
	static {
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
	 * Registers a storage type, which users then pick by one of its names with the {@code type} option
	 * of a database in the config.
	 *
	 * @param addon       the addon registering the type
	 * @param storage     the class of the storages it creates
	 * @param constructor creates a storage, given the registering addon and the type name the config used
	 * @param names       the names users can give as the {@code type}, such as {@code csv}; matched ignoring case
	 * @param <T>         the class of the storages it creates
	 * @return true if the type was registered, false if one of its names is already used by another type
	 * @throws IllegalArgumentException if the class, or a related one, is already registered, or no names are given
	 * @throws IllegalStateException    if variables have already been loaded
	 */
	public static <T extends VariableStorage> boolean registerStorage(SkriptAddon addon, Class<T> storage,
																	  BiFunction<SkriptAddon, String, T> constructor,
																	  String... names) {
		registerBuiltInStorages();
		return STORAGE_MANAGER.register(addon, storage, constructor, names);
	}

	/**
	 * Storages were rewritten, and storages made for the old API can't be used anymore.
	 *
	 * @throws UnsupportedOperationException always
	 * @deprecated Extend {@link VariableStorage} and use {@link #registerStorage(SkriptAddon, Class, BiFunction, String...)}.
	 */
	@Deprecated(since = "INSERT VERSION", forRemoval = true)
	public static boolean registerStorage(Class<?> storage, String... names) {
		throw new UnsupportedOperationException("Can't register the variable storage " + storage.getName()
			+ ": storages must now extend " + VariableStorage.class.getName()
			+ " and be registered with Variables.registerStorage(SkriptAddon, Class, BiFunction, String...)");
	}

	private static boolean builtInStoragesRegistered;

	private static synchronized void registerBuiltInStorages() {
		if (builtInStoragesRegistered)
			return;
		builtInStoragesRegistered = true;
		STORAGE_MANAGER.register(Skript.instance(), FlatFileStorage.class, FlatFileStorage::new, "csv", "file", "flatfile");
	}

	/**
	 * Load the variables configuration and all variables.
	 * <p>
	 * May only be called once, when Skript is loading.
	 *
	 * @return whether the loading was successful.
	 */
	public static boolean load() {
		Config config = SkriptConfig.getConfig();
		if (config == null)
			throw new SkriptAPIException("Cannot load variables before the config");

		Node databases = config.getMainNode().get("databases");
		if (!(databases instanceof SectionNode section)) {
			Skript.error("The config is missing the required 'databases' section that defines where the variables are saved");
			return false;
		}

		Skript.closeOnDisable(Variables::close);
		registerBuiltInStorages();
		return STORAGE_MANAGER.load(section);
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
	 * The {@link VariableMap} storing global variables. This is thread-safe, so reading it doesn't need a lock.
	 * Changes go through {@link #STORAGE_MANAGER}, which saves them.
	 */
	static final VariableMap variables = VariableMap.concurrent();

	/**
	 * Saves changes to {@link #variables} to the storages configured in the config.
	 */
	private static final VariableStorageManager STORAGE_MANAGER =
		new VariableStorageManager(variables, Variables::serialize, Classes::deserialize, Variables::runOnMainThread);

	/**
	 * Runs a task on the main thread later, in the order tasks are given. Does nothing once Skript is disabled.
	 */
	private static void runOnMainThread(Runnable task) {
		Skript skript = Skript.getInstance();
		try {
			Bukkit.getScheduler().runTask(skript, task);
		} catch (RuntimeException e) {
			// can't schedule once disabled, but storages have stopped receiving changes by then anyway
			if (skript.isEnabled())
				throw e;
		}
	}

	/**
	 * @return the manager of the storages that save global variables
	 */
	@ApiStatus.Internal
	public static VariableStorageManager getStorageManager() {
		return STORAGE_MANAGER;
	}

	/**
	 * A map storing all local variables,
	 * indexed by their {@link Event}.
	 */
	private static final Map<Event, VariableMap> localVariables = new ConcurrentHashMap<>();

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
			// key moves ahead whenever hasNext is called, so remove needs its own copy of what next returned
			@Nullable
			private String lastReturned;

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
				lastReturned = key;
				next = null;
				return n;
			}

			@Override
			public void remove() {
				if (lastReturned == null)
					throw new IllegalStateException();
				Variables.deleteVariable(subName + lastReturned, event, local);
				lastReturned = null;
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
		STORAGE_MANAGER.set(name, value);
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
	 * Sets the given global list to the given keys and values.
	 * The changes saved are the same as deleting the list and setting each element would save.
	 *
	 * @param listName the list's name, without ::* on the end, already lowercased if needed.
	 * @param keys the keys, already lowercased if needed and without separators.
	 * @param values the values, already converted with {@link ClassInfo#getSerializeAs()}.
	 */
	private static void setGlobalList(String listName, String[] keys, Object[] values) {
		STORAGE_MANAGER.setList(listName, keys, values);
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
	 * Saves what is left to save and closes the storages. Changes made afterwards are no longer saved.
	 */
	public static void close() {
		STORAGE_MANAGER.close();
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
