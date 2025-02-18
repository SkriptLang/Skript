package ch.njol.util.coll.iterator;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.jetbrains.annotations.Nullable;

/**
 * An iterator that iterates over all elements of several iterables.
 * <p>
 * Elements are removable from this iterator if the source iterables support element removal, unless removal is blocked on creation.
 * 
 * @deprecated use {@link com.google.common.collect.Iterators#concat(Iterator[])}
 */
@Deprecated
public class CombinedIterator<T> implements Iterator<T> {
	
	private final Iterator<? extends Iterable<T>> iterators;
	private boolean removable;
	
	public CombinedIterator(final Iterator<? extends Iterable<T>> iterators) {
		this(iterators, true);
	}
	
	public CombinedIterator(final Iterator<? extends Iterable<T>> iterators, final boolean removable) {
		this.iterators = iterators;
		this.removable = removable;
	}
	
	@Nullable
	private Iterator<T> current = null;
	
	@SuppressWarnings("null")
	@Override
	public boolean hasNext() {
		while ((current == null || !current.hasNext()) && iterators.hasNext()) {
			current = iterators.next().iterator();
		}
		return current != null && current.hasNext();
	}
	
	/**
	 * The iterator that returned the last element (stored for possible removal of said element)
	 */
	@Nullable
	private Iterator<T> last = null;
	
	@Nullable
	@Override
	public T next() {
		if (!hasNext())
			throw new NoSuchElementException();
		final Iterator<T> current = this.current;
		assert current != null;
		last = current;
		return current.next();
	}
	
	@Override
	public void remove() {
		if (!removable)
			throw new UnsupportedOperationException();
		if (last != null)
			last.remove();
		else
			throw new IllegalStateException();
	}
	
}
