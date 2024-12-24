package ch.njol.util.coll.iterator;

import java.util.Iterator;

import org.jetbrains.annotations.Nullable;

/**
 * @author Peter Güttinger
 */
public final class EmptyIterable<T> implements Iterable<T> {
	
	public final static EmptyIterable<Object> instance = new EmptyIterable<>();
	
	@SuppressWarnings("unchecked")
	public static <T> EmptyIterable<T> get() {
		return (EmptyIterable<T>) instance;
	}
	
	@Override
	public Iterator<T> iterator() {
		return EmptyIterator.get();
	}
	
	@Override
	public boolean equals(final @Nullable Object obj) {
		return obj instanceof EmptyIterable;
	}
	
	@Override
	public int hashCode() {
		return 0;
	}
	
}
