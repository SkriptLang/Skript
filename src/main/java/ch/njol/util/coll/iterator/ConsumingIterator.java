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
import java.util.function.Consumer;

/**
 * An {@link Iterator} that also calls {@link Consumer#accept(Object)} on each object provided by the given {@link Iterator}.
 */
public class ConsumingIterator<E> implements Iterator<E> {

	private final Iterator<E> iterator;
	private final Consumer<E> consumer;

	public ConsumingIterator(Iterator<E> iterator, Consumer<E> consumer) {
		this.iterator = iterator;
		this.consumer = consumer;
	}

	@Override
	public boolean hasNext() {
		return iterator.hasNext();
	}

	@Override
	public E next() {
		E value = iterator.next();
		consumer.accept(value);
		return value;
	}

	@Override
	public void remove() {
		iterator.remove();
	}

}
