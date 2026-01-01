package org.skriptlang.skript.lang.properties.handlers.base;

import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.NotNull;

/**
 * A handler to specify that it can return something and what can be returned.
 *
 * @param <Type> The type of object this property can be applied to.
 * @param <ReturnType> The type of object that is returned by this property.
 */
@Experimental
public interface ReturnablePropertyHandler<Type, ReturnType> extends PropertyHandler<Type> {

	/**
	 * The return type of this property. This is used for type checking and auto-completion.
	 * If the property can return multiple types, it should return the most general type that encompasses all
	 * possible return types.
	 *
	 * @return The return type of this property.
	 */
	@NotNull Class<ReturnType> returnType();

	/**
	 * The possible return types of this property. This is used for type checking and auto-completion.
	 * The default implementation returns an array containing the type returned by {@link #returnType()}.
	 * If the property can return multiple types, it should return all possible return types.
	 *
	 * @return The possible return types of this property.
	 */
	default Class<?> @NotNull [] possibleReturnTypes() {
		return new Class[]{ returnType() };
	}

}
