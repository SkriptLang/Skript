package org.skriptlang.skript.lang.properties;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.parser.ParserInstance;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.common.conditions.PropCondContains;
import org.skriptlang.skript.common.types.ScriptClassInfo;

import java.util.function.Function;

/**
 * A handler for a specific property. Any method of resolving or changing the property should be done here.
 * A handler can be nearly anything and do nearly anything. Some examples are provided in the sub-interfaces.
 * <br>
 * If a handler needs to store state, it should override {@link #newInstance()} to return a new instance of itself.
 * Each new instance will be initialized with {@link #init(Expression, ParserInstance)} before use, so state can be
 * set up there if it depends on the parser instance or parent expression.
 *
 * @see ExpressionPropertyHandler
 * @param <Type> The type of object this property can be applied to.
 */
public interface PropertyHandler<Type> {

	/**
	 * Creates a new instance of this handler. If a handler does not need to store state, it can simply return {@code this}.
	 * If a handler needs to store state, it **MUST** return a new instance of itself. See {@link ScriptClassInfo.ScriptNameHandler}
	 * for an example of a stateful handler.
	 *
	 * @return A new instance of this handler, or {@code this} if no state is stored.
	 */
	default PropertyHandler<Type> newInstance() {
		return this;
	}

	/**
	 * Initializes this handler with the given parser instance. This method is called once after {@link #newInstance()}.
	 * If the handler does not need any initialization, it can simply return {@code true}.
	 * <br>
	 * It is safe to print warnings or errors from this method.
	 *
	 * @param parentExpression The expression that is using this handler. Can be used to get context about the property usage.
	 * @param parser The parser instance that will use this handler.
	 * @return {@code true} if the handler was initialized successfully, {@code false} otherwise.
	 */
	default boolean init(Expression<?> parentExpression, ParserInstance parser) {
		return true;
	}

	/**
	 * A handler that can check if a container contains a specific element.
	 *
	 * @param <Container> The type of object that can contain elements.
	 * @param <Element> The type of object that can be contained.
	 *
	 * @see PropCondContains
	 */
	interface ContainsHandler<Container, Element> extends PropertyHandler<Container> {
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

	/**
	 * A handler that can get and optionally change a property value. This interface is suitable for properties that act
	 * like expressions, such as "name", "display name", etc. Properties that use this interface should also use
	 * {@link PropertyBaseExpression} for the parent expression.
	 * @param <Type> The type of object this property can be applied to.
	 * @param <ReturnType> The type of object that is returned by this property.
	 *
	 * @see PropertyBaseExpression
	 */
	interface ExpressionPropertyHandler<Type, ReturnType> extends PropertyHandler<Type> {

		/**
		 * Converts the given object to the property value. This method may return arrays if the property is multi-valued.
		 *
		 * @param propertyHolder The object to convert.
		 * @return The property value.
		 */
		ReturnType convert(Type propertyHolder);

		/**
		 * Returns the types of changes that this property supports. If the property does not support any changes,
		 * this method should return {@code null}. If the property supports changes, it should return the classes
		 * that are accepted for each change mode. {@link ChangeMode#RESET} and {@link ChangeMode#DELETE} do not require
		 * any specific types, so they can return an empty or non-empty array.
		 * <br>
		 * The default implementation returns {@code null}, indicating that the property is read-only.
		 *
		 * @param mode The change mode to check.
		 * @return The types supported by this property for the given change mode, or {@code null} if the property is read-only.
		 * @see Expression#acceptChange(ChangeMode)
		 */
		default Class<?> @Nullable [] acceptChange(ChangeMode mode) {
			return null;
		}

		/**
		 * Changes the property value of the given object. This method is only called if {@link #acceptChange(ChangeMode)}
		 * returns a non-null value for the given change mode.
		 *
		 * @param propertyHolder The object to change.
		 * @param delta The new value(s) to set. This is {@code null} for {@link ChangeMode#RESET} and {@link ChangeMode#DELETE}.
		 * @param mode The change mode to apply.
		 * @throws UnsupportedOperationException If the property is read-only and does not support changes.
		 */
		default void change(Type propertyHolder, Object @Nullable [] delta, ChangeMode mode) {
			throw new UnsupportedOperationException("Changing the name is not supported for this property.");
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
				public ReturnType convert(Type propertyHolder) {
					return converter.apply(propertyHolder);
				}

				@Override
				public @NotNull Class<ReturnType> returnType() {
					return returnType;
				}
			};
		}
	}

}
