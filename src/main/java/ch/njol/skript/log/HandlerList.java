/**
 * This file is part of Skript.
 * <p>
 * Skript is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Skript is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.log;

import org.eclipse.jdt.annotation.Nullable;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * @author Peter Güttinger
 */
public class HandlerList implements Iterable<LogHandler> {

	private final LinkedList<LogHandler> list = new LinkedList<>();

	public void add(LogHandler h) {
		list.addFirst(h);
	}

	@Nullable
	public LogHandler remove() {
		return list.pop();
	}

	@SuppressWarnings("null")
	@Override
	public Iterator<LogHandler> iterator() {
		return list.iterator();
	}

	public boolean contains(LogHandler h) {
		return list.contains(h);
	}

}
