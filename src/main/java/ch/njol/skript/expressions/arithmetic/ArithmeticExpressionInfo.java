package ch.njol.skript.expressions.arithmetic;

import org.bukkit.event.Event;

import ch.njol.skript.lang.Expression;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.arithmetic.Arithmetics;

/**
 * Arithmetic gettable wrapped around an expression.
 *
 * @param <T> expression type
 */
public record ArithmeticExpressionInfo<T>(Expression<? extends T> expression) implements ArithmeticGettable<T> {

	@Override
	public @Nullable T get(Event event) {
		T object = expression.getSingle(event);
		return object == null ? Arithmetics.getDefaultValue(expression.getReturnType()) : object;
	}

	@Override
	public Class<? extends T> getReturnType() {
		return expression.getReturnType();
	}

}
