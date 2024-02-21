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
package org.skriptlang.skript.lang.debug;

import ch.njol.skript.lang.SyntaxElement;
import ch.njol.skript.lang.TriggerItem;
import org.bukkit.event.Event;

/**
 * An interface to debug or profile script execution.
 */
public interface Debugger {

	/**
	 * Called after a {@link SyntaxElement} is parsed.
	 * @param element The SyntaxElement parsed
	 */
	void onParse(SyntaxElement element);

	/**
	 * Called before a {@link TriggerItem} is called.
	 * @param triggerItem The TriggerItem going to be executed
	 * @param event The event
	 */
	void onWalk(TriggerItem triggerItem, Event event);

}
