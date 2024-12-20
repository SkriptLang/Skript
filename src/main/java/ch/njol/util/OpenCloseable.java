package ch.njol.util;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.Iterator;

/**
 * A object that can both be opened and closed.
 *
 * @see ch.njol.skript.log.LogHandler
 */
public interface OpenCloseable extends AutoCloseable {
	
	/**
	 * An {@link OpenCloseable} without effect.
	 */
	OpenCloseable EMPTY = new OpenCloseable() {
		@Override
		public void open() { }
		
		@Override
		public void close() { }
	};
	
	/**
	 * @return a {@link OpenCloseable} that, when opened, calls {@link OpenCloseable#open()}
	 * on each given {@link OpenCloseable}, in the given order.
	 * When closed, calls {@link OpenCloseable#close()} in each given {@link OpenCloseable}
	 * in reverse order.
	 */
	static OpenCloseable combine(OpenCloseable... openCloseableArray) {
		Deque<OpenCloseable> openCloseables = new ArrayDeque<>(Arrays.asList(openCloseableArray));
		
		return new OpenCloseable() {
			@Override
			public void open() {
				// Ascending
				for (OpenCloseable openCloseable : openCloseables)
					openCloseable.open();
			}
			
			@Override
			public void close() {
				// Descending
				Iterator<OpenCloseable> openCloseableIterator = openCloseables.descendingIterator();
				while (openCloseableIterator.hasNext())
					openCloseableIterator.next().close();
			}
		};
	}
	
	void open();
	
	void close();
	
}
