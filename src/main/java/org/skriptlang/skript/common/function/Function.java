package org.skriptlang.skript.common.function;

import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.ApiStatus.NonExtendable;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a function implementation.
 *
 * <h2>This interface should only be extended by {@link DefaultFunction} and {@link ch.njol.skript.lang.function.Function}</h2>
 * <p>It will contain methods when Function has been properly reworked.</p>
 */
@NonExtendable
@Internal
@Experimental
public interface Function<T> {

	/**
	 * @return The signature belonging to this function.
	 */
	@NotNull Signature<T> signature();

}
