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

import ch.njol.skript.lang.parser.ParserInstance;
import org.eclipse.jdt.annotation.Nullable;
import org.skriptlang.skript.lang.script.Script;

/**
 * Called when a {@link Script} is made active or inactive in a {@link ParserInstance}.
 * Note that this event triggers <b>after</b> the change in activity has occurred.
 *
 * @see ParserInstance#isActive()
 */
@FunctionalInterface
public interface ScriptActivityChangeEvent extends ScriptLoaderEvent, ScriptEvent {

	/**
	 * The method that is called when this event triggers.
	 *
	 * @param parser The ParserInstance where the activity change occurred.
	 * @param script The Script this event was registered for.
	 * @param active Whether <code>script</code> became active or inactive within <code>parser</code>.
	 * @param other  The Script that was made active or inactive.
	 *               Whether it was made active or inactive is the negation of the <code>active</code>.
	 *               That is to say, if <code>script</code> became active, then <code>other</code> became inactive.
	 *               Null if <code>parser</code> was inactive (meaning no script became inactive)
	 *               or became inactive (meaning no script became active).
	 */
	void onActivityChange(ParserInstance parser, Script script, boolean active, @Nullable Script other);

}
