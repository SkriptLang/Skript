package ch.njol.skript.lang;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.conditions.CondCompare;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import com.google.common.collect.ImmutableSet;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A list of expressions.
 */
public class ExpressionList<T> implements Expression<T> {

	protected final Expression<? extends T>[] expressions;
	private final Class<T> returnType;
	private final Class<?>[] possibleReturnTypes;
	protected boolean and;
	private final boolean single;

	private final @Nullable ExpressionList<?> source;

	public ExpressionList(Expression<? extends T>[] expressions, Class<T> returnType, boolean and) {
		this(expressions, returnType, and, null);
	}

	public ExpressionList(Expression<? extends T>[] expressions, Class<T> returnType, Class<?>[] possibleReturnTypes, boolean and) {
		this(expressions, returnType, possibleReturnTypes, and, null);
	}

	protected ExpressionList(Expression<? extends T>[] expressions, Class<T> returnType, boolean and, @Nullable ExpressionList<?> source) {
		this(expressions, returnType, new Class[]{returnType}, and, source);
	}

	protected ExpressionList(Expression<? extends T>[] expressions, Class<T> returnType, Class<?>[] possibleReturnTypes, boolean and, @Nullable ExpressionList<?> source) {
		assert expressions != null;
		this.expressions = expressions;
		this.returnType = returnType;
		this.possibleReturnTypes = ImmutableSet.copyOf(possibleReturnTypes).toArray(new Class[0]);
		this.and = and;
		if (and) {
			single = false;
		} else {
			boolean single = true;
			for (Expression<?> e : expressions) {
				if (!e.isSingle()) {
					single = false;
					break;
				}
			}
			this.single = single;
		}
		this.source = source;
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		throw new UnsupportedOperationException();
	}

	@Override
	public @Nullable T getSingle(Event event) {
		if (!single)
			throw new UnsupportedOperationException();
		Expression<? extends T> expression = CollectionUtils.getRandom(expressions);
		return expression != null ? expression.getSingle(event) : null;
	}

	@Override
	public T[] getArray(Event event) {
		if (and)
			return getAll(event);
		Expression<? extends T> expression = CollectionUtils.getRandom(expressions);
		//noinspection unchecked
		return expression != null ? expression.getArray(event) : (T[]) Array.newInstance(returnType, 0);
	}

	@Override
	public T[] getAll(Event event) {
		List<T> values = new ArrayList<>();
		for (Expression<? extends T> expr : expressions)
			values.addAll(Arrays.asList(expr.getAll(event)));
		//noinspection unchecked
		return values.toArray((T[]) Array.newInstance(returnType, values.size()));
	}

