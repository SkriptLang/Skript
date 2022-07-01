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
package ch.njol.skript.variables;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.TreeMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Pattern;

import ch.njol.skript.log.SkriptLogger;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

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
import ch.njol.skript.registrations.Converters;
import ch.njol.skript.variables.DatabaseStorage.Type;
import ch.njol.skript.variables.SerializedVariable.Value;
import ch.njol.util.Closeable;
import ch.njol.util.Kleenean;
import ch.njol.util.NonNullPair;
import ch.njol.util.SynchronizedReference;
import ch.njol.yggdrasil.Yggdrasil;

/**
 * @author Peter Güttinger
 */
public abstract class Variables {
	private Variables() {}
	
	public final static short YGGDRASIL_VERSION = 1;
	
	public final static Yggdrasil yggdrasil = new Yggdrasil(YGGDRASIL_VERSION);

	public static boolean caseInsensitiveVariables = true;
	
	private final static String configurationSerializablePrefix = "ConfigurationSerializable_";
	static {
		yggdrasil.registerSingleClass(Kleenean.class, "Kleenean");
		yggdrasil.registerClassResolver(new ConfigurationSerializer<ConfigurationSerializable>() {
			{
				init(); // separate method for the annotation
			}
			
			@SuppressWarnings("unchecked")
			private void init() {
				// used by asserts
				info = (ClassInfo<? extends ConfigurationSerializable>) (ClassInfo<?>) Classes.getExactClassInfo(Object.class);
			}
			
			@SuppressWarnings({"unchecked"})
			@Override
			@Nullable
			public String getID(final @NonNull Class<?> c) {
				if (ConfigurationSerializable.class.isAssignableFrom(c) && Classes.getSuperClassInfo(c) == Classes.getExactClassInfo(Object.class))
					return configurationSerializablePrefix + ConfigurationSerialization.getAlias((Class<? extends ConfigurationSerializable>) c);
				return null;
			}
			
			@Override
			@Nullable
			public Class<? extends ConfigurationSerializable> getClass(final @NonNull String id) {
				if (id.startsWith(configurationSerializablePrefix))
					return ConfigurationSerialization.getClassByAlias(id.substring(configurationSerializablePrefix.length()));
				return null;
			}
		});
	}
	
	static List<VariablesStorage> storages = new ArrayList<>();
	
	public static boolean load() {
		assert variables.treeMap.isEmpty();
		assert variables.hashMap.isEmpty();
		assert storages.isEmpty();
		
		final Config c = SkriptConfig.getConfig();
		if (c == null)
			throw new SkriptAPIException("Cannot load variables before the config");
		final Node databases = c.getMainNode().get("databases");
		if (databases == null || !(databases instanceof SectionNode)) {
			Skript.error("The config is missing the required 'databases' section that defines where the variables are saved");
			return false;
		}
		
		Skript.closeOnDisable(new Closeable() {
			@Override
			public void close() {
				Variables.close();
			}
		});
		
		// reports once per second how many variables were loaded. Useful to make clear that Skript is still doing something if it's loading many variables
		final Thread loadingLoggerThread = new Thread() {
			@Override
			public void run() {
				while (true) {
					try {
						Thread.sleep(Skript.logNormal() ? 1000 : 5000); // low verbosity won't disable these messages, but makes them more rare
					} catch (final InterruptedException e) {}
					synchronized (tempVars) {
						final Map<String, NonNullPair<Object, VariablesStorage>> tvs = tempVars.get();
						if (tvs != null)
							Skript.info("Loaded " + tvs.size() + " variables so far...");
						else
							break;
					}
				}
			}
		};
		loadingLoggerThread.start();
		
		try {
			boolean successful = true;
			for (final Node node : (SectionNode) databases) {
				if (node instanceof SectionNode) {
					final SectionNode n = (SectionNode) node;
					final String type = n.getValue("type");
					if (type == null) {
						Skript.error("Missing entry 'type' in database definition");
						successful = false;
						continue;
					}
					
					final String name = n.getKey();
					assert name != null;
					final VariablesStorage s;
					if (type.equalsIgnoreCase("csv") || type.equalsIgnoreCase("file") || type.equalsIgnoreCase("flatfile")) {
						s = new FlatFileStorage(name);
					} else if (type.equalsIgnoreCase("mysql")) {
						s = new DatabaseStorage(name, Type.MYSQL);
					} else if (type.equalsIgnoreCase("sqlite")) {
						s = new DatabaseStorage(name, Type.SQLITE);
					} else {
						if (!type.equalsIgnoreCase("disabled") && !type.equalsIgnoreCase("none")) {
							Skript.error("Invalid database type '" + type + "'");
							successful = false;
						}
						continue;
					}
					
					final int x;
					synchronized (tempVars) {
						final Map<String, NonNullPair<Object, VariablesStorage>> tvs = tempVars.get();
						assert tvs != null;
						x = tvs.size();
					}
					final long start = System.currentTimeMillis();
					if (Skript.logVeryHigh())
						Skript.info("Loading database '" + node.getKey() + "'...");
					
					if (s.load(n))
						storages.add(s);
					else
						successful = false;
					
					final int d;
					synchronized (tempVars) {
						final Map<String, NonNullPair<Object, VariablesStorage>> tvs = tempVars.get();
						assert tvs != null;
						d = tvs.size() - x;
					}
					if (Skript.logVeryHigh())
						Skript.info("Loaded " + d + " variables from the database '" + n.getKey() + "' in " + ((System.currentTimeMillis() - start) / 100) / 10.0 + " seconds");
				} else {
					Skript.error("Invalid line in databases: databases must be defined as sections");
					successful = false;
				}
			}
			if (!successful)
				return false;
			
			if (storages.isEmpty()) {
				Skript.error("No databases to store variables are defined. Please enable at least the default database, even if you don't use variables at all.");
				return false;
			}
		} finally {
			SkriptLogger.setNode(null);

			// make sure to put the loaded variables into the variables map
			final int n = onStoragesLoaded();
			if (n != 0) {
				Skript.warning(n + " variables were possibly discarded due to not belonging to any database (SQL databases keep such variables and will continue to generate this warning, while CSV discards them).");
			}
			
			loadingLoggerThread.interrupt();
			
			saveThread.start();
		}
		return true;
	}
	
