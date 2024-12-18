package org.skriptlang.skript.lang.condition;

import ch.njol.skript.lang.Debuggable;
import ch.njol.util.Kleenean;
import com.google.common.base.Preconditions;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * An object which can evaluate to `true`, `false`, or `unknown`.
 * `unknown` is currently unused, but intended for future handling of unexpected runtime situations, where some aspect of
 * the condition in ill-defined by the user and would result in ambiguous or undefined behavior.
 * @param <T> The context class to use for evaluation.
 */
public interface Conditional<T> extends Debuggable {

	/**
	 * Evaluates this object as `true`, `false`, or `unknown`.
	 * This value may change between subsequent callings.
	 *
	 * @param context The context with which to evaluate this object.
	 * @return The evaluation of this object.
	 */
	@Contract(pure = true)
	Kleenean evaluate(T context);

	/**
	 * Evaluates this object as `true`, `false`, or `unknown`.
	 * This value may change between subsequent callings.
	 * May use a mutable cache of evaluated conditionals to prevent duplicate evaluations.
	 *
	 * @param context The context with which to evaluate this object.
	 * @param cache The cache of evaluated conditionals.
	 * @return The evaluation of this object.
	 */
	@Contract(pure = true)
	default Kleenean evaluate(T context, @Nullable Map<Conditional<T>, Kleenean> cache) {
		if (cache == null)
			return evaluate(context);
		//noinspection DataFlowIssue
		return cache.computeIfAbsent(this, cond -> cond.evaluate(context));
	}

	/**
	 * Computes {@link Kleenean#and(Kleenean)} with the evaluations of this {@link Conditional} and the other.
	 * Evaluates the other first, and shortcuts if it is not {@link Kleenean#TRUE}.
	 *
	 * @param other The {@link Conditional} to AND with. Will always be evaluated.
	 * @param context The context with which to evaluate the conditionals.
	 * @return The result of {@link Kleenean#and(Kleenean)}, given the evaluations of the two conditionals.
	 */
	@Contract(pure = true)
	default Kleenean evaluateAnd(Conditional<T> other, T context) {
		return evaluateAnd(other.evaluate(context, null), context, null);
	}

	/**
	 * Computes {@link Kleenean#and(Kleenean)} with the evaluations of this {@link Conditional} and the other.
	 * Evaluates the other first, and shortcuts if it is not {@link Kleenean#TRUE}.
	 * Uses a mutable cache of evaluated conditionals to prevent duplicate evaluations.
	 *
	 * @param other The {@link Conditional} to AND with. Will always be evaluated.
	 * @param context The context with which to evaluate the conditionals.
	 * @param cache The cache of evaluated conditionals.
	 * @return The result of {@link Kleenean#and(Kleenean)}, given the evaluations of the two conditionals.
	 */
	@Contract(pure = true)
	default Kleenean evaluateAnd(Conditional<T> other, T context, @Nullable Map<Conditional<T>, Kleenean> cache) {
		return evaluateAnd(other.evaluate(context, cache), context, cache);
	}

	/**
	 * Computes {@link Kleenean#and(Kleenean)} with the evaluation of this {@link Conditional} and the given Kleenean.
	 * Evaluates this object iff the given {@link Kleenean} is {@link Kleenean#TRUE}.
	 *
	 * @param other The {@link Kleenean} to AND with.
	 * @param context The context with which to evaluate the conditional, if necessary.
	 * @return The result of {@link Kleenean#and(Kleenean)}, given the evaluation of the conditional.
	 */
	@Contract(pure = true)
	default Kleenean evaluateAnd(Kleenean other, T context) {
		return evaluateAnd(other, context, null);
	}

