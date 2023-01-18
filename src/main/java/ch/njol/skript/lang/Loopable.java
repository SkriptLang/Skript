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

import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A simple interface to state a syntax element loopable, used by ExprLoopValue.
 * Please note a while loop is not "loopable" since doesn't conform to this interface
 * because it doesn't supply a looped expression nor currently iterated object.
 */
public interface Loopable {
	/**
	 * @param event The current event
	 * @return The currently being looped object
	 */
	@Nullable Object getCurrent(@NotNull Event event);
	
	/**
	 * @return The expression being looped
	 */
	@NotNull Expression<?> getLoopedExpression();
}