	@SuppressWarnings("null")
	private final static Pattern variableNameSplitPattern = Pattern.compile(Pattern.quote(Variable.SEPARATOR));
	
	@SuppressWarnings("null")
	public static String[] splitVariableName(final String name) {
		return variableNameSplitPattern.split(name);
	}
	
	final static ReadWriteLock variablesLock = new ReentrantReadWriteLock(true);
	/**
	 * must be locked with {@link #variablesLock}.
	 */
	final static VariablesMap variables = new VariablesMap();

	private final static Map<Event, VariablesMap> localVariables = new ConcurrentHashMap<>();
	
	/**
	 * Remember to lock with {@link #getReadLock()} and to not make any changes!
	 */
	static TreeMap<String, Object> getVariables() {
		return variables.treeMap;
	}
	
	/**
	 * Removes local variables associated with given event and returns them,
	 * if they exist.
	 * @param event Event.
	 * @return Local variables or null.
	 */
	@Nullable
	public static VariablesMap removeLocals(Event event) {
		return localVariables.remove(event);
	}
	
	/**
	 * Sets local variables associated with given event.
	 * If the given map is null, local variables for this event will be <b>removed</b> if they are present!
	 * Warning: this can overwrite local variables!
	 * @param event Event.
	 * @param map New local variables.
	 */
	public static void setLocalVariables(Event event, @Nullable Object map) {
		if (map != null) {
			localVariables.put(event, (VariablesMap) map);
		} else {
			removeLocals(event);
		}
	}

	/**
	 * Creates a copy of the VariablesMap for local variables in an event.
	 * @param event The event to copy local variables from.
	 * @return A VariablesMap copy for the local variables in an event.
	 */
	@Nullable
	public static Object copyLocalVariables(Event event) {
		VariablesMap from = localVariables.get(event);
		if (from == null)
			return null;
		VariablesMap copy = new VariablesMap();
		copy.hashMap.putAll(from.hashMap);
		copy.treeMap.putAll(from.treeMap);
		return copy;
	}
	
	/**
	 * Remember to lock with {@link #getReadLock()}!
	 */
	@SuppressWarnings("null")
	static Map<String, Object> getVariablesHashMap() {
		return Collections.unmodifiableMap(variables.hashMap);
	}
	
	@SuppressWarnings("null")
	static Lock getReadLock() {
		return variablesLock.readLock();
	}
	
