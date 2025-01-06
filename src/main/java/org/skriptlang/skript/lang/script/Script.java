package org.skriptlang.skript.lang.script;

import ch.njol.skript.config.Config;
import ch.njol.skript.lang.util.common.AnyNamed;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.UnknownNullability;
import org.jetbrains.annotations.Unmodifiable;
import org.skriptlang.skript.util.event.EventRegistry;
import org.skriptlang.skript.lang.structure.Structure;
import org.skriptlang.skript.util.Validated;

import java.io.File;
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
public final class Script implements Validated, AnyNamed {

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

	@Override
	public String name() {
		return config.name();
	}

	/**
	 * This is added to support the legacy script name syntax.
	 * Script names used to be printed including their directory but excluding their file extension.
	 *
	 * @return The script's name, including its path from the script directory, e.g. `games/murder mystery`
	 */
	public String nameAndPath() {
		String name = config.getFileName();
		if (name == null)
			return null;
		if (name.contains("."))
			return name.substring(0, name.lastIndexOf('.'));
		return name;
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

	/**
	 * Marks this script reference as invalid.
	 * Typically invoked during unloading (when its data is discarded).
	 */
	@Override
	public void invalidate() {
		this.config.invalidate();
	}

	/**
	 * This is a reference to a script (having been loaded); if the script is reloaded,
	 * disabled, moved or changed in some way then this object will no longer be a valid
	 * reference to it.
	 * <br/>
	 * If a script reference is not valid, it is not safe to assume that the data in
	 * this object is an accurate reflection of the program (e.g. the data could have cleared
	 * during unloading, the user might have edited the file and reloaded it, etc.) and
	 * it is recommended to obtain a new reference to the script from {@link ch.njol.skript.ScriptLoader}.
	 *
	 * @return Whether this script object is a valid reflection of a script
	 */
	@Override
	public boolean valid() {
		if (config.valid()) {
			@Nullable File file = config.getFile();
			return file == null || file.exists();
			// If this is file-linked and that file was moved/deleted (e.g. this was disabled)
			// then we should not assume this is a safe reference to use, unless it was
			// immediately obtained.
		}
		return false;
	}

}
