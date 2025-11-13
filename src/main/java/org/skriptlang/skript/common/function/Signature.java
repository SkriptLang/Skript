package org.skriptlang.skript.common.function;

import ch.njol.skript.doc.Documentable;
import ch.njol.skript.util.Contract;
import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.ApiStatus.NonExtendable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.SequencedMap;

/**
 * Represents a function signature.
 *
 * <h2>This interface should only be extended by {@link ch.njol.skript.lang.function.Signature}.</h2>
 * <p>It will contain methods when Signature has been properly reworked.</p>
 */
@NonExtendable
@Internal
@Experimental
public interface Signature<T> extends Documentable {

	/**
	 * @return The type of this parameter.
	 */
	Class<T> returnType();

	/**
	 * @return An unmodifiable view of all the parameters that this signature has.
	 */
	@Unmodifiable @NotNull SequencedMap<String, Parameter<?>> parameters();

	@Experimental
	Contract contract();

	@Experimental
	void addCall(FunctionReference<?> reference);

	/**
	 * @return Whether this signature returns single values.
	 */
	default boolean single() {
		if (returnType() == null) {
			return false;
		}
		return !returnType().isArray();
	}

}
