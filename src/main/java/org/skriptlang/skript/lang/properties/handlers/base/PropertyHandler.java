package org.skriptlang.skript.lang.properties.handlers.base;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.parser.ParserInstance;
import org.jetbrains.annotations.ApiStatus;
import org.skriptlang.skript.common.types.ScriptClassInfo;

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
@ApiStatus.Experimental
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

}
