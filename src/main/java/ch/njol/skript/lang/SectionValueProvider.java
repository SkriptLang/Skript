package ch.njol.skript.lang;

import ch.njol.skript.expressions.ExprSectionExpression;

/**
 * Interface required to use {@link SectionEvent} and {@link SectionValueExpression}.
 */
public interface SectionValueProvider {

	/**
	 * Get the expression to be used for {@link ExprSectionExpression}.
	 */
	public Expression<?> getSectionValue();

}
