package ch.njol.skript.expressions.base;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxElement;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.google.common.base.Preconditions;
import java.util.Arrays;
import org.bukkit.event.Event;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;
import org.skriptlang.skript.lang.converter.Converter;
import org.skriptlang.skript.lang.converter.Converters;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;
import org.skriptlang.skript.util.Priority;

/**
 * Represents an expression which represents a property of another one. Remember to set the expression with {@link #setExpr(Expression)} in
 * {@link SyntaxElement#init(Expression[], int, Kleenean, ParseResult) init()}.
 *
 * @see SimplePropertyExpression
 * @see #register(Class, Class, String, String)
 */
public abstract class PropertyExpression<F, T> extends SimpleExpression<T> {

	/**
	 * A helper method to get the property patterns given a property, type, and default expression parameter.
	 * @param property the property
	 * @param fromType the type(s) that the property should apply to
	 * @param defaultExpr whether the type(s) should be optional
	 * @return an array of strings representing the patterns of the given property and type(s)
	 * @throws IllegalArgumentException if property or fromType is null
	 */
	private static String[] patternsOf(String property, String fromType, boolean defaultExpr) {
		Preconditions.checkNotNull(property, "property must be present");
		Preconditions.checkNotNull(fromType, "fromType must be present");
		String types = defaultExpr ? "[of %" + fromType + "%]" : "of %" + fromType + "%";
		return new String[]{"[the] " + property + " " + types, "%" + fromType + "%'[s] " + property};
	}

	/**
	 * Returns the standard property patterns given a property and type(s).
	 * i.e. "[the] property of %types%" and "%types%'[s] property"
	 *
	 * @param property the property
	 * @param fromType the type(s) that the property should apply to
	 *
	 * @return an array of strings representing the standard property patterns given a property and type(s)
	 * @throws IllegalArgumentException if property or fromType is null
	 */
	public static String[] getPatterns(String property, String fromType) {
		return patternsOf(property, fromType, false);
	}

	/**
	 * Returns the property patterns given a property and type(s), with the latter being optional to force a default expression.
	 * i.e. "[the] property [of %types%]" and "%types%'[s] property"
	 *
	 * @param property the property
	 * @param fromType the type(s) that the property should apply to
	 *
	 * @return an array of strings representing the property patterns given a property and type(s), that force a default expression
	 * @throws IllegalArgumentException if property or fromType is null
	 */
	public static String[] getDefaultPatterns(String property, String fromType) {
		return patternsOf(property, fromType, true);
	}

	/**
	 * A priority for {@link PropertyExpression}s.
	 * They will be registered before {@link SyntaxInfo#PATTERN_MATCHES_EVERYTHING} expressions
	 *  but after {@link SyntaxInfo#COMBINED} expressions.
	 */
	@ApiStatus.Experimental
	public static final Priority DEFAULT_PRIORITY = Priority.before(SyntaxInfo.PATTERN_MATCHES_EVERYTHING);

	/**
	 * Registers an expression with the two default property patterns "property of %types%" and "%types%'[s] property"
	 *
	 * @param registry The SyntaxRegistry to register with.
	 * @param expressionClass The PropertyExpression class being registered.
	 * @param returnType The class representing the expression's return type.
	 * @param property The name of the property.
	 * @param fromType Should be plural to support multiple objects but doesn't have to be.
	 * @param <T> The return type.
	 * @param <E> The Expression type.
	 * @return The registered {@link SyntaxInfo}.
	 */
	@ApiStatus.Experimental
	public static <E extends Expression<T>, T> SyntaxInfo.Expression<E, T> register(SyntaxRegistry registry, Class<E> expressionClass, Class<T> returnType, String property, String fromType) {
		SyntaxInfo.Expression<E, T> info = SyntaxInfo.Expression.builder(expressionClass, returnType)
				.priority(DEFAULT_PRIORITY)
				.addPatterns(getPatterns(property, fromType))
				.build();
		registry.register(SyntaxRegistry.EXPRESSION, info);
		return info;
	}

