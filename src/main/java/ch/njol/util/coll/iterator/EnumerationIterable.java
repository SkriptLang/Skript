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
