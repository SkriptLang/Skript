package ch.njol.util.coll.iterator;

import com.google.common.collect.PeekingIterator;
import org.jetbrains.annotations.Nullable;

import java.util.NoSuchElementException;

/**
 * A simple iterator to iterate over an array.
 * 
 * @author Peter Güttinger
 */
public class ArrayIterator<T> implements PeekingIterator<T> {
	
	@Nullable
	private final T[] array;
	
	private int index = 0;
	
	public ArrayIterator(final @Nullable T[] array) {
		this.array = array;
	}
	
	public ArrayIterator(final @Nullable T[] array, final int start) {
		this.array = array;
		index = start;
	}
	
	@Override
	public boolean hasNext() {
		final T[] array = this.array;
		if (array == null)
			return false;
		return index < array.length;
	}

	@Override
	public T peek() {
		int peekIndex = index + 1;
		if (array == null || peekIndex >= array.length)
			return null;
		return array[peekIndex];
	}

	@Override
	@Nullable
	public T next() {
		final T[] array = this.array;
		if (array == null || index >= array.length)
			throw new NoSuchElementException();
		return array[index++];
	}
	
	/**
	 * not supported by arrays.
	 * 
	 * @throws UnsupportedOperationException always
	 */
	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
	
}
