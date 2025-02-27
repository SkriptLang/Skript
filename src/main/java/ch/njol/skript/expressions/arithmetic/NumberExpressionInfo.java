package ch.njol.skript.expressions.arithmetic;

import ch.njol.skript.lang.Expression;
import org.bukkit.event.Event;

@Deprecated
public class NumberExpressionInfo implements ArithmeticGettable<Number> {

	private final Expression<? extends Number> expression;

	public NumberExpressionInfo(Expression<? extends Number> expression) {
		this.expression = expression;
	}

	public Number get(Event event, boolean integer) {
		return get(event);
	}

	@Override
	public Number get(Event event) {
		Number number = expression.getSingle(event);
		return number != null ? number : 0;
	}

	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}
}
