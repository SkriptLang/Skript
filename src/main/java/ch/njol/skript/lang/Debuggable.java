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
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Peter Güttinger
 */
public interface Debuggable {
	
	/**
	 * @param event The event to get information to. This is always null if debug == false.
	 * @param debug If true this should print more information, if false this should print what is shown to the end user
	 * @return String representation of this object
	 */
	public String toString(@Nullable Event event, boolean debug);
	
	/**
	 * Should return <tt>{@link #toString(Event, boolean) toString}(null, false)</tt>
	 */
	@Override
	public String toString();
	
}
