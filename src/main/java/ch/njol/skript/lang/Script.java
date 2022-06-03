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
package ch.njol.skript.lang;

import ch.njol.skript.config.Config;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A Script is a container for the raw structure of a user's script along with
 *  various data such as suppressed warnings.
 */
public class Script {

	private final Config config;

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

	// Warning Suppressions

	private final Set<ScriptWarning> suppressedWarnings = new HashSet<>();

	/**
	 * @param warning The warning to check.
	 * @return Whether this script suppresses the provided warning.
	 */
	public boolean suppressesWarning(ScriptWarning warning) {
		return suppressedWarnings.contains(warning);
	}

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

	public enum ScriptWarning {
		VARIABLE_SAVE, // Variable cannot be saved (the ClassInfo is not serializable)
		MISSING_CONJUNCTION, // Missing "and" or "or"
		VARIABLE_STARTS_WITH_EXPRESSION // Variable starts with an Expression
	}

	// Script Data

	private final Map<Class<? extends ScriptData>, ScriptData> scriptData = new HashMap<>();

	@Nullable
	@SuppressWarnings("unchecked")
	public <T extends ScriptData> T getData(Class<T> dataType) {
		return (T) scriptData.get(dataType);
	}

	public void addData(ScriptData data) {
		scriptData.put(data.getClass(), data);
	}

	public void removeData(Class<? extends ScriptData> dataType) {
		scriptData.remove(dataType);
	}

	public static abstract class ScriptData { }

	// Script Events

	private final Set<ScriptEventHandler> eventHandlers = new HashSet<>();

	public Set<ScriptEventHandler> getEventHandlers() {
		return Collections.unmodifiableSet(eventHandlers);
	}

	public void addEventHandler(ScriptEventHandler eventHandler) {
		eventHandlers.add(eventHandler);
	}

	public void removeEventHandler(ScriptEventHandler eventHandler) {
		eventHandlers.remove(eventHandler);
	}

	public static abstract class ScriptEventHandler {

		/**
		 * Called when this Script is loaded.
		 * @param oldScript The Script that was just unloaded.
		 *                  Null if there wasn't a Script unloaded.
		 */
		public void onLoad(@Nullable Script oldScript) { }

		/**
		 * Called when this Script is unloaded.
		 * @param newScript The Script that will be loaded after this one is unloaded.
		 *                  Null if there won't be a Script loaded.
		 */
		public void onUnload(@Nullable Script newScript) { }

	}

}
