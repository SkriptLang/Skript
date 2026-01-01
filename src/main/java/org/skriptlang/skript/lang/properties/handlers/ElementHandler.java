package org.skriptlang.skript.lang.properties.handlers;

import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.properties.handlers.base.ReturnablePropertyHandler;

/**
 * A handler for getting elements/objects contains within another object.
 *
 * @param <Type> The type of object this property can be applied to.
 * @param <ReturnType> The type of object that is returned by this property.
 */
@Experimental
public interface ElementHandler<Type, ReturnType> extends ReturnablePropertyHandler<Type, ReturnType> {

	/**
	 * Retrieve an element at the specified {@code index}.
	 *
	 * @param type The object that contains elements.
	 * @param index The index to retrieve an element from.
	 * @return The retrieved element.
	 */
	@Nullable ReturnType get(Type type, Integer index);

	/**
	 * Retrieve elements from {@code start} to {@code end}.
	 *
	 * @param type The object that contains elements.
	 * @param start The starting index.
	 * @param end The end index.
	 * @return The retrieved elements.
	 */
	ReturnType @Nullable [] get(Type type, Integer start, Integer end);

	/**
	 * Retrieve the number of elements.
	 *
	 * @param type The object that contains elements.
	 * @return The number of elements contained.
	 */
	int size(Type type);

}
