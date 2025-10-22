package ch.njol.skript.lang.util.common;

import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript.common.properties.expressions.PropExprAmount;

/**
 * A provider for anything with a (number) amount/size.
 * Anything implementing this (or convertible to this) can be used by the {@link PropExprAmount}
 * property expression.
 *
 * @see AnyProvider
 * @deprecated Use {@link org.skriptlang.skript.lang.properties.Property#AMOUNT} instead.
 */
@FunctionalInterface
@Deprecated(since="2.13", forRemoval = true)
public interface AnyAmount extends AnyProvider {

	/**
	 * @return This thing's amount/size
	 */
	@NotNull Number amount();

	/**
	 * This is called before {@link #setAmount(Number)}.
	 * If the result is false, setting the name will never be attempted.
	 *
	 * @return Whether this supports being set
	 */
	default boolean supportsAmountChange() {
		return false;
	}

	/**
	 * The behaviour for changing this thing's name, if possible.
	 * If not possible, then {@link #supportsAmountChange()} should return false and this
	 * may throw an error.
	 *
	 * @param amount The name to change
	 * @throws UnsupportedOperationException If this is impossible
	 */
	default void setAmount(Number amount) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	/**
	 * @return Whether the amount of this is zero, i.e. empty
	 */
	default boolean isEmpty() {
		return this.amount().intValue() == 0;
	}

}
