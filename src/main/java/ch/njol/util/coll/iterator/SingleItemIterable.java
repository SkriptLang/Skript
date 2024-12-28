package ch.njol.util.coll.iterator;

import java.util.Iterator;

/**
 * @author Peter Güttinger
 */
public class SingleItemIterable<T> implements Iterable<T> {
	
	private final T item;
	
	public SingleItemIterable(final T item) {
		this.item = item;
	}
	
	@Override
	public Iterator<T> iterator() {
		return new SingleItemIterator<>(item);
	}
	
}
