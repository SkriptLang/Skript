package org.skriptlang.skript.util;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;

/**
 * For objects that can provide an unmodifiable view of themselves.
 * An unmodifiable view means that the object may only be used in a read-only manner (its values may not be changed).
 * Since it is a view, it will reflect any changes made to the object it was created from.
 * @param <T> The type being viewed.
 */
@ApiStatus.Experimental
public interface ViewProvider<T> {

	/**
	 * Constructs an unmodifiable view of <code>this</code>.
	 * @return An unmodifiable view of <code>this</code>.
	 */
	@Contract("-> new")
	T unmodifiableView();

}
