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
package org.skriptlang.skript.lang.script.event;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.config.Config;
import ch.njol.util.OpenCloseable;
import org.skriptlang.skript.lang.script.Script;

import java.io.File;
import java.util.Collection;
import java.util.Set;

/**
 * Called when {@link ScriptLoader} is preparing to load {@link Config}s into {@link Script}s.
 *
 * @see ScriptLoader#loadScripts(File, OpenCloseable)
 * @see ScriptLoader#loadScripts(Set, OpenCloseable)
 */
@FunctionalInterface
public interface PreScriptInitEvent extends ScriptLoaderEvent {

	/**
	 * The method that is called when this event triggers.
	 * Modifications to the given collection will affect what is loaded.
	 *
	 * @param configs The Configs to be loaded.
	 */
	void onPreInit(Collection<Config> configs);

}
