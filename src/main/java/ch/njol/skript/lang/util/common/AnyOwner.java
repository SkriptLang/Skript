package ch.njol.skript.lang.util.common;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

@FunctionalInterface
public interface AnyOwner<T> extends AnyProvider {

	/**
	 * @return The owner of this
	 */
	@UnknownNullability
	T getOwner();

	/**
	 * Sets the owner of this
	 * @param value The new owner to set
	 */
	default void setOwner(@Nullable T value) {
		throw new UnsupportedOperationException();
	}

	/**
	 * This is called before {@link #setOwner(T)}
	 * @return True if this supports changing owner, otherwise False
	 */
	default boolean supportsChangingOwner() {
		return false;
	}

	default String getOwnerType() {
		return null;
	}

	default Class<T> getReturnType() {
		return null;
	}

}
