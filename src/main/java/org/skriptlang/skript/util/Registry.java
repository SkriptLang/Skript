package org.skriptlang.skript.util;

import org.jetbrains.annotations.ApiStatus;

import java.util.Collection;
import java.util.Iterator;

/**
 * A registry maintains a collection of elements.
 * It is up to individual implementations as to how they may be modified.
 * @param <T> The type of elements stored in a registry.
 */
@ApiStatus.Experimental
public interface Registry<T> extends Iterable<T> {

	/**
	 * @return A collection of all elements in this registry.
	 */
	Collection<T> elements();

	/**
	 * By default, this is a wrapper for <code>elements().iterator()</code>.
	 * @return An iterator over all elements in this registry.
	 * @see Collection#iterator()
	 */
	@Override
	default Iterator<T> iterator() {
		return elements().iterator();
	}

}
