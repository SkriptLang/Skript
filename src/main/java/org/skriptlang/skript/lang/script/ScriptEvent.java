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

import ch.njol.skript.lang.parser.ParserInstance;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A ScriptEvent is used for listening to and performing actions for different Script events.
 * @see Script#registerEvent(ScriptEvent)
 */
public interface ScriptEvent {

	/**
	 * A ScriptEvent that is called when a Script is made active in a {@link ParserInstance}.
	 */
	@FunctionalInterface
	interface ScriptActiveEvent extends ScriptEvent {

		/**
		 * Called when this Script is made active in a {@link ParserInstance}.
		 *
		 * @param oldScript The Script that was just made inactive.
		 * Null if the {@link ParserInstance} handling this Script
		 *  was not {@link ParserInstance#isActive()} (it is becoming active).
		 */
		void onActive(@Nullable Script oldScript);

	}

	/**
	 * A ScriptEvent that is called when a Script is made inactive in a {@link ParserInstance}.
	 */
	@FunctionalInterface
	interface ScriptInactiveEvent extends ScriptEvent {

		/**
		 * Called when this Script is made inactive in a {@link ParserInstance}.
		 *
		 * @param newScript The Script that will be made active after this one is completely inactive.
		 * Null if the {@link ParserInstance} handling this Script
		 *  will not be {@link ParserInstance#isActive()} (will become inactive).
		 */
		void onInactive(@Nullable Script newScript);

	}

}
