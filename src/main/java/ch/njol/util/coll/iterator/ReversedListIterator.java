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

import java.util.List;
import java.util.ListIterator;

import org.jetbrains.annotations.Nullable;

/**
 * @author Peter Güttinger
 */
public class ReversedListIterator<T> implements ListIterator<T> {
	
	private final ListIterator<T> iter;
	
	public ReversedListIterator(final List<T> list) {
		final ListIterator<T> iter = list.listIterator(list.size());
		if (iter == null)
			throw new IllegalArgumentException("" + list);
		this.iter = iter;
	}
	
	public ReversedListIterator(final List<T> list, final int index) {
		final ListIterator<T> iter = list.listIterator(list.size() - index);
		if (iter == null)
			throw new IllegalArgumentException("" + list);
		this.iter = iter;
	}
	
	public ReversedListIterator(final ListIterator<T> iter) {
		this.iter = iter;
	}
	
	@Override
	public boolean hasNext() {
		return iter.hasPrevious();
	}
	
	@Override
	@Nullable
	public T next() {
		return iter.previous();
	}
	
	@Override
	public boolean hasPrevious() {
		return iter.hasNext();
	}
	
	@Override
	@Nullable
	public T previous() {
		return iter.next();
	}
	
	@Override
	public int nextIndex() {
		return iter.previousIndex();
	}
	
	@Override
	public int previousIndex() {
		return iter.nextIndex();
	}
	
	@Override
	public void remove() {
		iter.remove();
	}
	
	@Override
	public void set(final @Nullable T e) {
		iter.set(e);
	}
	
	@Override
	public void add(final @Nullable T e) {
		throw new UnsupportedOperationException();
	}
	
}