	/**
	 * Computes {@link Kleenean#and(Kleenean)} with the evaluation of this {@link Conditional} and the given Kleenean.
	 * Evaluates this object iff the given {@link Kleenean} is not {@link Kleenean#FALSE}.
	 * Uses a mutable cache of evaluated conditionals to prevent duplicate evaluations.
	 *
	 * @param other The {@link Kleenean} to AND with.
	 * @param context The context with which to evaluate the conditional, if necessary.
	 * @param cache The cache of evaluated conditionals.
	 * @return The result of {@link Kleenean#and(Kleenean)}, given the evaluation of the conditional.
	 */
	@Contract(pure = true)
	default Kleenean evaluateAnd(Kleenean other, T context, @Nullable Map<Conditional<T>, Kleenean> cache) {
		if (other.isFalse())
			return other;
		return other.and(evaluate(context, cache));
	}

	/**
	 * Computes {@link Kleenean#or(Kleenean)} with the evaluations of this {@link Conditional} and the other.
	 * Evaluates the other first, and shortcuts if it is not {@link Kleenean#TRUE}.
	 *
	 * @param other The {@link Conditional} to OR with. Will always be evaluated.
	 * @param context The context with which to evaluate the conditionals.
	 * @return The result of {@link Kleenean#or(Kleenean)}, given the evaluations of the two conditionals.
	 */
	@Contract(pure = true)
	default Kleenean evaluateOr(Conditional<T> other, T context) {
		return evaluateOr(other.evaluate(context, null), context, null);
	}

	/**
	 * Computes {@link Kleenean#or(Kleenean)} with the evaluations of this {@link Conditional} and the other.
	 * Evaluates the other first, and shortcuts if it is not {@link Kleenean#TRUE}.
	 * Uses a mutable cache of evaluated conditionals to prevent duplicate evaluations.
	 *
	 * @param other The {@link Conditional} to OR with. Will always be evaluated.
	 * @param context The context with which to evaluate the conditionals.
	 * @param cache The cache of evaluated conditionals.
	 * @return The result of {@link Kleenean#and(Kleenean)}, given the evaluations of the two conditionals.
	 */
	@Contract(pure = true)
	default Kleenean evaluateOr(Conditional<T> other, T context, @Nullable Map<Conditional<T>, Kleenean> cache) {
		return evaluateOr(other.evaluate(context, cache), context, cache);
	}

	/**
	 * Computes {@link Kleenean#or(Kleenean)} with the evaluation of this {@link Conditional} and the given Kleenean.
	 * Evaluates this object iff the given {@link Kleenean} is {@link Kleenean#FALSE} or {@link Kleenean#UNKNOWN}.
	 *
	 * @param other The {@link Kleenean} to OR with.
	 * @param context The context with which to evaluate the conditional, if necessary.
	 * @return The result of {@link Kleenean#or(Kleenean)}, given the evaluation of the conditional.
	 */
	@Contract(pure = true)
	default Kleenean evaluateOr(Kleenean other, T context) {
		return evaluateOr(other, context, null);
	}

	/**
	 * Computes {@link Kleenean#or(Kleenean)} with the evaluation of this {@link Conditional} and the given Kleenean.
	 * Evaluates this object iff the given {@link Kleenean} is {@link Kleenean#FALSE} or {@link Kleenean#UNKNOWN}.
	 * Uses a mutable cache of evaluated conditionals to prevent duplicate evaluations.
	 *
	 * @param other The {@link Kleenean} to OR with.
	 * @param context The context with which to evaluate the conditional, if necessary.
	 * @param cache The cache of evaluated conditionals.
	 * @return The result of {@link Kleenean#or(Kleenean)}, given the evaluation of the conditional.
	 */
	@Contract(pure = true)
	default Kleenean evaluateOr(Kleenean other, T context, @Nullable Map<Conditional<T>, Kleenean> cache) {
		if (other.isTrue())
			return other;
		return other.or(evaluate(context, cache));
	}

	/**
	 * Computes {@link Kleenean#not()} on the evaluation of this {@link Conditional}.
	 * @param context The context with which to evaluate the conditional.
	 * @return The result of {@link Kleenean#not()}, given the evaluation of the conditional.
	 */
	@Contract(pure = true)
	default Kleenean evaluateNot(T context) {
		return evaluateNot(context, null);
	}