	/**
	 * Returns the internal value of the requested variable.
	 * <p>
	 * <b>Do not modify the returned value!</b>
	 *
	 * @param name
	 * @return an Object for a normal Variable or a Map<String, Object> for a list variable, or null if the variable is not set.
	 */
	@Nullable
	public static Object getVariable(final String name, final @Nullable Event e, final boolean local) {
		String n = name;
        if (caseInsensitiveVariables) {
            n = name.toLowerCase(Locale.ENGLISH);
        }
        assert n != null;
	    if (local) {
			final VariablesMap map = localVariables.get(e);
			if (map == null)
				return null;
			return map.getVariable(n);
		} else {
			// Prevent race conditions from returning variables with incorrect values
			if (!changeQueue.isEmpty()) {
				for (VariableChange change : changeQueue) {
					if (change.name.equals(n))
						return change.value;
				}
			}
			
			try {
				variablesLock.readLock().lock();
				return variables.getVariable(n);
			} finally {
				variablesLock.readLock().unlock();
			}
		}
	}
	
	/**
	 * Sets a variable.
	 *
	 * @param name The variable's name. Can be a "list variable::*" (<tt>value</tt> must be <tt>null</tt> in this case)
	 * @param value The variable's value. Use <tt>null</tt> to delete the variable.
	 */
	public static void setVariable(final String name, @Nullable Object value, final @Nullable Event e, final boolean local) {
        String n = name;
        if (caseInsensitiveVariables) {
            n = name.toLowerCase(Locale.ENGLISH);
        }
        assert n != null;
	    if (value != null) {
			assert !n.endsWith("::*");
			final ClassInfo<?> ci = Classes.getSuperClassInfo(value.getClass());
			final Class<?> sas = ci.getSerializeAs();
			if (sas != null) {
				value = Converters.convert(value, sas);
				assert value != null : ci + ", " + sas;
			}
		}
		if (local) {
			assert e != null : n;
			VariablesMap map = localVariables.computeIfAbsent(e, event -> new VariablesMap());
			map.setVariable(n, value);
		} else {
			setVariable(n, value);
		}
	}
	
	static void setVariable(final String name, @Nullable final Object value) {
		boolean gotLock = variablesLock.writeLock().tryLock();
		if (gotLock) {
			try {
				variables.setVariable(name, value);
				saveVariableChange(name, value);
				processChangeQueue(); // Process all previously queued writes
			} finally {
				variablesLock.writeLock().unlock();
			}
		} else { // Can't block here, queue the change
			queueVariableChange(name, value);
		}
	}
	
	/**
	 * Changes to variables that have not yet been written.
	 */
	final static Queue<VariableChange> changeQueue = new ConcurrentLinkedQueue<>();
	
	/**
	 * A variable change name-value pair.
	 */
	private static class VariableChange {
		
		public final String name;
		@Nullable
		public final Object value;
		
		public VariableChange(String name, @Nullable Object value) {
			this.name = name;
			this.value = value;
		}
	}
	
	/**
	 * Queues a variable change. Only to be called when direct write is not
	 * possible, but thread cannot be allowed to block.
	 * @param name Variable name.
	 * @param value New value.
	 */
	private static void queueVariableChange(String name, @Nullable Object value) {
		changeQueue.add(new VariableChange(name, value));
	}
	
	/**
	 * Processes all entries in variable change queue. Note that caller MUST
	 * acquire write lock before calling this, then release it.
	 */
	static void processChangeQueue() {
		while (true) { // Run as long as we still have changes
			VariableChange change = changeQueue.poll();
			if (change == null)
				break;
			
			variables.setVariable(change.name, change.value);
			saveVariableChange(change.name, change.value);
		}
	}
	
	/**
	 * Stores loaded variables while variable storages are loaded.
	 * <p>
	 * Access must be synchronised.
	 */
	final static SynchronizedReference<Map<String, NonNullPair<Object, VariablesStorage>>> tempVars = new SynchronizedReference<>(new HashMap<String, NonNullPair<Object, VariablesStorage>>());
	
	private static final int MAX_CONFLICT_WARNINGS = 50;
	private static int loadConflicts = 0;
	
