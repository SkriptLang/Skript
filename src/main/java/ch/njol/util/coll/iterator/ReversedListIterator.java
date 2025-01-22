package ch.njol.util.coll.iterator;

import java.util.List;
import java.util.ListIterator;

import org.jetbrains.annotations.Nullable;

/**
 * @deprecated unused
 */
@Deprecated
public class ReversedListIterator<T> implements ListIterator<T> {
	
	private final ListIterator<T> iter;
	
	public ReversedListIterator(final List<T> list) {
		final ListIterator<T> iter = list.listIterator(list.size());
		if (iter == null)
			throw new IllegalArgumentException("" + list);
		this.iter = iter;
	}
	
	public ReversedListIterator(final List<T> list, final int index) {
		final ListIterator<T> iter = list.listIterator(list.size() - index);
		if (iter == null)
			throw new IllegalArgumentException("" + list);
		this.iter = iter;
	}
	
	public ReversedListIterator(final ListIterator<T> iter) {
		this.iter = iter;
	}
	
	@Override
	public boolean hasNext() {
		return iter.hasPrevious();
	}
	
	@Override
	@Nullable
	public T next() {
		return iter.previous();
	}
	
	@Override
	public boolean hasPrevious() {
		return iter.hasNext();
	}
	
	@Override
	@Nullable
	public T previous() {
		return iter.next();
	}
	
	@Override
	public int nextIndex() {
		return iter.previousIndex();
	}
	
	@Override
	public int previousIndex() {
		return iter.nextIndex();
	}
	
	@Override
	public void remove() {
		iter.remove();
	}
	
	@Override
	public void set(final @Nullable T e) {
		iter.set(e);
	}
	
	@Override
	public void add(final @Nullable T e) {
		throw new UnsupportedOperationException();
	}
	
}
