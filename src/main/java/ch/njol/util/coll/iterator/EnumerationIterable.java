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

import java.util.Enumeration;
import java.util.Iterator;

import org.jetbrains.annotations.Nullable;

/**
 * TODO this should actually only be an Iterator
 * 
 * @author Peter Güttinger
 */
public class EnumerationIterable<T> implements Iterable<T> {
	
	@Nullable
	final Enumeration<? extends T> e;
	
	public EnumerationIterable(final @Nullable Enumeration<? extends T> e) {
		this.e = e;
	}
	
	@Override
	public Iterator<T> iterator() {
		final Enumeration<? extends T> e = this.e;
		if (e == null)
			return EmptyIterator.get();
		return new Iterator<T>() {
			@Override
			public boolean hasNext() {
				return e.hasMoreElements();
			}
			
			@Override
			@Nullable
			public T next() {
				return e.nextElement();
			}
			
			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
	
}
