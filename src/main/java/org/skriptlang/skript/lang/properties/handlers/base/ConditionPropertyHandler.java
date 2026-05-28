package org.skriptlang.skript.lang.properties.handlers.base;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript.lang.properties.PropertyBaseCondition;
import org.skriptlang.skript.lang.properties.handlers.ContainsHandler;

import java.util.function.Predicate;

/**
 * A handler for a simple property condition. This property must be an inherent condition of the thing,
 * and not require secondary inputs, like {@link ContainsHandler} does. Properties that use this interface should a
 * lso use {@link PropertyBaseCondition} for the parent condition.
 * @param <Type> The type of object this property can be applied to.
 *
 * @see PropertyBaseCondition
 */
@ApiStatus.Experimental
public interface ConditionPropertyHandler<Type> extends PropertyHandler<Type> {

	boolean check(Type propertyHolder);

	/**
	 * Creates a simple property handler from the given predicate.
	 *
	 * @param predicate The predicate to evaluate the condition with.
	 * @param <Type> The type of object this property can be applied to.
	 * @return A new property handler that uses the given predicate.
	 */
	@Contract(value = "_ -> new", pure = true)
	static <Type> @NotNull ConditionPropertyHandler<Type> of(
		Predicate<Type> predicate
	) {
		return predicate::test;
	}

}
