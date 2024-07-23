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
package ch.njol.skript.lang.util;

import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;

/**
 * A very basic SkriptEvent which returns true for all events (i.e. all registered events).
 * 
 * @author Peter Güttinger
 */
public class SimpleEvent extends SkriptEvent {

	public SimpleEvent() {}

	@Override
	public boolean check(Event event) {
		return true;
	}

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parser) {
		if (args.length != 0)
			throw new SkriptAPIException("Invalid use of SimpleEvent");
		return true;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "simple event";
	}

}
