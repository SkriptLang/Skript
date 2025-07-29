package ch.njol.util.coll.iterator;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.jetbrains.annotations.Nullable;

/**
 * @author Peter Güttinger
 */
public abstract class NonNullIterator<T> implements Iterator<T> {
	
	@Nullable
	private T current = null;
	
	private boolean ended = false;
	
	@Override
	public final boolean hasNext() {
		if (current != null)
			return true;
		if (ended)
			return false;
		current = getNext();
		if (current == null)
			ended = true;
		return !ended;
	}
	
	@Nullable
	protected abstract T getNext();
	
	@Override
	public final T next() {
		if (!hasNext())
			throw new NoSuchElementException();
		final T t = current;
		current = null;
		assert t != null;
		return t;
	}
	
	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
	
}
