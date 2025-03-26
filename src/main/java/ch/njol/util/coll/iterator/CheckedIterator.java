package ch.njol.util.coll.iterator;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.jetbrains.annotations.Nullable;

import ch.njol.util.NullableChecker;

public class CheckedIterator<T> implements Iterator<T> {
	
	private final Iterator<T> iter;
	private final NullableChecker<T> checker;
	
	private boolean returnedNext = true;
	@Nullable
	private T next;
	
	public CheckedIterator(final Iterator<T> iter, final NullableChecker<T> checker) {
		this.iter = iter;
		this.checker = checker;
	}
	
	@Override
	public boolean hasNext() {
		if (!returnedNext)
			return true;
		if (!iter.hasNext())
			return false;
		while (iter.hasNext()) {
			next = iter.next();
			if (checker.check(next)) {
				returnedNext = false;
				return true;
			}
		}
		return false;
	}
	
	@Override
	@Nullable
	public T next() {
		if (!hasNext())
			throw new NoSuchElementException();
		returnedNext = true;
		return next;
	}
	
	@Override
	public void remove() {
		iter.remove();
	}
	
}
