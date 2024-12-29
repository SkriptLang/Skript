package ch.njol.util.coll.iterator;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author Peter Güttinger
 */
public class SingleItemIterator<T> implements Iterator<T> {
	
	private final T item;
	private boolean returned = false;
	
	public SingleItemIterator(final T item) {
		this.item = item;
	}
	
	@Override
	public boolean hasNext() {
		return !returned;
	}
	
	@Override
	public T next() {
		if (returned)
			throw new NoSuchElementException();
		returned = true;
		return item;
	}
	
	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
	
}
