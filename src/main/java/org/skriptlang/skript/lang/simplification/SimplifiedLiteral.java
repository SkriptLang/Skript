package org.skriptlang.skript.lang.simplification;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.util.ContextlessEvent;
import ch.njol.skript.lang.util.SimpleLiteral;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a literal, i.e. a static value like a number or a string, that was created by simplifying another expression.
 * @param <T> the type of the literal
 */
public class SimplifiedLiteral<T> extends SimpleLiteral<T> {

	public static <T> SimplifiedLiteral<T> fromExpression(Expression<T> original) {
		Event event = ContextlessEvent.get();

		if (original instanceof SimplifiedLiteral<T> literal)
			return literal;

		//noinspection unchecked
		return new SimplifiedLiteral<>(
				original.getAll(event),
				(Class<T>) original.getReturnType(),
				original.getAnd(),
				original);
	}

	Expression<T> sourceExpr;

	/**
	 * Creates a new simplified literal.
	 * @param data the data of the literal
	 * @param type the type of the literal
	 * @param and whether the literal is an "and" literal
	 * @param source the source expression this literal was created from. Used for toString values.
	 */
	public SimplifiedLiteral(T[] data, Class<T> type, boolean and, Expression<T> source) {
		super(data, type, and);
		sourceExpr = source;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (debug)
			return "[" + sourceExpr.toString(event, true) + " (SIMPLIFIED)]";
		return sourceExpr.toString(event, false);
	}

}
