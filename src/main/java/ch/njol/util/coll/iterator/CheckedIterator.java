package ch.njol.util.coll.iterator;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

public class CheckedIterator<T> implements Iterator<T> {
	
	private final Iterator<T> iter;
	private final Predicate<T> checker;
	
	private boolean returnedNext = true;
	private T next;
	
	public CheckedIterator(Iterator<T> iter, Predicate<T> checker) {
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
			if (checker.test(next)) {
				returnedNext = false;
				return true;
			}
		}
		return false;
	}
	
	@Override
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
