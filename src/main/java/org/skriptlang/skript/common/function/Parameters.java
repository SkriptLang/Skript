package org.skriptlang.skript.common.function;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Arrays;
import java.util.Collections;
import java.util.SequencedMap;

/**
 * Holding class for a parameters of a function.
 */
public final class Parameters {

	private final SequencedMap<String, Parameter<?>> named;
	private final Parameter<?>[] indexed;

	public Parameters(SequencedMap<String, Parameter<?>> parameters) {
		this.named = parameters;

		indexed = new Parameter[parameters.size()];
		int i = 0;
		for (Parameter<?> parameter : parameters.values()) {
			indexed[i] = parameter;
			i++;
		}
	}

	/**
	 * Gets a parameter by name.
	 *
	 * @param name The name.
	 * @return The parameter, if present.
	 */
	public Parameter<?> get(@NotNull String name) {
		return named.get(name);
	}

	/**
	 * @return The first parameter, if present.
	 */
	public Parameter<?> getFirst() {
		return named.firstEntry().getValue();
	}

	/**
	 * Gets a parameter by index.
	 *
	 * @param index The index.
	 * @return The parameter, if present.
	 */
	public Parameter<?> get(int index) {
		return indexed[index];
	}

	/**
	 * @return An array of all parameters.
	 */
	public Parameter<?>[] all() {
		return Arrays.copyOf(indexed, indexed.length);
	}

	/**
	 * @return The amount of parameters.
	 */
	public int size() {
		return indexed.length;
	}

	/**
	 * @return A copy of the backing sequenced map.
	 */
	public @UnmodifiableView SequencedMap<String, Parameter<?>> sequencedMap() {
		return Collections.unmodifiableSequencedMap(named);
	}

}
