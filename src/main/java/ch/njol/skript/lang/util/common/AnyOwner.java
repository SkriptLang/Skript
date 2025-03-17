package ch.njol.skript.lang.util.common;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

@FunctionalInterface
public interface AnyOwner<F> extends AnyProvider {

	/**
	 * @return The owner of this
	 */
	@UnknownNullability F getOwner();

	/**
	 * Sets the owner of this
	 * @param value The new owner to set
	 */
	default void setOwner(@Nullable F value) {
		throw new UnsupportedOperationException();
	}

	/**
	 * This is called before {@link #setOwner(F)}
	 * @return True if this supports changing owner, otherwise False
	 */
	default boolean supportsChangingOwner() {
		return false;
	}

	default boolean supportsChangeValue(Class<?> classy) {
		return false;
	}

}
