package org.skriptlang.skript.lang.properties.handlers.base;


import ch.njol.skript.classes.Changer;
import ch.njol.skript.lang.Expression;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.properties.PropertyBaseExpression;

import java.util.function.Function;

/**
 * A handler that can get and optionally change a property value. This interface is suitable for properties that act
 * like expressions, such as "name", "display name", etc. Properties that use this interface should also use
 * {@link PropertyBaseExpression} for the parent expression.
 * @param <Type> The type of object this property can be applied to.
 * @param <ReturnType> The type of object that is returned by this property.
 *
 * @see PropertyBaseExpression
 */
@ApiStatus.Experimental
public interface ExpressionPropertyHandler<Type, ReturnType> extends PropertyHandler<Type> {

	/**
	 * Converts the given object to the property value. This method may return arrays if the property is multi-valued.
	 *
	 * @param propertyHolder The object to convert.
	 * @return The property value.
	 */
	@Nullable ReturnType convert(Type propertyHolder);

	/**
	 * Returns the types of changes that this property supports. If the property does not support any changes,
	 * this method should return {@code null}. If the property supports changes, it should return the classes
	 * that are accepted for each change mode. {@link Changer.ChangeMode#RESET} and {@link Changer.ChangeMode#DELETE} do not require
	 * any specific types, so they can return an empty or non-empty array.
	 * <br>
	 * The default implementation returns {@code null}, indicating that the property is read-only.
	 *
	 * @param mode The change mode to check.
	 * @return The types supported by this property for the given change mode, or {@code null} if the property is read-only.
	 * @see Expression#acceptChange(Changer.ChangeMode)
	 */
	default Class<?> @Nullable [] acceptChange(Changer.ChangeMode mode) {
		return null;
	}

	/**
	 * Changes the property value of the given object. This method is only called if {@link #acceptChange(Changer.ChangeMode)}
	 * returns a non-null value for the given change mode.
	 *
	 * @param propertyHolder The object to change.
	 * @param delta The new value(s) to set. This is {@code null} for {@link Changer.ChangeMode#RESET} and {@link Changer.ChangeMode#DELETE}.
	 * @param mode The change mode to apply.
	 * @throws UnsupportedOperationException If the property is read-only and does not support changes.
	 */
	default void change(Type propertyHolder, Object @Nullable [] delta, Changer.ChangeMode mode) {
		throw new UnsupportedOperationException("Changing is not supported for this property.");
	}

	/**
	 * Whether changing this property requires the source expression to be re-set.
	 * For example, `set x of (velocity of player) to 1` requires the velocity to be re-set.
	 * `set name of tool of player` does not, since the slot property updates the item.
	 * @return Whether the source expression for this property needs to be changed.
	 */
	default boolean requiresSourceExprChange() {
		return false;
	}

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

	/**
	 * Creates a simple property handler from the given converter function and return type.
	 * This is a convenience method for creating property handlers that only need to convert
	 * a value and do not support changing the property or hold any state.
	 *
	 * @param converter The function to convert the object to the property value.
	 * @param returnType The return type of the property.
	 * @param <Type> The type of object this property can be applied to.
	 * @param <ReturnType> The type of object that is returned by this property.
	 * @return A new property handler that uses the given converter and return type.
	 */
	@Contract(value = "_, _ -> new", pure = true)
	static <Type, ReturnType> @NotNull ExpressionPropertyHandler<Type, ReturnType> of(
		Function<Type, ReturnType> converter,
		@NotNull Class<ReturnType> returnType
	) {
		return new ExpressionPropertyHandler<>() {

			@Override
			public @Nullable ReturnType convert(Type propertyHolder) {
				return converter.apply(propertyHolder);
			}

			@Override
			public @NotNull Class<ReturnType> returnType() {
				return returnType;
			}
		};
	}

}
