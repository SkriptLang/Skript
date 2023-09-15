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
package ch.njol.util.coll.iterator;

import org.eclipse.jdt.annotation.Nullable;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

public class ListRangeIterator<T> implements Iterator<T> {

	private final ListIterator<T> iter;
	private int end;

	public ListRangeIterator(final List<T> list, final int start, final int end) {
		final ListIterator<T> iter = list.listIterator(start);
		if (iter == null)
			throw new IllegalArgumentException("" + list);
		this.iter = iter;
		this.end = end;
	}

	@Override
	public boolean hasNext() {
		return iter.nextIndex() < end;
	}

	@Override
	@Nullable
	public T next() {
		if (!hasNext())
			throw new NoSuchElementException();
		return iter.next();
	}

	@Override
	public void remove() {
		iter.remove();
		end--;
	}

}
