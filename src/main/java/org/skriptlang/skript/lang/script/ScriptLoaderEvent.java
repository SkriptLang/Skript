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

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.lang.parser.ParserInstance;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A ScriptLoaderEvent is used for listening to and performing actions for different Script events on a global scale.
 * Note that some of these events may be used on a Script-specific scale, if they extend {@link ScriptEvent}.
 * @see ScriptLoader#getEventRegister()
 */
public interface ScriptLoaderEvent {

	/**
	 * An event that is called when a Script is made active or inactive in a {@link ParserInstance}.
	 * Note that this event triggers <b>AFTER</b> the activity change has occurred.
	 * That is to say, the involved script will already have been made active/inactive when this event triggers.
	 * @see ParserInstance#isActive()
	 */
	@FunctionalInterface
	interface ScriptActivityChangeEvent extends ScriptLoaderEvent, ScriptEvent {

		/**
		 * Called when a Script is made active or inactive in a {@link ParserInstance}.
		 * Note that this event triggers <b>AFTER</b> the activity change has occurred.
		 * That is to say, the script will already have been made active/inactive when this event triggers.
		 *
		 * @param parser The ParserInstance where the activity change occurred.
		 * @param script The Script this event was registered for.
		 * @param active Whether <code>script</code> became active or inactive within <code>parser</code>.
		 * @param other The Script that was made active or inactive.
		 *  Whether it was made active or inactive is the negation of the <code>active</code>.
		 *  That is to say, if <code>script</code> became active, then <code>other</code> became inactive.
		 *  Null if <code>parser</code> was inactive (meaning no script became inactive)
		 *   or became inactive (meaning no script became active).
		 * @see ParserInstance#isActive()
		 */
		void onActivityChange(ParserInstance parser, Script script, boolean active, @Nullable Script other);

	}

	/**
	 * An event that is called right before a Script is unloaded in the {@link ScriptLoader}.
	 * Note that this event triggers <b>BEFORE</b> the script is actually unloaded.
	 * @see ScriptLoader#unloadScript(Script)
	 */
	@FunctionalInterface
	interface ScriptUnloadEvent extends ScriptLoaderEvent, ScriptEvent {

		/**
		 * Called when this Script is unloaded in the {@link ScriptLoader}.
		 * Note that this event triggers <b>BEFORE</b> the script is actually unloaded.
		 *
		 * @param parser The ParserInstance handling the unloading of <code>script</code>.
		 * @param script The Script being unloaded.
		 * @see ScriptLoader#unloadScript(Script)
		 */
		void onUnload(ParserInstance parser, Script script);

	}

}
