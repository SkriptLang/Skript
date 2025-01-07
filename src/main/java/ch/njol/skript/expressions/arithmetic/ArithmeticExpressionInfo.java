package ch.njol.skript.expressions.arithmetic;

import org.bukkit.event.Event;

import ch.njol.skript.lang.Expression;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.arithmetic.Arithmetics;

public class ArithmeticExpressionInfo<T> implements ArithmeticGettable<T> {
	
	private final Expression<? extends T> expression;
	
	public ArithmeticExpressionInfo(Expression<? extends T> expression) {
		this.expression = expression;
	}

	@Override
	@Nullable
	public T get(Event event) {
		T object = expression.getSingle(event);
		return object == null ? Arithmetics.getDefaultValue(expression.getReturnType()) : object;
	}

	@Override
	public Class<? extends T> getReturnType() {
		return expression.getReturnType();
	}

}
