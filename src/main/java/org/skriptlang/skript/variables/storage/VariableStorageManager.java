package org.skriptlang.skript.variables.storage;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.variables.SerializedVariable;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.variables.VariableList;
import org.skriptlang.skript.variables.VariableMap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
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
	// whether loading has begun, after which no more types can be registered either.
	private boolean loading;
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
		if (started || loading)
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

	// ---------------------------- ROUTING ----------------------------

	private @Nullable VariableStorage route(String name) {
		if (name.startsWith(Variable.EPHEMERAL_VARIABLE_TOKEN) || closed)
			return null;
		for (VariableStorage storage : storages) {
			if (storage.accept(name)) {
				// a storage that failed to load still owns its names, so they don't end up saved somewhere else
				return storage.loadFailed ? null : storage;
			}
		}
		return null;
	}

	public void set(String name, @Nullable Object value) {
		if (name.endsWith(Variable.SEPARATOR + "*")) {
			if (value != null) {
				throw new IllegalArgumentException("Cannot set a list to a non-null value! (var: '" + name + "', value: " + value + ")");
			}
			// clear list
			clearList(name.substring(0, name.length() - 1));
			return;
		}
		VariableStorage storage = route(name);
		if (storage == null) {
			// no owner, just put in map and call it there
			globals.setVariable(name, value);
			return;
		}
		// serialize first. things that fail to serialize will be removed from the storage but stay in the map
		SerializedVariable.Value serialized = value == null ? null : serializer.apply(value);
		// write it to the storage (it will be put in the map under the storage lock to maintain coherence
		storage.queue().write(name, serialized, () -> globals.setVariable(name, value));
	}

	private List<VariableStorage> involvedIn(String listPrefix) {
		if (listPrefix.startsWith(Variable.EPHEMERAL_VARIABLE_TOKEN) || closed) {
			return List.of();
		}
		List<VariableStorage> involvedStorages = new ArrayList<>();
		for (VariableStorage storage : storages) {
			if (storage.couldAccept(listPrefix)) {
				involvedStorages.add(storage);
				// if this storage owns the whole list, then we don't care about the rest
				if (storage.accept(listPrefix))
					break;
			}
		}
		return involvedStorages;
	}

	private void underLocks(List<VariableStorage> storages, Runnable action) {
		long[] stamps = new long[storages.size()];
		int locked = 0;
		try {
			// lock them all
			for (; locked < storages.size(); locked++)
				stamps[locked] = storages.get(locked).queue().lockExclusive();
			// action!
			action.run();
		} finally {
			// unlock in reverse order
			while (locked-- > 0)
				storages.get(locked).queue().unlockExclusive(stamps[locked]);
		}
	}

	private void clearList(String listPrefix) {
		// we have to find all storages that can hold parts of this list
		// (e.g. clear a::*, storage 1 holds a::b::*, storage 2 holds a::c::*, both need to be involved here).
		List<VariableStorage> involvedStorages = involvedIn(listPrefix);
		if (involvedStorages.isEmpty()) {
			globals.setVariable(listPrefix + "*", null);
			return;
		}
		underLocks(involvedStorages, () -> {
			// clear each
			for (VariableStorage storage : involvedStorages) {
				if (!storage.loadFailed)
					storage.queue().recordClear(listPrefix, true);
			}
			globals.setVariable(listPrefix + "*", null);
		});
	}

	/**
	 * @param listName the list's name, without {@code ::*}, lowercased if variables are case-insensitive
	 * @param keys     the keys, lowercased if needed, without separators; if a key repeats, the last value wins
	 * @param values   the values, one per key, already converted for saving; none of them null
	 */
	public void setList(String listName, String[] keys, Object[] values) {
		String listPrefix = listName + Variable.SEPARATOR;
		// we have to find all storages that can hold parts of this list
		// (e.g. clear a::*, storage 1 holds a::b::*, storage 2 holds a::c::*, both need to be involved here).
		List<VariableStorage> involvedStorages = involvedIn(listPrefix);
		if (involvedStorages.isEmpty()) {
			globals.setList(listName, keys, values);
			return;
		}

		// route and serialize outside the locks, since serializing can be slow
		String[] names = new String[keys.length];
		VariableStorage[] owners = new VariableStorage[keys.length]; // null if the element isn't saved
		SerializedVariable.Value[] serialized = new SerializedVariable.Value[keys.length];
		// usual case: one storage owns the whole list, so it owns every element
		VariableStorage onlyOwner = involvedStorages.size() == 1 && involvedStorages.getFirst().accept(listPrefix)
			? involvedStorages.getFirst() : null;
		// determine owner for each key
		for (int i = 0; i < keys.length; i++) {
			names[i] = listPrefix + keys[i];
			if (onlyOwner != null) {
				owners[i] = onlyOwner;
			} else {
				// storages that aren't involved can't accept any name in the list, so the owner is one of these
				for (VariableStorage storage : involvedStorages) {
					if (storage.accept(names[i])) {
						owners[i] = storage;
						break;
					}
				}
			}
			// owned by a storage that failed to load: not saved
			if (owners[i] != null && owners[i].loadFailed)
				owners[i] = null;
			// don't serialize if we aren't gonna store it
			if (owners[i] != null)
				serialized[i] = serializer.apply(values[i]);
		}

		underLocks(involvedStorages, () -> {
			// clear first! the changes have to come after the clear, or draining drops them
			for (VariableStorage storage : involvedStorages) {
				if (!storage.loadFailed)
					storage.queue().recordClear(listPrefix, true);
			}
			globals.setList(listName, keys, values);
			// in order, so a repeated key's last value wins, as in the map
			for (int i = 0; i < names.length; i++) {
				if (owners[i] != null)
					owners[i].queue().putPending(names[i], serialized[i]);
			}
		});
	}

	// ---------------------------- STARTUP ----------------------------

	public boolean load(SectionNode databases) {
		synchronized (this) {
			if (started || loading)
				throw new IllegalStateException("Variables have already been loaded");
			loading = true; // no more types can be registered
		}

		boolean successful = true;
		List<VariableStorage> configured = new ArrayList<>();
		// backup folders are named after the storage, so two names mapping to one folder would mix their backups.
		// ignoring case, since some file systems do
		Map<String, String> backupNames = new HashMap<>();
		try {
			for (Node node : databases) {
				// parse each database
				SkriptLogger.setNode(node);
				if (!(node instanceof SectionNode section)) {
					Skript.error("Invalid line in databases: databases must be defined as sections");
					successful = false;
					continue;
				}
				String name = section.getKey();
				assert name != null;

				// check type
				String typeName = section.getValue("type");
				if (typeName == null) {
					Skript.error("Missing entry 'type' in database definition");
					successful = false;
					continue;
				}
				if (typeName.equalsIgnoreCase("disabled") || typeName.equalsIgnoreCase("none"))
					continue;
				StorageType<?> type = typeFor(typeName);
				if (type == null) {
					Skript.error("Invalid database type '" + typeName + "'");
					successful = false;
					continue;
				}

				// check for name repeats
				String clash = backupNames.putIfAbsent(StorageBackups.safeName(name).toLowerCase(Locale.ENGLISH), name);
				if (clash != null) {
					Skript.error("The databases '" + clash + "' and '" + name + "' would share a backup folder. Please rename one of them.");
					successful = false;
					continue;
				}

				// create storage and loadConfig
				VariableStorage storage;
				try {
					storage = type.create(typeName);
				} catch (RuntimeException e) {
					//noinspection ThrowableNotThrown
					Skript.exception(e, "Failed to create the database '" + name + "' of type '" + typeName + "'");
					successful = false;
					continue;
				}
				if (!storage.loadConfig(section)) { // reports its own errors and releases its file
					successful = false;
					continue;
				}
				configured.add(storage);
			}
		} finally {
			SkriptLogger.setNode(null);
		}

		// finish loading (intentional not to short-circuit)
		return loadStorages(configured) && successful;
	}

	boolean loadStorages(List<VariableStorage> configured) {
		synchronized (this) {
			if (started)
				throw new IllegalStateException("Variables have already been loaded");
			loading = true;
		}

		// start loading variables
		boolean successful = true;
		LoadProgress progress = new LoadProgress();
		Migration migration = new Migration(configured);
		for (VariableStorage storage : configured) {
			long start = System.nanoTime();
			if (Skript.logVeryHigh())
				Skript.info("Loading database '" + storage.getName() + "'...");

			Loader listener = new Loader(storage, progress, migration);
			try {
				storage.load(listener);
			} catch (IOException e) {
				Skript.error("Failed to load the database '" + storage.getName() + "': " + e.getMessage()
					+ ". Its variables won't be saved until it loads after a restart.");
				failed(storage);
				successful = false;
				continue;
			} catch (RuntimeException e) {
				//noinspection ThrowableNotThrown
				Skript.exception(e, "Failed to load the database '" + storage.getName() + "'."
					+ " Its variables won't be saved until it loads after a restart.");
				failed(storage);
				successful = false;
				continue;
			}

			if (listener.unreadable > 0) {
				Skript.warning(listener.unreadable + " variables in the database '" + storage.getName() + "' couldn't be read,"
					+ " probably because their type no longer exists. They are left in the database as they are.");
			}
			if (Skript.logVeryHigh()) {
				Skript.info("Loaded " + listener.loaded + " variables from the database '" + storage.getName() + "' in "
					+ (TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start) / 100) / 10.0 + " seconds");
			}
		}

		// move variables to the storages that own them, before saving starts
		migration.resolve();

		// start saving threads for each successful storage
		for (VariableStorage storage : configured) {
			if (storage.loadFailed)
				continue;
			try {
				storage.startSaving();
			} catch (RuntimeException e) {
				//noinspection ThrowableNotThrown
				Skript.exception(e, "Failed to start saving the database '" + storage.getName() + "'."
					+ " Its variables won't be saved until the server restarts.");
				failed(storage);
				successful = false;
			}
		}

		// failed storages stay in the routing order, so the names they own aren't saved to a storage after them
		setStorages(configured);
		// only now, so a reported change is checked against the storages that are running
		for (VariableStorage storage : configured) {
			if (!storage.loadFailed)
				storage.startReceiving(reports);
		}

		if (configured.isEmpty()) {
			Skript.error("No databases to store variables are defined. Please enable at least the default database, even if you don't use variables at all.");
			return false;
		}
		if (configured.stream().noneMatch(storage -> storage.getPrefixes() == null)) {
			StringJoiner prefixes = new StringJoiner(", ");
			for (VariableStorage storage : configured) {
				assert storage.getPrefixes() != null;
				storage.getPrefixes().forEach(prefixes::add);
			}
			Skript.warning("No database saves every variable, so variables whose names don't start with one of these"
				+ " prefixes won't be saved: " + prefixes);
		}
		return successful;
	}

	private static void failed(VariableStorage storage) {
		storage.loadFailed = true;
		storage.stopReceiving();
		try {
			storage.close();
		} catch (RuntimeException e) {
			//noinspection ThrowableNotThrown
			Skript.exception(e, "Failed to close the database '" + storage.getName() + "'");
		} finally {
			storage.releaseFile();
		}
	}

	// helper for printing the variable loading progress
	private static final class LoadProgress {

		private final long intervalNanos = TimeUnit.SECONDS.toNanos(Skript.logNormal() ? 1 : 5);
		private long count;
		private long lastLog = System.nanoTime();

		void loaded() {
			count++;
			long now = System.nanoTime();
			if (now - lastLog >= intervalNanos) {
				lastLog = now;
				Skript.info("Loaded " + count + " variables so far...");
			}
		}

	}

	// storages load one at a time, on the thread loading variables, which is the main thread while Skript enables.
	// so this needs no locking, and can deserialize types that need the main thread
	private final class Loader implements LoadListener {

		private final VariableStorage source;
		private final LoadProgress progress;
		private final Migration migration;
		private int loaded;
		private int unreadable;

		Loader(VariableStorage source, LoadProgress progress, Migration migration) {
			this.source = source;
			this.progress = progress;
			this.migration = migration;
		}

		@Override
		public void loaded(String name, SerializedVariable.Value value) {
			// older versions could save values under list names, which can't exist
			if (name.endsWith(Variable.SEPARATOR + "*")) {
				Skript.warning("Skipping the stored variable '" + name + "', since variables can't have the same name as a list.");
				return;
			}
			Object deserialized = deserializer.apply(value.type, value.data);
			if (deserialized == null) {
				unreadable++; // left in the storage, so it's back once the type is
				return;
			}
			if (migration.keep(source, name, value))
				globals.setVariable(name, deserialized);
			loaded++;
			progress.loaded();
		}

	}

	// ---------------------------- MIGRATION ----------------------------

	private static final Runnable NO_MAP_WRITE = () -> {
	};

	// a variable loaded from a storage that doesn't own it, or loaded by more than one storage
	private static final class MisplacedVariable {

		private final VariableStorage owner;
		// the storage whose copy is in the map, null if the owner had its own copy
		private @Nullable VariableStorage keeper;
		private SerializedVariable.@Nullable Value value;
		// other storages with a copy, which is deleted
		private final List<VariableStorage> others = new ArrayList<>(1);

		MisplacedVariable(VariableStorage owner, @Nullable VariableStorage keeper, SerializedVariable.@Nullable Value value) {
			this.owner = owner;
			this.keeper = keeper;
			this.value = value;
		}

		void ownerHasIt() {
			if (keeper != null) {
				others.add(keeper);
				keeper = null;
				value = null;
			}
		}

		void deleteCopies(String name) {
			if (keeper != null && !keeper.loadFailed)
				keeper.queue().write(name, null, NO_MAP_WRITE);
			for (VariableStorage other : others) {
				if (!other.loadFailed)
					other.queue().write(name, null, NO_MAP_WRITE);
			}
		}

	}

	// only records the exceptions, so variables already in the storage they belong to cost nothing extra
	private final class Migration {

		private final List<VariableStorage> configured;
		private final Map<String, MisplacedVariable> misplaced = new HashMap<>();
		private int duplicates;
		private int unowned;

		Migration(List<VariableStorage> configured) {
			this.configured = configured;
		}

		// the same rule as routing, against the configured storages, since routing isn't live yet
		private @Nullable VariableStorage ownerOf(String name) {
			if (name.startsWith(Variable.EPHEMERAL_VARIABLE_TOKEN))
				return null;
			for (VariableStorage storage : configured) {
				if (storage.accept(name))
					return storage;
			}
			return null;
		}

		// whether the loaded value goes in the map: the owner's copy wins, otherwise the first one loaded
		boolean keep(VariableStorage source, String name, SerializedVariable.Value value) {
			VariableStorage owner = ownerOf(name);
			MisplacedVariable entry = misplaced.get(name);
			if (owner == source) {
				// we are the source of truth
				if (entry != null) {
					// if there's an imposter (sus) we overrule
					entry.ownerHasIt();
					duplicates++;
				}
				return true;
			}
			// we are not the owner atp
			if (owner == null) {
				// no real owner
				if (globals.getVariable(name) != null) {
					// if someone already loaded it, it's a duplicate and we leave the existing value be
					duplicates++;
					return false;
				}
				// no one loaded it, so we should load it and mark as unowned.
				unowned++;
				return true;
			}
			// there is an owner and it is not us
			if (entry != null) {
				// someone already marked this as misplaced, so just add ourselves to the list
				entry.others.add(source);
				duplicates++;
				return false;
			}
			if (globals.getVariable(name) != null) {
				// there is no entry but there is a value/owner, so we are the first misplaced copy
				// therefore create entry
				entry = new MisplacedVariable(owner, null, null);
				entry.others.add(source);
				misplaced.put(name, entry);
				duplicates++;
				return false;
			}
			// there is an owner but it did not load this var, so it's simply misplaced.
			misplaced.put(name, new MisplacedVariable(owner, source, value));
			return true;
		}

		// moves each misplaced variable to its owner, and deletes the other copies only once the owner has saved it
		void resolve() {
			Map<VariableStorage, List<String>> moves = new LinkedHashMap<>();
			int ownerFailed = 0;
			for (Map.Entry<String, MisplacedVariable> entry : misplaced.entrySet()) {
				MisplacedVariable variable = entry.getValue();
				if (variable.owner.loadFailed) {
					ownerFailed++; // left where it is, since the owner can't save it
				} else if (variable.keeper == null) {
					variable.deleteCopies(entry.getKey()); // the owner has its own copy
				} else {
					moves.computeIfAbsent(variable.owner, owner -> new ArrayList<>()).add(entry.getKey());
				}
			}

			int moved = 0;
			int notMoved = 0;
			for (Map.Entry<VariableStorage, List<String>> entry : moves.entrySet()) {
				VariableStorage owner = entry.getKey();
				List<String> names = entry.getValue();
				for (String name : names)
					owner.queue().write(name, misplaced.get(name).value, NO_MAP_WRITE); // reuses the stored bytes
				if (owner.flush()) {
					for (String name : names)
						misplaced.get(name).deleteCopies(name);
					moved += names.size();
				} else {
					// still queued for the owner, which keeps retrying. the copies stay until a later start
					// finds the owner has the variable
					notMoved += names.size();
				}
			}
			misplaced.clear();

			if (moved > 0)
				Skript.info("Moved " + moved + " variables to the databases they belong to");
			if (notMoved > 0) {
				Skript.warning(notMoved + " variables couldn't be moved to the databases they belong to, since saving to"
					+ " them failed. They're kept where they were, and are moved on a later start.");
			}
			if (ownerFailed > 0) {
				Skript.warning(ownerFailed + " variables weren't moved to the databases they belong to,"
					+ " since those failed to load.");
			}
			if (duplicates > 0)
				Skript.warning("A total of " + duplicates + " variables were loaded more than once from different databases");
			if (unowned > 0) {
				Skript.warning(unowned + " variables were loaded from databases that don't save them, since their names"
					+ " don't start with any database's prefix. They're left where they are, but changes to them won't be saved.");
			}
		}

	}

	// ---------------------------- REPORTED CHANGES ----------------------------

	// changes storages report from outside this server. applied on the main thread, in the order they were reported,
	// since some types can only be deserialized there
	private final VariableStorage.ChangeHandler reports = new VariableStorage.ChangeHandler() {

		@Override
		public void updated(VariableStorage source, String name, SerializedVariable.@Nullable Value value) {
			if (!closed)
				mainThread.execute(() -> applyUpdate(source, name, value));
		}

		@Override
		public void cleared(VariableStorage source, String listName) {
			if (!closed)
				mainThread.execute(() -> applyClear(source, listName));
		}

	};

	private void applyUpdate(VariableStorage source, String name, SerializedVariable.@Nullable Value value) {
		if (route(name) != source) { // closed, ephemeral, or saved by another storage here
			if (!closed)
				Skript.debug("Ignoring a change to '" + name + "' from the database '" + source.getName() + "', which doesn't save it");
			return;
		}
		Object deserialized = null;
		if (value != null) {
			deserialized = deserializer.apply(value.type, value.data);
			if (deserialized == null) {
				Skript.debug("Ignoring a change to '" + name + "' from the database '" + source.getName() + "', which couldn't be read");
				return;
			}
		}
		Object newValue = deserialized;
		// also drops any unsaved local change to it, so the storage's value isn't overwritten
		source.queue().externalWrite(name, () -> globals.setVariable(name, newValue));
	}

	private void applyClear(VariableStorage source, String listName) {
		String listPrefix = listName.substring(0, listName.length() - 1);
		List<VariableStorage> involved = involvedIn(listPrefix); // empty if closed or ephemeral
		if (!involved.contains(source)) {
			if (!closed)
				Skript.debug("Ignoring a clear of '" + listName + "' from the database '" + source.getName() + "', which doesn't save it");
			return;
		}
		ChangeQueue queue = source.queue();
		long stamp = queue.lockExclusive();
		try {
			// drops the unsaved local changes under the list, without asking the storage to delete it again
			queue.recordClear(listPrefix, false);
			if (involved.size() == 1 && source.accept(listPrefix)) {
				globals.setVariable(listName, null); // the whole list is this storage's
			} else {
				// only delete what this storage saves, so other storages' parts of the list stay
				List<String> owned = new ArrayList<>();
				collectOwned(source, listPrefix, globals.getVariable(listName), owned);
				for (String name : owned)
					globals.setVariable(name, null);
			}
		} finally {
			queue.unlockExclusive(stamp);
		}
	}

	private void collectOwned(VariableStorage source, String listPrefix, @Nullable Object list, List<String> owned) {
		if (!(list instanceof VariableList variables))
			return;
		for (Map.Entry<String, Object> child : variables.children().entrySet()) {
			String name = listPrefix + child.getKey();
			if (child.getValue() instanceof VariableList nested) {
				if (nested.ownValue() != null && route(name) == source)
					owned.add(name);
				String nestedPrefix = name + Variable.SEPARATOR;
				if (source.couldAccept(nestedPrefix))
					collectOwned(source, nestedPrefix, nested, owned);
			} else if (route(name) == source) {
				owned.add(name);
			}
		}
	}

}