	@Override
	public @Nullable Iterator<? extends T> iterator(Event event) {
		if (!and) {
			Expression<? extends T> expression = CollectionUtils.getRandom(expressions);
			return expression != null ? expression.iterator(event) : null;
		}
		return new Iterator<>() {
			private int i = 0;
			@Nullable
			private Iterator<? extends T> current = null;

			@Override
			public boolean hasNext() {
				Iterator<? extends T> iterator = current;
				while (i < expressions.length && (iterator == null || !iterator.hasNext()))
					current = iterator = expressions[i++].iterator(event);
				return iterator != null && iterator.hasNext();
			}

			@Override
			public T next() {
				if (!hasNext())
					throw new NoSuchElementException();
				Iterator<? extends T> iterator = current;
				if (iterator == null)
					throw new NoSuchElementException();
				T value = iterator.next();
				assert value != null : current;
				return value;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	@Override
	public boolean isSingle() {
		return single;
	}

	@Override
	public boolean check(Event event, Predicate<? super T> checker, boolean negated) {
		return CollectionUtils.check(expressions, expr -> expr.check(event, checker) ^ negated, and);
	}

	@Override
	public boolean check(Event event, Predicate<? super T> checker) {
		return check(event, checker, false);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <R> @Nullable Expression<? extends R> getConvertedExpression(Class<R>... to) {
		Expression<? extends R>[] exprs = new Expression[expressions.length];
		Set<Class<?>> possibleReturnTypeSet = new HashSet<>();
		for (int i = 0; i < exprs.length; i++) {
			if ((exprs[i] = expressions[i].getConvertedExpression(to)) == null)
				return null;
			possibleReturnTypeSet.addAll(Arrays.asList(exprs[i].possibleReturnTypes()));
		}
		Class<?>[] possibleReturnTypes = possibleReturnTypeSet.toArray(new Class[0]);
		return new ExpressionList<>(exprs, (Class<R>) Classes.getSuperClassInfo(possibleReturnTypes).getC(), possibleReturnTypes, and, this);
	}

	@Override
	public Class<T> getReturnType() {
		return returnType;
	}

	@Override
	public Class<? extends T>[] possibleReturnTypes() {
		//noinspection unchecked
		return (Class<? extends T>[]) possibleReturnTypes;
	}

	@Override
	public boolean getAnd() {
		return and;
	}

	/**
	 * For use in {@link CondCompare} only.
	 */
	public void invertAnd() {
		and = !and;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {

		// given X: Object.class, Y: Vector.class, Number.class, Z: Integer.class
		// output should be Integer.class.

		// get all accepted type arrays.
		List<Class<?>[]> expressionTypes = new ArrayList<>();
		for (Expression<?> expr : expressions) {
			Class<?>[] exprTypes = expr.acceptChange(mode);
			if (exprTypes == null)
				return null;
			expressionTypes.add(exprTypes);
		}

		// shortcut
		if (expressionTypes.size() == 1)
			return expressionTypes.get(0);

		// iterate over types and keep what works
		Set<Class<?>> acceptable = new LinkedHashSet<>(Arrays.asList(expressionTypes.get(0)));
		for (int i = 1; i < expressionTypes.size(); i++) {
			Set<Class<?>> newAcceptable = new LinkedHashSet<>();

			// Check if each existing acceptable types can be matched to this expr's accepted types
			for (Class<?> candidate : acceptable) {
				for (Class<?> accepted : expressionTypes.get(i)) {
					// keep the more specific version
					if (accepted.isAssignableFrom(candidate)) {
						newAcceptable.add(candidate);
						break;
					} else if (candidate.isAssignableFrom(accepted)) {
						newAcceptable.add(accepted);
						break;
					}
				}
			}

			acceptable = newAcceptable;

			if (acceptable.isEmpty()) {
				return new Class<?>[0]; // Early exit if no common types
			}
		}

		return acceptable.toArray(new Class<?>[0]);
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) throws UnsupportedOperationException {
		if (and) {
			for (Expression<?> expr : expressions) {
				expr.change(event, delta, mode);
			}
		} else {
			int i = ThreadLocalRandom.current().nextInt(expressions.length);
			expressions[i].change(event, delta, mode);
		}
	}

	@Override
	public <R> void changeInPlace(Event event, Function<T, R> changeFunction, boolean getAll) {
		if (and || getAll) {
			for (Expression<?> expr : expressions) {
				//noinspection unchecked,rawtypes
				expr.changeInPlace(event, (Function) changeFunction, getAll);
			}
		} else {
			int i = ThreadLocalRandom.current().nextInt(expressions.length);
			//noinspection unchecked,rawtypes
			expressions[i].changeInPlace(event, (Function) changeFunction, false);
		}
	}

	private int time = 0;

	@Override
	public boolean setTime(int time) {
		boolean ok = false;
		for (Expression<?> e : expressions) {
			ok |= e.setTime(time);
		}
		if (ok)
			this.time = time;
		return ok;
	}

	@Override
	public int getTime() {
		return time;
	}

	@Override
	public boolean isDefault() {
		return false;
	}

	@Override
	public boolean isLoopOf(String input) {
		for (Expression<?> expression : expressions)
			if (expression.isLoopOf(input))
				return true;
		return false;
	}

	@Override
	public Expression<?> getSource() {
		ExpressionList<?> source = this.source;
		return source == null ? this : source;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		StringBuilder result = new StringBuilder("(");
		for (int i = 0; i < expressions.length; i++) {
			if (i != 0) {
				if (i == expressions.length - 1)
					result.append(and ? " and " : " or ");
				else
					result.append(", ");
			}
			result.append(expressions[i].toString(event, debug));
		}
		result.append(")");
		if (debug)
			result.append("[").append(returnType).append("]");
		return result.toString();
	}

	@Override
	public String toString() {
		return toString(null, false);
	}

	/**
	 * @return The internal list of expressions. Can be modified with care.
	 */
	public Expression<? extends T>[] getExpressions() {
		return expressions;
	}

	/**
	 * Retrieves all expressions, including those nested within any {@code ExpressionList}s.
	 *
	 * @return A list of all expressions.
	 */
	public List<Expression<? extends T>> getAllExpressions() {
		List<Expression<? extends T>> expressions = new ArrayList<>();
		for (Expression<? extends T> expression : this.expressions) {
			if (expression instanceof ExpressionList<? extends T> innerList) {
				expressions.addAll(innerList.getAllExpressions());
				continue;
			}
			expressions.add(expression);
		}
		return expressions;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Expression<T> simplify() {
		boolean isLiteralList = true;
		for (int i = 0; i < expressions.length; i++) {
			expressions[i] = expressions[i].simplify();
			isLiteralList &= expressions[i] instanceof Literal;
		}
		if (isLiteralList) {
			Literal<? extends T>[] ls = Arrays.copyOf(expressions, expressions.length, Literal[].class);
			return new LiteralList<>(ls, returnType, and);
		}
		return this;
	}

}
