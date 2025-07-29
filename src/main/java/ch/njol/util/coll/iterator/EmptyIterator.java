package ch.njol.util.coll.iterator;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.jetbrains.annotations.Nullable;

/**
 * @author Peter Güttinger
 */
public final class EmptyIterator<T> implements Iterator<T> {
	
	public final static EmptyIterator<Object> instance = new EmptyIterator<>();
	
	@SuppressWarnings("unchecked")
	public static <T> EmptyIterator<T> get() {
		return (EmptyIterator<T>) instance;
	}
	
	@Override
	public boolean hasNext() {
		return false;
	}
	
	@Override
	public T next() {
		throw new NoSuchElementException();
	}
	
	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean equals(final @Nullable Object obj) {
		return obj instanceof EmptyIterator;
	}
	
	@Override
	public int hashCode() {
		return 0;
	}
	
}