	/**
	 * Registers an expression as {@link ExpressionType#PROPERTY} with the two default property patterns "property of %types%" and "%types%'[s] property"
	 *
	 * @param expressionClass the PropertyExpression class being registered.
	 * @param type the main expression type the property is based off of.
	 * @param property the name of the property.
	 * @param fromType should be plural to support multiple objects but doesn't have to be.
	 */
	public static <T> void register(Class<? extends Expression<T>> expressionClass, Class<T> type, String property, String fromType) {
		Skript.registerExpression(expressionClass, type, ExpressionType.PROPERTY, getPatterns(property, fromType));
	}

	/**
	 * Registers an expression with the two default property patterns "property [of %types%]" and "%types%'[s] property"
	 * This method also makes the expression type optional to force a default expression on the property expression.
	 *
	 * @param registry The SyntaxRegistry to register with.
	 * @param expressionClass The PropertyExpression class being registered.
	 * @param returnType The class representing the expression's return type.
	 * @param property The name of the property.
	 * @param fromType Should be plural to support multiple objects but doesn't have to be.
	 * @param <T> The return type.
	 * @param <E> The Expression type.
	 * @return The registered {@link SyntaxInfo}.
	 */
	@ApiStatus.Experimental
	public static <E extends Expression<T>, T> SyntaxInfo.Expression<E, T> registerDefault(SyntaxRegistry registry, Class<E> expressionClass, Class<T> returnType, String property, String fromType) {
		SyntaxInfo.Expression<E, T> info = SyntaxInfo.Expression.builder(expressionClass, returnType)
				.priority(DEFAULT_PRIORITY)
				.addPatterns(getDefaultPatterns(property, fromType))
				.build();
		registry.register(SyntaxRegistry.EXPRESSION, info);
		return info;
	}

	/**
	 * Registers an expression as {@link ExpressionType#PROPERTY} with the two default property patterns "property [of %types%]" and "%types%'[s] property"
	 * This method also makes the expression type optional to force a default expression on the property expression.
	 *
	 * @param expressionClass the PropertyExpression class being registered.
	 * @param type the main expression type the property is based off of.
	 * @param property the name of the property.
	 * @param fromType should be plural to support multiple objects but doesn't have to be.
	 */
	public static <T> void registerDefault(Class<? extends Expression<T>> expressionClass, Class<T> type, String property, String fromType) {
		Skript.registerExpression(expressionClass, type, ExpressionType.PROPERTY, getDefaultPatterns(property, fromType));
	}

	private @UnknownNullability Expression<? extends F> expr;

	/**
	 * Sets the expression this expression represents a property of. No reference to the expression should be kept.
	 *
	 * @param expr The expression this expression represents a property of.
	 */
	protected final void setExpr(@NotNull Expression<? extends F> expr) {
		Preconditions.checkNotNull(expr, "The expr param cannot be null");
		this.expr = expr;
	}

	public final Expression<? extends F> getExpr() {
		return expr;
	}

	@Override
	protected final T[] get(Event event) {
		return get(event, expr.getArray(event));
	}

	@Override
	public final T[] getAll(Event event) {
		T[] result = get(event, expr.getAll(event));
		return Arrays.copyOf(result, result.length);
	}

	/**
	 * Converts the given source object(s) to the correct type.
	 * <p>
	 * Please note that the returned array must neither be null nor contain any null elements!
	 *
	 * @param event the event involved at the time of runtime calling.
	 * @param source the array of the objects from the expressions.
	 * @return An array of the converted objects, which may contain less elements than the source array, but must not be null.
	 * @see Converters#convert(Object[], Class, Converter)
	 */
	protected abstract T[] get(Event event, F[] source);

	/**
	 * @param source the array of the objects from the expressions.
	 * @param converter must return instances of {@link #getReturnType()}
	 * @return An array containing the converted values
	 * @throws ArrayStoreException if the converter returned invalid values
	 */
	@SuppressWarnings("deprecation") // for backwards compatibility
	protected T[] get(final F[] source, final ch.njol.skript.classes.Converter<? super F, ? extends T> converter) {
		assert source != null;
		assert converter != null;
		return ch.njol.skript.registrations.Converters.convertUnsafe(source, getReturnType(), converter);
	}

	@Override
	public boolean isSingle() {
		return expr.isSingle();
	}

	@Override
	public final boolean getAnd() {
		return expr.getAnd();
	}

	@Override
	public Expression<? extends T> simplify() {
		expr = expr.simplify();
		return this;
	}

}
