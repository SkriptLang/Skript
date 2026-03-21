package org.skriptlang.skript.lang.parsing.sites;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.skriptlang.skript.lang.parsing.constraints.Constraints;

import java.util.Iterator;
import java.util.List;

/**
 * An immutable parsing site representing a fixed-order list of expression slots. Each slot in the
 * list carries its own {@link ExpressionSite} with individual constraints; this site itself has no
 * list-level constraints.
 */
public final class ExpressionListSite implements ParsingSite, Iterable<ExpressionSite> {

	private static final Constraints CONSTRAINTS = Constraints.of();

	private final List<ExpressionSite> expressionSites;
	private final boolean optional;

	/**
	 * Creates a new non-optional expression list site containing the given expression slots.
	 * @param expressionSites The ordered list of expression slots.
	 */
	public ExpressionListSite(List<ExpressionSite> expressionSites) {
		this(expressionSites, false);
	}

	/**
	 * Creates a new expression list site containing the given expression slots.
	 * @param expressionSites The ordered list of expression slots.
	 * @param optional Whether this site is optional.
	 */
	public ExpressionListSite(List<ExpressionSite> expressionSites, boolean optional) {
		this.expressionSites = List.copyOf(expressionSites);
		this.optional = optional;
	}

	@Override
	public Constraints constraints() {
		return CONSTRAINTS;
	}

	@Override
	public boolean isOptional() {
		return optional;
	}

	/**
	 * @return An unmodifiable view of the expression slots in this list.
	 */
	public @Unmodifiable List<ExpressionSite> getExpressionSites() {
		return expressionSites;
	}

	@Override
	public @NotNull Iterator<ExpressionSite> iterator() {
		return expressionSites.iterator();
	}

}
