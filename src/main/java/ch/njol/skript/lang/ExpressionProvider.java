package ch.njol.skript.lang;

/**
 * Interface to determine the implementing {@link Class}es provides an {@link Expression}.
 */
public interface ExpressionProvider {

	/**
	 * @return The provided {@link Expression}.
	 */
	Expression<?> getProvidedExpression();

}
