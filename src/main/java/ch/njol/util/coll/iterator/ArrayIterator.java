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

import com.google.common.collect.PeekingIterator;
import org.jetbrains.annotations.Nullable;

import java.util.NoSuchElementException;

/**
 * A simple iterator to iterate over an array.
 * 
 * @author Peter Güttinger
 */
public class ArrayIterator<T> implements PeekingIterator<T> {
	
	@Nullable
	private final T[] array;
	
	private int index = 0;
	
	public ArrayIterator(final @Nullable T[] array) {
		this.array = array;
	}
	
	public ArrayIterator(final @Nullable T[] array, final int start) {
		this.array = array;
		index = start;
	}
	
	@Override
	public boolean hasNext() {
		final T[] array = this.array;
		if (array == null)
			return false;
		return index < array.length;
	}

	@Override
	public T peek() {
		int peekIndex = index + 1;
		if (array == null || peekIndex >= array.length)
			return null;
		return array[peekIndex];
	}

	@Override
	@Nullable
	public T next() {
		final T[] array = this.array;
		if (array == null || index >= array.length)
			throw new NoSuchElementException();
		return array[index++];
	}
	
	/**
	 * not supported by arrays.
	 * 
	 * @throws UnsupportedOperationException always
	 */
	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
	
}
