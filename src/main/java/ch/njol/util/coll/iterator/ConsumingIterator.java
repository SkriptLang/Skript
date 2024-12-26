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
