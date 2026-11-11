package org.skriptlang.skript.variables.storage;

import ch.njol.skript.variables.SerializedVariable;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.variables.VariableMap;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Connects Skript's global variables to the storages that save them.
 * <p>
 * Skript keeps every global variable in one in-memory {@link VariableMap} and serves all reads from it.
 * This class does everything around saving: it keeps the registered storage types, creates and loads the storages
 * configured in the {@code databases} section, routes every change to the storage that saves it, applies
 * changes that storages report, and shuts the storages down.
 * <p>
 * Routing: a variable is saved to the first storage, in config order, that {@link VariableStorage#accept accepts}
 * its name. Names starting with {@code -} are never saved. The same order is the order in which storages are locked
 * when a change has to lock several of them, so that two such changes can't deadlock.
 * <p>
 * {@code Variables} holds the one instance, built from its global map, its serializer and deserializer,
 * and the server's main thread. Tests build their own, with a plain map and test storages.
 */
@ApiStatus.Internal
public final class VariableStorageManager {

	private final VariableMap globals;
	private final Function<@Nullable Object, SerializedVariable.@Nullable Value> serializer;
	private final BiFunction<String, byte[], @Nullable Object> deserializer;
	private final Executor mainThread;

	// registered storage types, in registration order.
	private final List<StorageType<?>> types = new ArrayList<>();
	// the loaded storages in config order, which is the routing and locking order.
	private volatile @Unmodifiable List<VariableStorage> storages = List.of();
	// whether storages have been set up, after which no more types can be registered.
	private boolean started;
	// whether the storages have been shut down, after which changes only reach the map
	private volatile boolean closed;

	/**
	 * @param globals      the map holding every global variable, which reads are served from
	 * @param serializer   serializes a value for saving, on the thread that sets it; null for a deleted variable
	 * @param deserializer turns a stored value's type and data back into a value, or null if it can't
	 * @param mainThread   runs tasks on the server's main thread, for changes that storages report
	 */
	public VariableStorageManager(VariableMap globals,
								  Function<@Nullable Object, SerializedVariable.@Nullable Value> serializer,
								  BiFunction<String, byte[], @Nullable Object> deserializer,
								  Executor mainThread) {
		this.globals = globals;
		this.serializer = serializer;
		this.deserializer = deserializer;
		this.mainThread = mainThread;
	}

	// ---------------------------- REGISTRATION ----------------------------

	/**
	 * Registers a storage type, which users then pick by one of its names with the {@code type} option
	 * of a section in the config's {@code databases}.
	 *
	 * @param addon       the addon registering the type
	 * @param storage     the class of the storages it creates
	 * @param constructor creates a storage, given the registering addon and the type name the config used
	 * @param names       the names users can give as the {@code type}, such as {@code csv}; matched ignoring case
	 * @param <T>         the class of the storages it creates
	 * @return true if the type was registered, false if one of its names is already used by another type
	 * @throws IllegalArgumentException if this class, a subclass or a superclass of it is already registered,
	 *                                  or no names (or blank ones) are given
	 * @throws IllegalStateException    if storages have already been set up, so the type could never be used
	 */
	public synchronized <T extends VariableStorage> boolean register(SkriptAddon addon, Class<T> storage,
																	 BiFunction<SkriptAddon, String, T> constructor,
																	 String... names) {
		if (started)
			throw new IllegalStateException("Storage types must be registered before variables are loaded: " + storage.getName());
		StorageType<T> type = new StorageType<>(addon, storage, constructor, names); // checks the names
		for (StorageType<?> registered : types) {
			Class<?> other = registered.storageClass();
			if (other.isAssignableFrom(storage) || storage.isAssignableFrom(other)) {
				throw new IllegalArgumentException("Can't register " + storage.getName() + " as a storage type, since "
					+ (other == storage ? "it is" : "the related class " + other.getName() + " is") + " already registered");
			}
		}
		for (StorageType<?> registered : types) {
			if (registered.sharesNameWith(type))
				return false;
		}
		types.add(type);
		return true;
	}

	/**
	 * @param configuredType a {@code type} from the config
	 * @return the registered type with that name, ignoring case, or null if there is none
	 */
	synchronized @Nullable StorageType<?> typeFor(String configuredType) {
		for (StorageType<?> type : types) {
			if (type.matches(configuredType))
				return type;
		}
		return null;
	}

	/**
	 * @return the registered storage types, in registration order
	 */
	synchronized @Unmodifiable List<StorageType<?>> types() {
		return List.copyOf(types);
	}

	// ---------------------------- STORAGES ----------------------------

	/**
	 * Sets the loaded storages, in config order, and stops new types from being registered.
	 * Called once, when loading is done; package-private so tests can set storages without a config.
	 *
	 * @param loaded the storages that loaded, in config order
	 * @throws IllegalStateException if storages were already set
	 */
	synchronized void setStorages(List<VariableStorage> loaded) {
		if (started)
			throw new IllegalStateException("Storages have already been set up");
		started = true;
		storages = List.copyOf(loaded);
	}

	/**
	 * @return the loaded storages in config order, which is the order names are routed and storages are locked in.
	 * Empty before variables are loaded.
	 */
	public @Unmodifiable List<VariableStorage> storages() {
		return storages;
	}

	/**
	 * @return whether the storages have been shut down, after which changes are no longer saved
	 */
	public boolean isClosed() {
		return closed;
	}

}
