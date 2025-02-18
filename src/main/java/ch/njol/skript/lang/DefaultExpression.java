package ch.njol.skript.lang;

/**
 * Represents an expression that can be used as the default value of a certain type or event.
 */
public interface DefaultExpression<T> extends Expression<T> {

	/**
	 * Called when an expression is initialized.
	 *
	 * @return Whether the expression is valid in its context. Skript will error if false.
	 */
	boolean init();

	/**
	 * @return Usually true, though this is not required, as e.g. SimpleLiteral implements DefaultExpression but is usually not the default of an event.
	 */
	@Override
	boolean isDefault();

}
