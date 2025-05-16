package ch.njol.skript.lang.util.loops;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.sections.SecLoop;
import ch.njol.util.Kleenean;
import com.google.common.base.Preconditions;
import org.bukkit.event.Event;

import javax.annotation.Nullable;
import java.lang.reflect.Method;

/**
 * Abstract class for expressions used in loops within the Skript plugin.
 * Allows defining special expressions like loop-val, loop-key, etc.,
 * which return the current value within a loop section (e.g., in a loop block).
 * @param <T> The type of value returned by the expression.
 */
public abstract class AbstractLoopExpression<T> extends SimpleExpression<T> {
	boolean itsIntendedLoop = false;
	protected SecLoop loop;
	protected String name;
	protected Class<? extends Expression<?>> expressionToLoop;
	protected String expressionName;

	/**
	 * Registers an expression as {@link ExpressionType#SIMPLE} with a pattern like loop-val, loop-key, etc.
	 *
	 * @param expressionClass The expression class (subclass of AbstractLoopExpression).
	 * @param returnType The type of value returned by the expression.
	 * @param pattern The pattern for the loop expression (e.g., "val", "key").
	 */
	public static <T> void register(
		Class<? extends AbstractLoopExpression<T>> expressionClass,
		Class<T> returnType,
		String pattern
	) {
		Preconditions.checkNotNull(pattern, "pattern must be present");

		Skript.info("Registering " + expressionClass.getSimpleName() + " " + "[the] loop-" + pattern + "[-%-*integer%]");
		Skript.registerExpression(expressionClass, returnType, ExpressionType.SIMPLE, "[the] loop-" + pattern + "[-%-*integer%]");
	}

	@SuppressWarnings("unchecked")
	protected static <T> T[] callGetMethod(Expression<T> expression, Event event) {
		try {
			Method getMethod = expression.getClass().getSuperclass().getDeclaredMethod("get", Event.class);
			getMethod.setAccessible(true);
			return (T[]) getMethod.invoke(expression, event);
		} catch (Exception ex) {
			throw Skript.exception(ex, "Failed to get value from expression %s", expression.toString(event, false));
		}
	}

	/**
	 * Returns a string representation of the expression for debugging.
	 * If the expression is in the intended loop, returns the current loop value.
	 * @param e The event (can be null).
	 * @param debug Whether to use debug output.
	 * @return String representation of the value.
	 */
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		if (e == null)
			return name;
		if (itsIntendedLoop) {
			final Object current = loop.getCurrent(e);
			Object[] objects = callGetMethod(loop.getLoopedExpression(), e);

			if (current == null || objects == null)
				return Classes.getDebugMessage(null);
			return Classes.getDebugMessage(current);
		}
		return Classes.getDebugMessage(loop.getCurrent(e));
	}

	/**
	 * Determines whether the given loop is intended for this expression.
	 * This method also acts as a secondary initialization point, because it is invoked directly during {@code init}.
	 * If this method returns {@code false}, the initialization will fail.
	 * <p>
	 * Using the provided {@code initializer}, you can set up internal state, expressions, parse marks, etc.,
	 * just like you would normally do inside {@code init}.
	 *
	 * <p><b>Example:</b></p>
	 * <pre>{@code
	 * public boolean isIntendedLoop(AbstractLoop loop, final AbstractExpressionInitializer initializer) {
	 *     switch (initializer.parser().mark) {
	 *         case 1 -> state = LoopState.KEY;
	 *         case 2 -> state = LoopState.VALUE;
	 *         case 5 -> state = LoopState.INDEX;
	 *         case -1 -> { return false; }
	 *     }
	 *     return true;
	 * }
	 * }</pre>
	 *
	 * @param loop The loop section.
	 * @param initializer The expression initializer.
	 * @return {@code true} if the loop is intended for this expression, {@code false} otherwise.
	 */

	public abstract boolean isIntendedLoop(SecLoop loop, final AbstractExpressionInitializer initializer);

	/**
	 * Finds the corresponding loop section by order and expression type.
	 * @param i The order of the loop (e.g., 1 for the first loop).
	 * @param expressionName The name of the expression (e.g., "val").
	 * @param cls The class of the expression to be looped.
	 * @return The corresponding loop section or null.
	 */
	private SecLoop getSecLoop(int i, String expressionName, Class<? extends Expression<?>> cls) {
		if (cls == null) return null;
		this.expressionName = expressionName;
		int j = 1;
		SecLoop loop = null;
		for (SecLoop l : ParserInstance.get().getCurrentSections(SecLoop.class)) {
			if (l.getLoopedExpression().getClass().isAssignableFrom(cls)) {
				if (j < i) {
					j++;
					continue;
				}
				if (loop != null) {
					return null;
				}
				loop = l;
				if (j == i)
					break;
			}
		}
		return loop;
	}

	/**
	 * Returns the class of the expression to be looped.
	 * @return The expression class.
	 */
	public abstract Class<? extends Expression<?>> getExpressionToLoop();

	/**
	 * Initializes the expression within a loop.
	 * @param vars Array of expressions (e.g., the loop order number).
	 * @param matchedPattern The index of the matched pattern.
	 * @param isDelayed Whether the expression is delayed.
	 * @param parser The parse result.
	 * @return true if initialization was successful, false otherwise.
	 */
	@Override
	public boolean init(final Expression<?>[] vars, final int matchedPattern, final Kleenean isDelayed, final SkriptParser.ParseResult parser) {
		name = parser.expr;
		var number = (Expression<Integer>) vars[0];
		String expressionName = name.split("-")[1];
		int i = -1;
		if (number != null) {
			i = ((Literal<Integer>) number).getSingle();
		}
		expressionToLoop = getExpressionToLoop();
		if (expressionToLoop == null || !Expression.class.isAssignableFrom(expressionToLoop)) return false;
		SecLoop loop = getSecLoop(i, expressionName, this.getExpressionToLoop());

		if (loop == null) {
			Skript.error("here are multiple loops that match loop-%s. Use loop-%s-1/2/3/etc. to specify which loop's value you want.".formatted(expressionName, expressionName));
			return false;
		}

		if (loop.getObject() == null) {
			Skript.error("There's no loop that matches 'loop-%s'.".formatted(expressionName));
			return false;
		}

		var initializer = new AbstractExpressionInitializer(vars, matchedPattern, isDelayed, parser);
		if (! this.isIntendedLoop(loop, initializer)) return false;

		this.loop = loop;
		return true;
	}
}
