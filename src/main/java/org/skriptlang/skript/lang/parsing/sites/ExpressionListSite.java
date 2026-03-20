package org.skriptlang.skript.lang.parsing.sites;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.skriptlang.skript.lang.parsing.constraints.Constraints;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class ExpressionListSite extends AbstractParsingSite implements Iterable<ExpressionSite> {

	private final List<ExpressionSite> expressionSites;
	private boolean optional;

	public ExpressionListSite(List<ExpressionSite> expressionSites) {
		this.expressionSites = expressionSites;
	}

	@Override
	protected Constraints buildConstraints() {
		return Constraints.of();
	}

	@Override
	public boolean isOptional() {
		return optional;
	}

	public ExpressionListSite optional(boolean optional) {
		this.optional = optional;
		return this;
	}

	public @Unmodifiable List<ExpressionSite> getExpressionSites() {
		return Collections.unmodifiableList(expressionSites);
	}

	@Override
	public @NotNull Iterator<ExpressionSite> iterator() {
		return expressionSites.iterator();
	}

}