	/**
	 * Sets a variable and moves it to the appropriate database if the config was changed. Must only be used while variables are loaded when Skript is starting.
	 * <p>
	 * Must be called on Bukkit's main thread.
	 * <p>
	 * This method directly invokes {@link VariablesStorage#save(String, String, byte[])}, i.e. you should not be holding any database locks or such when calling this!
	 *
	 * @param name
	 * @param value
	 * @param source
	 * @return Whether the variable was stored somewhere. Not valid while storages are loading.
	 */
	static boolean variableLoaded(final String name, final @Nullable Object value, final VariablesStorage source) {
		assert Bukkit.isPrimaryThread(); // required by serialisation
		
		synchronized (tempVars) {
			final Map<String, NonNullPair<Object, VariablesStorage>> tvs = tempVars.get();
			if (tvs != null) {
				if (value == null)
					return false;
				final NonNullPair<Object, VariablesStorage> v = tvs.get(name);
				if (v != null && v.getSecond() != source) {// variable already loaded from another database
					loadConflicts++;
					if (loadConflicts <= MAX_CONFLICT_WARNINGS)
						Skript.warning("The variable {" + name + "} was loaded twice from different databases (" + v.getSecond().databaseName + " and " + source.databaseName + "), only the one from " + source.databaseName + " will be kept.");
					else if (loadConflicts == MAX_CONFLICT_WARNINGS + 1)
						Skript.warning("[!] More than " + MAX_CONFLICT_WARNINGS + " variables were loaded more than once from different databases, no more warnings will be printed.");
					v.getSecond().save(name, null, null);
				}
				tvs.put(name, new NonNullPair<>(value, source));
				return false;
			}
		}
		
		variablesLock.writeLock().lock();
		try {
			variables.setVariable(name, value);
		} finally {
			variablesLock.writeLock().unlock();
		}
		
		for (final VariablesStorage s : storages) {
			if (s.accept(name)) {
				if (s != source) {
					final Value v = serialize(value);
					s.save(name, v != null ? v.type : null, v != null ? v.data : null);
					if (value != null)
						source.save(name, null, null);
				}
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Stores loaded variables into the variables map and the appropriate databases.
	 *
	 * @return How many variables were not stored anywhere
	 */
	@SuppressWarnings("null")
	private static int onStoragesLoaded() {
		if (loadConflicts > MAX_CONFLICT_WARNINGS)
			Skript.warning("A total of " + loadConflicts + " variables were loaded more than once from different databases");
		Skript.debug("Databases loaded, setting variables...");
		
		synchronized (tempVars) {
			final Map<String, NonNullPair<Object, VariablesStorage>> tvs = tempVars.get();
			tempVars.set(null);
			assert tvs != null;
			variablesLock.writeLock().lock();
			try {
				int n = 0;
				for (final Entry<String, NonNullPair<Object, VariablesStorage>> tv : tvs.entrySet()) {
					if (!variableLoaded(tv.getKey(), tv.getValue().getFirst(), tv.getValue().getSecond()))
						n++;
				}
				
				for (final VariablesStorage s : storages)
					s.allLoaded();
				
				Skript.debug("Variables set. Queue size = " + saveQueue.size());
				
				return n;
			} finally {
				variablesLock.writeLock().unlock();
			}
		}
	}
	
	public static SerializedVariable serialize(final String name, final @Nullable Object value) {
		assert Bukkit.isPrimaryThread();
		final SerializedVariable.Value var = serialize(value);
		return new SerializedVariable(name, var);
	}
	
	public static SerializedVariable.@Nullable Value serialize(final @Nullable Object value) {
		assert Bukkit.isPrimaryThread();
		return Classes.serialize(value);
	}

	private static void saveVariableChange(final String name, final @Nullable Object value) {
		saveQueue.add(serialize(name, value));
	}
	
	final static BlockingQueue<SerializedVariable> saveQueue = new LinkedBlockingQueue<>();
	
	static volatile boolean closed = false;
	
	private final static Thread saveThread = Skript.newThread(new Runnable() {
		@Override
		public void run() {
			while (!closed) {
				try {
					// Save one variable change
					SerializedVariable v = saveQueue.take();
					for (VariablesStorage s : storages) {
						if (s.accept(v.name)) {
							s.save(v);
							break;
						}
					}
				} catch (final InterruptedException e) {}
			}
		}
	}, "Skript variable save thread");
	
	public static void close() {
		try { // Ensure that all changes are to save soon
			variablesLock.writeLock().lock();
			processChangeQueue();
		} finally {
			variablesLock.writeLock().unlock();
		}
		
		while (saveQueue.size() > 0) {
			try {
				Thread.sleep(10);
			} catch (final InterruptedException e) {}
		}
		closed = true;
		saveThread.interrupt();
	}
	
	public static int numVariables() {
		try {
			variablesLock.readLock().lock();
			return variables.hashMap.size();
		} finally {
			variablesLock.readLock().unlock();
		}
	}
	
}
