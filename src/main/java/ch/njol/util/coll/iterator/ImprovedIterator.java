package ch.njol.util.coll.iterator;

import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

/**
 * @deprecated use {@link Iterator} instead.
 */
@Deprecated(since = "2.10.0", forRemoval = true)
public class ImprovedIterator<T> implements Iterator<T> {
	
	private final Iterator<T> iter;
	
	@Nullable
	private T current = null;
	
	public ImprovedIterator(final Iterator<T> iter) {
		this.iter = iter;
	}
	
	@Override
	public boolean hasNext() {
		return iter.hasNext();
	}
	
	@Override
	@Nullable
	public T next() {
		return current = iter.next();
	}
	
	@Override
	public void remove() {
		iter.remove();
	}
	
	@Nullable
	public T current() {
		return current;
	}
	
}
