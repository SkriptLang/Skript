package ch.njol.util.coll.iterator;

import java.util.Iterator;

/**
 * @author Peter Güttinger
 */
public class IteratorIterable<T> implements Iterable<T> {
	
	private final Iterator<T> iter;
	
	public IteratorIterable(final Iterator<T> iter) {
		this.iter = iter;
	}
	
	@Override
	public Iterator<T> iterator() {
		return iter;
	}
	
}
