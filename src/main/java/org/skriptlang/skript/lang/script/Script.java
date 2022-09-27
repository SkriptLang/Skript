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
package org.skriptlang.skript.lang.script;

import ch.njol.skript.config.Config;
import org.eclipse.jdt.annotation.Nullable;
import org.skriptlang.skript.lang.structure.Structure;

import java.util.ArrayList;
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

	private final List<Structure> structures = new ArrayList<>();

	/**
	 * Creates a new Script to be used across the API.
	 * Only one script should be created per Config. A loaded script may be obtained through {@link ch.njol.skript.ScriptLoader}.
	 * @param config The Config containing the contents of this script.
	 */
	public Script(Config config) {
		this.config = config;
	}

	/**
	 * @return The Config representing the structure of this script.
	 */
	public Config getConfig() {
		return config;
	}

	/**
	 * @return A list of all Structures within this Script.
	 */
	public List<Structure> getStructures() {
		return structures;
	}

	// Warning Suppressions

	private final Set<ScriptWarning> suppressedWarnings = new HashSet<>(ScriptWarning.values().length);

	/**
	 * @param warning Suppresses the provided warning for this script.
	 */
	public void suppressWarning(ScriptWarning warning) {
		suppressedWarnings.add(warning);
	}

	/**
	 * @param warning Allows the provided warning for this script.
	 */
	public void allowWarning(ScriptWarning warning) {
		suppressedWarnings.remove(warning);
	}

	/**
	 * @param warning The warning to check.
	 * @return Whether this script suppresses the provided warning.
	 */
	public boolean suppressesWarning(ScriptWarning warning) {
		return suppressedWarnings.contains(warning);
	}

	// Script Data

	private final Map<Class<? extends ScriptData>, ScriptData> scriptData = new ConcurrentHashMap<>(5);

	/**
	 * Adds new ScriptData to this Script's data map.
	 * @param data The data to add.
	 */
	public void addData(ScriptData data) {
		scriptData.put(data.getClass(), data);
	}

	/**
	 * Removes the ScriptData matching the specified data type.
	 * @param dataType The type of the data to remove.
	 */
	public void removeData(Class<? extends ScriptData> dataType) {
		scriptData.remove(dataType);
	}

	/**
	 * A method to obtain ScriptData matching the specified data type.
	 * @param dataType The class representing the ScriptData to obtain.
	 * @return ScriptData found matching the provided class, or null if no data is present.
	 */
	@Nullable
	@SuppressWarnings("unchecked")
	public <Type extends ScriptData> Type getData(Class<Type> dataType) {
		return (Type) scriptData.get(dataType);
	}

	/**
	 * A method that always obtains ScriptData matching the specified data type.
	 * By using the mapping supplier, it will also add ScriptData of the provided type if it is not already present.
	 * @param dataType The class representing the ScriptData to obtain.
	 * @param mapper A supplier to create ScriptData of the provided type if such ScriptData is not already present.
	 * @return Existing ScriptData found matching the provided class, or new data provided by the mapping function.
	 */
	@SuppressWarnings("unchecked")
	public <Value extends ScriptData> Value getData(Class<? extends Value> dataType, Supplier<Value> mapper) {
		return (Value) scriptData.computeIfAbsent(dataType, clazz -> mapper.get());
	}

	// Script Events

	private final Set<ScriptEventHandler> eventHandlers = new HashSet<>(5);

	/**
	 * Adds the provided event handler to this Script.
	 * @param eventHandler The event handler to add.
	 */
	public void addEventHandler(ScriptEventHandler eventHandler) {
		eventHandlers.add(eventHandler);
	}

	/**
	 * Removes the provided event handler from this Script.
	 * @param eventHandler The event handler to remove.
	 */
	public void removeEventHandler(ScriptEventHandler eventHandler) {
		eventHandlers.remove(eventHandler);
	}

	/**
	 * @return An unmodifiable set of all event handlers.
	 */
	public Set<ScriptEventHandler> getEventHandlers() {
		return Collections.unmodifiableSet(eventHandlers);
	}

}
