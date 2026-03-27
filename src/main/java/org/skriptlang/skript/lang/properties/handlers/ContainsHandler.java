package org.skriptlang.skript.lang.properties.handlers;


import org.jetbrains.annotations.ApiStatus;
import org.skriptlang.skript.common.properties.conditions.PropCondContains;
import org.skriptlang.skript.lang.properties.handlers.base.PropertyHandler;

/**
 * A handler that can check if a container contains a specific element.
 *
 * @param <Container> The type of object that can contain elements.
 * @param <Element> The type of object that can be contained.
 *
 * @see PropCondContains
 */
@ApiStatus.Experimental
public interface ContainsHandler<Container, Element> extends PropertyHandler<Container> {

	/**
	 * Checks if the given container contains the given element.
	 *
	 * @param container The container to check.
	 * @param element The element to check for.
	 * @return {@code true} if the container contains the element, {@code false} otherwise.
	 */
	boolean contains(Container container, Element element);

	/**
	 * The types of elements that can be contained. This is used for type checking and auto-completion.
	 * Implementations that override {@link #canContain(Class)} may not return accurate results for this method.
	 * Callers should prefer {@link #canContain(Class)} when possible.
	 *
	 * @return The types of elements that can be contained.
	 */
	Class<? extends Element>[] elementTypes();

	/**
	 * Checks if this handler can contain the given type of element.
	 * The default implementation checks if the given type is assignable to any of the types returned by
	 * {@link #elementTypes()}.
	 *
	 * @param type The type to check.
	 * @return {@code true} if this handler can contain the given type, {@code false} otherwise.
	 */
	default boolean canContain(Class<?> type) {
		for (Class<? extends Element> elementType : elementTypes()) {
			if (elementType.isAssignableFrom(type)) {
				return true;
			}
		}
		return false;
	}

}
