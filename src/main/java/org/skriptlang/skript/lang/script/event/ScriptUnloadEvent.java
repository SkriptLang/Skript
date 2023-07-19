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
import ch.njol.skript.lang.parser.ParserInstance;
import org.skriptlang.skript.lang.script.Script;

/**
 * Called when a {@link Script} is unloaded in the {@link ScriptLoader}.
 * Note that this event triggers <b>before</b> the script is unloaded.
 *
 * @see ScriptLoader#unloadScript(Script)
 */
@FunctionalInterface
public interface ScriptUnloadEvent extends ScriptLoaderEvent, ScriptEvent {

	/**
	 * The method that is called when this event triggers.
	 *
	 * @param parser The ParserInstance handling the unloading of <code>script</code>.
	 * @param script The Script being unloaded.
	 */
	void onUnload(ParserInstance parser, Script script);

}
