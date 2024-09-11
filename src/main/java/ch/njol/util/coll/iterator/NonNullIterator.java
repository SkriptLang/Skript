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
package ch.njol.util.coll.iterator;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.jetbrains.annotations.Nullable;

/**
 * @author Peter Güttinger
 */
public abstract class NonNullIterator<T> implements Iterator<T> {
	
	@Nullable
	private T current = null;
	
	private boolean ended = false;
	
	@Override
	public final boolean hasNext() {
		if (current != null)
			return true;
		if (ended)
			return false;
		current = getNext();
		if (current == null)
			ended = true;
		return !ended;
	}
	
	@Nullable
	protected abstract T getNext();
	
	@Override
	public final T next() {
		if (!hasNext())
			throw new NoSuchElementException();
		final T t = current;
		current = null;
		assert t != null;
		return t;
	}
	
	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
	
}
