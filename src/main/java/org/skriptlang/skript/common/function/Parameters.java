package org.skriptlang.skript.common.function;

import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;

/**
 * Holding class for a parameters of a function.
 */
public final class Parameters {

	private final int maxCount;
	private int minCount = 0;
	private final SequencedMap<String, Parameter<?>> named;
	private final Parameter<?>[] indexed;

	public Parameters(SequencedMap<String, Parameter<?>> parameters) {
		this.named = parameters;
		this.maxCount = parameters.size();

		this.indexed = new Parameter[this.maxCount];
		{
			int i = 0;
			for (Parameter<?> parameter : parameters.values()) {
				this.indexed[i] = parameter;
				i++;
			}
		}

		int j = size() - 1;
		for (Parameter<?> parameter : Lists.reverse(new LinkedList<>(parameters.values()))) {
			if (!parameter.hasModifier(Parameter.Modifier.OPTIONAL)) {
				this.minCount = j + 1;
				break;
			}
			j--;
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
		return maxCount;
	}

	/**
	 * @return The most amount of parameters this function supports.
	 */
	public int maxCount() {
		return maxCount;
	}

	/**
	 * @return The least amount of parameters this function supports.
	 */
	public int minCount() {
		return minCount;
	}

	/**
	 * @return A copy of the backing sequenced map.
	 */
	public @UnmodifiableView SequencedMap<String, Parameter<?>> sequencedMap() {
		return Collections.unmodifiableSequencedMap(named);
	}

}
