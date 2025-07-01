package ch.njol.skript.lang.simplification;


import ch.njol.skript.classes.Changer;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.util.ContextlessEvent;
import ch.njol.skript.lang.util.SimpleLiteral;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * Represents a literal, i.e. a static value like a number or a string, that was created by simplifying another expression.
 * Maintains a reference to the original expression to allow for changers and toString generation.
 * @param <T> the type of the literal
 */
public class SimplifiedLiteral<T> extends SimpleLiteral<T> {

	/**
	 * Creates a new simplified literal from an expression by evaluating it with a {@link ContextlessEvent}.
	 * Any expression that requires specific event data cannot be safely simplified to a literal.
	 * The original expression is stored for later toString generation.
	 *
	 * @param original the original expression to simplify
	 * @param <T> the type of the literal
	 * @return a new simplified literal
	 */
	public static <T> SimplifiedLiteral<T> fromExpression(Expression<T> original) {
		Event event = ContextlessEvent.get();

		if (original instanceof SimplifiedLiteral<T> literal)
			return literal;

		T[] values = original.getAll(event);

		//noinspection unchecked
		return new SimplifiedLiteral<>(
			values,
			(Class<T>) values.getClass().getComponentType(),
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
	public Class<?> @Nullable [] acceptChange(Changer.ChangeMode mode) {
		return sourceExpr.acceptChange(mode);
	}

	@Override
	public Object @Nullable [] beforeChange(Expression<?> changed, Object @Nullable [] delta) {
		return sourceExpr.beforeChange(changed, delta);
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, Changer.ChangeMode mode) throws UnsupportedOperationException {
		sourceExpr.change(event, delta, mode);
	}

	@Override
	public boolean isLoopOf(String input) {
		return sourceExpr.isLoopOf(input);
	}

	@Override
	public <R> void changeInPlace(Event event, Function<T, R> changeFunction) {
		sourceExpr.changeInPlace(event, changeFunction);
	}

	@Override
	public <R> void changeInPlace(Event event, Function<T, R> changeFunction, boolean getAll) {
		sourceExpr.changeInPlace(event, changeFunction, getAll);
	}

	@Override
	public Expression<?> getSource() {
		return sourceExpr;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (debug)
			return "[" + sourceExpr.toString(event, true) + " (SIMPLIFIED)]";
		return sourceExpr.toString(event, false);
	}

}