	/**
	 * Computes {@link Kleenean#not()} on the evaluation of this {@link Conditional}.
	 * @param context The context with which to evaluate the conditional.
	 * @return The result of {@link Kleenean#not()}, given the evaluation of the conditional.
	 */
	@Contract(pure = true)
	default Kleenean evaluateNot(T context, @Nullable Map<Conditional<T>, Kleenean> cache) {
		return this.evaluate(context, cache).not();
	}

	/**
	 * Creates a compound conditional from multiple conditionals using {@link Operator#AND} or {@link Operator#OR}.
	 * This does not maintain DNF. Use {@link DNFConditionalBuilder} for that purpose.
	 * @param operator The operator to use (AND or OR).
	 * @param conditionals The conditionals to combine.
	 * @return A new conditional that contains this conditional and the given conditionals
	 */
	@Contract("_, _ -> new")
	static <T> Conditional<T> compound(Operator operator, Collection<Conditional<T>> conditionals) {
		Preconditions.checkArgument(operator != Operator.NOT, "Cannot combine conditionals using NOT!");
		return new CompoundConditional<>(operator, conditionals);
	}

	/**
	 * Creates a compound conditional from multiple conditionals using {@link Operator#AND} or {@link Operator#OR}.
	 * This does not maintain DNF. Use {@link DNFConditionalBuilder} for that purpose.
	 * @param operator The operator to use (AND or OR).
	 * @param conditionals The conditionals to combine.
	 * @return A new conditional that contains this conditional and the given conditionals
	 */
	@SuppressWarnings("unchecked")
	@Contract("_, _ -> new")
	static <T> Conditional<T> compound(Operator operator, Conditional<T>... conditionals) {
		return compound(operator, List.of(conditionals));
	}

	/**
	 * Provides a builder for conditions in disjunctive normal form. ex: {@code (A && B) || C || (!D &&E)}
	 * @param ignoredContextClass The class of the context to use for the built condition.
	 * @return a new builder object for making DNF conditions.
	 */
	@Contract(value = "_ -> new", pure = true)
	static <T> @NotNull DNFConditionalBuilder<T> builderDNF(Class<T> ignoredContextClass) {
		return new DNFConditionalBuilder<>();
	}

	/**
	 * @param conditional A conditional to begin the builder with.
	 * @return a new builder object for making conditions, specifically compound ones.
	 */
	@Contract("_ -> new")
	static <T> @NotNull DNFConditionalBuilder<T> builderDNF(Conditional<T> conditional) {
		return new DNFConditionalBuilder<>(conditional);
	}

	/**
	 * Negates a given conditional. Follows the following transformation rules: <br>
	 * {@code !!a -> a}<br>
	 * {@code !(a || b) -> (!a && !b)}<br>
	 * {@code !(a && b) -> (!a || !b)}<br>
	 * @param conditional The conditional to negate.
	 * @return The negated conditional.
	 */
	static <T> Conditional<T> negate(Conditional<T> conditional) {
		if (!(conditional instanceof CompoundConditional<T> compound))
			return new CompoundConditional<>(Operator.NOT, conditional);

		return switch (compound.getOperator()) {
			// !!a -> a
			case NOT -> compound.getConditionals().getFirst();
			// !(a && b) -> (!a || !b)
			case AND -> {
				List<Conditional<T>> newConditionals = new ArrayList<>();
				for (Conditional<T> cond : compound.getConditionals()) {
					newConditionals.add(negate(cond));
				}
				yield new CompoundConditional<>(Operator.OR, newConditionals);
			}
			// !(a || b) -> (!a && !b)
			case OR -> {
				List<Conditional<T>> newConditionals = new ArrayList<>();
				for (Conditional<T> cond : compound.getConditionals()) {
					newConditionals.add(negate(cond));
				}
				yield new CompoundConditional<>(Operator.AND, newConditionals);
			}
		};
	}

	/**
	 * Represents a boolean logic operator.
	 */
	enum Operator {
		AND("&&"),
		OR("||"),
		NOT("!");

		private final String symbol;

		Operator(String symbol) {
			this.symbol = symbol;
		}

		String getSymbol() {
			return symbol;
		}

	}

}
