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

import ch.njol.util.NullableChecker;

public class CheckedIterator<T> implements Iterator<T> {
	
	private final Iterator<T> iter;
	private final NullableChecker<T> checker;
	
	private boolean returnedNext = true;
	@Nullable
	private T next;
	
	public CheckedIterator(final Iterator<T> iter, final NullableChecker<T> checker) {
		this.iter = iter;
		this.checker = checker;
	}
	
	@Override
	public boolean hasNext() {
		if (!returnedNext)
			return true;
		if (!iter.hasNext())
			return false;
		while (iter.hasNext()) {
			next = iter.next();
			if (checker.check(next)) {
				returnedNext = false;
				return true;
			}
		}
		return false;
	}
	
	@Override
	@Nullable
	public T next() {
		if (!hasNext())
			throw new NoSuchElementException();
		returnedNext = true;
		return next;
	}
	
	@Override
	public void remove() {
		iter.remove();
	}
	
}
