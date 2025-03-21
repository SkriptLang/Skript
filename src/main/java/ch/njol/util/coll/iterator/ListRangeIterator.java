package ch.njol.util.coll.iterator;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import org.jetbrains.annotations.Nullable;

public class ListRangeIterator<T> implements Iterator<T> {
	
	private final ListIterator<T> iter;
	private int end;
	
	public ListRangeIterator(final List<T> list, final int start, final int end) {
		final ListIterator<T> iter = list.listIterator(start);
		if (iter == null)
			throw new IllegalArgumentException("" + list);
		this.iter = iter;
		this.end = end;
	}
	
	@Override
	public boolean hasNext() {
		return iter.nextIndex() < end;
	}
	
	@Override
	@Nullable
	public T next() {
		if (!hasNext())
			throw new NoSuchElementException();
		return iter.next();
	}
	
	@Override
	public void remove() {
		iter.remove();
		end--;
	}
	
}
