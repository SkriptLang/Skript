package org.skriptlang.skript.lang.script;

import ch.njol.skript.config.Config;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Unmodifiable;
import org.skriptlang.skript.util.event.EventRegistry;
import org.skriptlang.skript.lang.structure.Structure;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Scripts are the primary container of all code.
 * Every script is made up of one or more {@link Structure}s, which contain user-defined instructions and information.
 * Every script also has its own internal information, such as
 *  custom data, suppressed warnings, and associated event handlers.
 */
public final class Script {

	private final Config config;

	private final List<Structure> structures;

	/**
	 * Creates a new Script to be used across the API.
	 * Only one Script should be created per Config. A loaded Script may be obtained through {@link ch.njol.skript.ScriptLoader}.
	 * @param config The Config containing the contents of this Script.
	 * @param structures The list of Structures contained in this Script.
	 */
	@ApiStatus.Internal
	public Script(Config config, List<Structure> structures) {
		this.config = config;
		this.structures = structures;
	}

	/**
	 * @return The Config representing the structure of this Script.
	 */
	public Config getConfig() {
		return config;
	}

	/**
	 * @return An unmodifiable list of all Structures within this Script.
	 */
	@Unmodifiable
	public List<Structure> getStructures() {
		return Collections.unmodifiableList(structures);
	}

	// Warning Suppressions

	private final Set<ScriptWarning> suppressedWarnings = new HashSet<>(ScriptWarning.values().length);

	/**
	 * @param warning Suppresses the provided warning for this Script.
	 */
	public void suppressWarning(ScriptWarning warning) {
		suppressedWarnings.add(warning);
	}

	/**
	 * @param warning Allows the provided warning for this Script.
	 */
	public void allowWarning(ScriptWarning warning) {
		suppressedWarnings.remove(warning);
	}

	/**
	 * @param warning The warning to check.
	 * @return Whether this Script suppresses the provided warning.
	 */
	public boolean suppressesWarning(ScriptWarning warning) {
		return suppressedWarnings.contains(warning);
	}

	// Script Data

	private final Map<Class<? extends ScriptData>, ScriptData> scriptData = new ConcurrentHashMap<>(5);

	/**
	 * <b>This API is experimental and subject to change.</b>
	 * Adds new ScriptData to this Script's data map.
	 * @param data The data to add.
	 */
	@ApiStatus.Experimental
	public void addData(ScriptData data) {
		scriptData.put(data.getClass(), data);
	}

	/**
	 * <b>This API is experimental and subject to change.</b>
	 * Removes the ScriptData matching the specified data type.
	 * @param dataType The type of the data to remove.
	 */
	@ApiStatus.Experimental
	public void removeData(Class<? extends ScriptData> dataType) {
		scriptData.remove(dataType);
	}

	/**
	 * <b>This API is experimental and subject to change.</b>
	 * Clears the data stored for this script.
	 */
	@ApiStatus.Experimental
	public void clearData() {
		scriptData.clear();
	}

	/**
	 * <b>This API is experimental and subject to change.</b>
	 * A method to obtain ScriptData matching the specified data type.
	 * @param dataType The class representing the ScriptData to obtain.
	 * @return ScriptData found matching the provided class, or null if no data is present.
	 */
	@ApiStatus.Experimental
	@Nullable
	@SuppressWarnings("unchecked")
	public <Type extends ScriptData> Type getData(Class<Type> dataType) {
		return (Type) scriptData.get(dataType);
	}

	/**
	 * <b>This API is experimental and subject to change.</b>
	 * A method that always obtains ScriptData matching the specified data type.
	 * By using the mapping supplier, it will also add ScriptData of the provided type if it is not already present.
	 * @param dataType The class representing the ScriptData to obtain.
	 * @param mapper A supplier to create ScriptData of the provided type if such ScriptData is not already present.
	 * @return Existing ScriptData found matching the provided class, or new data provided by the mapping function.
	 */
	@ApiStatus.Experimental
	@SuppressWarnings("unchecked")
	public <Value extends ScriptData> Value getData(Class<? extends Value> dataType, Supplier<Value> mapper) {
		return (Value) scriptData.computeIfAbsent(dataType, clazz -> mapper.get());
	}

	// Script Events

	/**
	 * Used for listening to events involving a Script.
	 * @see #eventRegistry()
	 */
	public interface Event extends org.skriptlang.skript.util.event.Event { }

	private final EventRegistry<Event> eventRegistry = new EventRegistry<>();

	/**
	 * @return An EventRegistry for this Script's events.
	 */
	public EventRegistry<Event> eventRegistry() {
		return eventRegistry;
	}

}
