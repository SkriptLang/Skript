package org.skriptlang.skript.lang.condition;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.Contract;
import org.skriptlang.skript.lang.condition.Conditional.Operator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.skriptlang.skript.lang.condition.Conditional.negate;

/**
 * Builds a Disjunctive Normal Form {@link CompoundConditional}, meaning it is solely composed of groups of ANDs all ORed together,
 * ex. {@code (a && !b && c) || b || (!c && d)}.
 * <br>
 * A builder should no longer be used after calling {@link #build()}.
 * @param <T> The context class to use for evaluation.
 */
public class DNFConditionalBuilder<T> {

	private CompoundConditional<T> root;

	/**
	 * Creates an empty builder.
	 */
	DNFConditionalBuilder() {
		this.root = null;
	}

	/**
	 * Creates a builder with a single conditional.
	 * @param root If given a {@link CompoundConditional}, it is used as the root conditional.
	 *             Otherwise, a OR compound conditional is created as the root with the given conditional within.
	 */
	DNFConditionalBuilder(Conditional<T> root) {
		if (root instanceof CompoundConditional<T> compoundConditional) {
			this.root = compoundConditional;
		} else {
			this.root = new CompoundConditional<>(Operator.OR, root);
		}
	}

	/**
	 * @return The root conditional, which will be DNF-compliant
	 * @throws IllegalStateException if the builder is empty.
	 */
	public Conditional<T> build() {
		return Preconditions.checkNotNull(root, "Cannot build an empty conditional!");
	}

	/**
	 * Adds conditionals to the root node via the AND operator: {@code (existing) && newA && newB && ...}.
	 * If the root is currently OR, the statement is transformed as follows to maintain DNF:
	 * {@code (a || b) && c -> (a && c) || (b && c)}
	 * @param andConditionals conditionals to AND to the existing conditional.
	 * @return the builder
	 */
	@SafeVarargs
	@Contract("_ -> this")
	public final DNFConditionalBuilder<T> and(Conditional<T>... andConditionals) {
		if (root == null) {
			root = new CompoundConditional<>(Operator.AND, andConditionals);
			return this;
		}

		// unroll conditionals if they're ANDs
		List<Conditional<T>> newConditionals = unroll(List.of(andConditionals), Operator.AND);

		// if the root is still just AND, we can just append.
		if (root.getOperator() == Operator.AND) {
			root.addConditionals(newConditionals);
			return this;
		}
		// Otherwise, we need to transform:
		// (a || b) && c -> (a && c) || (b && c)
		List<Conditional<T>> transformedConditionals = new ArrayList<>();
		newConditionals.add(0, null); // just for padding
		for (Conditional<T> conditional : root.getConditionals()) {
			newConditionals.set(0, conditional);
			transformedConditionals.add(new CompoundConditional<>(Operator.AND, newConditionals));
		}

		root = new CompoundConditional<>(Operator.OR, transformedConditionals);
		return this;
	}

	/**
	 * Adds conditionals to the root node via the OR operator: {@code (existing) || newA || newB || ...}.
	 * If the root is currently AND, a new OR root node is created containing the previous root and the new conditionals.
	 * @param orConditionals conditionals to OR to the existing conditional.
	 * @return the builder
	 */
	@SafeVarargs
	@Contract("_ -> this")
	public final DNFConditionalBuilder<T> or(Conditional<T>... orConditionals) {
		if (root == null) {
			root = new CompoundConditional<>(Operator.OR, orConditionals);
			return this;
		}

		// unroll conditionals if they're ORs
		List<Conditional<T>> newConditionals = unroll(List.of(orConditionals), Operator.OR);

		// Since DNF is a series of ANDs, ORed together, we can simply add these to the root if it's already OR.
		if (root.getOperator() == Operator.OR) {
			root.addConditionals(newConditionals);
			return this;
		}
		// otherwise we need to nest the AND/NOT condition within a new root with OR operator.
		newConditionals.add(0, root);
		root = new CompoundConditional<>(Operator.OR, newConditionals);
		return this;
	}

	/**
	 * Adds a negated conditional to the root node via the AND and NOT operators: {@code (existing) && !new}.
	 * @param conditional The conditional to negate and add.
	 * @return the builder
	 */
	@Contract("_ -> this")
	public DNFConditionalBuilder<T> andNot(Conditional<T> conditional) {
		return and(negate(conditional));
	}

	/**
	 * Adds a negated conditional to the root node via the OR and NOT operators: {@code (existing) || !new}.
	 * @param conditional The conditional to negate and add.
	 * @return the builder
	 */
	@Contract("_ -> this")
	public DNFConditionalBuilder<T> orNot(Conditional<T> conditional) {
		return or(negate(conditional));
	}

	/**
	 * Adds conditionals to the root node via the AND or OR operators.
	 * A helper for dynamically adding conditionals.
	 * @param or Whether to use OR (true) or AND (false)
	 * @param conditionals The conditional to add.
	 * @return the builder
	 */
	@SafeVarargs
	@Contract("_,_ -> this")
	public final DNFConditionalBuilder<T> add(boolean or, Conditional<T>... conditionals) {
		if (or)
			return or(conditionals);
		return and(conditionals);
	}

	/**
	 * Unrolls nested conditionals which are superfluous:
	 * {@code a || (b || c) -> a || b || c}
	 * @param conditionals A collection of conditionals to unroll.
	 * @param operator Which operator to unroll.
	 * @return A new list of conditionals without superfluous nesting.
	 */
	@Contract("_,_ -> new")
	private static <T> List<Conditional<T>> unroll(Collection<Conditional<T>> conditionals, Operator operator) {
		List<Conditional<T>> newConditionals = new ArrayList<>();
		for (Conditional<T> conditional : conditionals) {
			if (conditional instanceof CompoundConditional<T> compound && compound.getOperator() == operator) {
				newConditionals.addAll(unroll(compound.getConditionals(), operator));
			} else {
				newConditionals.add(conditional);
			}
		}
		return newConditionals;
	}

}
