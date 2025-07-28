package ch.njol.skript.lang;

import ch.njol.skript.sections.ExprSecBuildable;

/**
 * Object that can be builded upon via {@link ExprSecBuildable}.
 */
public interface BuildableObject<T> {

	/**
	 * @return The object to builded upon.
	 */
	T getSource();

	/**
	 * @return The {@link Class} of the object.
	 */
	Class<? extends T> getReturnType();

}
