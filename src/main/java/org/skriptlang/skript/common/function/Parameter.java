package org.skriptlang.skript.common.function;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.skriptlang.skript.common.function.DefaultFunction.Builder;
import org.skriptlang.skript.lang.converter.Converter;
import org.skriptlang.skript.lang.converter.Converters;

import java.util.Set;

/**
 * Represents a function parameter.
 *
 * @param <T> The type of the function parameter.
 */
public interface Parameter<T> {

	/**
	 * @return The name of this parameter.
	 */
	@NotNull String name();

	/**
	 * @return The type of this parameter.
	 */
	@NotNull Class<T> type();

	/**
	 * @return All modifiers belonging to this parameter.
	 */
	@Unmodifiable @NotNull Set<Modifier> modifiers();

	/**
	 * Returns whether this parameter has the specified modifier.
	 * @param modifier The modifier.
	 * @return True when {@link #modifiers()} contains the specified modifier, false if not.
	 */
	default boolean hasModifier(Modifier modifier) {
		return modifiers().contains(modifier);
	}

	/**
	 * Gets a modifier of the specified type if present.
	 * @param modifierClass The class of the modifier to retrieve
	 * @return The modifier instance, or null if not present
	 */
	default <M extends Modifier> M getModifier(Class<M> modifierClass) {
		return modifiers().stream()
			.filter(modifierClass::isInstance)
			.map(modifierClass::cast)
			.findFirst()
			.orElse(null);
	}

	/**
	 * @return Whether this parameter is for single values.
	 */
	default boolean single() {
		return !type().isArray();
	}

	/**
	 * Represents a modifier that can be applied to a parameter
	 * when constructing one using {@link Builder#parameter(String, Class, Modifier[])}}.
	 */
	interface Modifier {

		/**
		 * @return A new Modifier instance to be used as a custom flag.
		 */
		static Modifier of() {
			return new Modifier() { };
		}

		/**
		 * The modifier for parameters that are optional.
		 */
		Modifier OPTIONAL = of();

		/**
		 * The modifier for parameters that support optional keyed expressions.
		 *
		 * @see ch.njol.skript.lang.KeyProviderExpression
		 * @see ch.njol.skript.lang.KeyReceiverExpression
		 */
		Modifier KEYED = of();

		/**
		 * A modifier to use for checking if a parameter is ranged.
		 * Do NOT use for declaring a parameter to be ranged, use {@link Modifier#ranged(Comparable, Comparable)}
		 * Accessing the min and the max values can be done via {@link Parameter#getModifier(Class)}.
		 */
		Modifier RANGED = new RangedModifier<>(0,0); // 0 and 0 are just dummy values.

		/**
		 * Creates a range modifier with inclusive min and max bounds.
		 */
		static <T extends Comparable<T>> RangedModifier<T> ranged(T min, T max) {
			return new RangedModifier<>(min, max);
		}

		/**
		 * Modifier specifying valid range bounds for numeric parameters.
		 * Note that ALL instances will have the same hashCode and will be equal to {@link Modifier#RANGED}.
		 * Avoid comparing these objects or putting multiple into a HashSet or HashMap!
		 */
		class RangedModifier<T extends Comparable<T>> implements Modifier {
			private final T min;
			private final T max;

			/**
			 * Inclusive range between min and max
			 * @param min min value
			 * @param max max value
			 */
			private RangedModifier(T min, T max) {
				Preconditions.checkState(min.compareTo(max) < 1, "Min value cannot be greater than max value!");
				this.min = min;
				this.max = max;
			}

			/**
			 * @return Min value of the range (inclusive)
			 */
			public T getMin() {
				return min;
			}

			/**
			 * @return Max value of the range (inclusive)
			 */
			public T getMax() {
				return max;
			}

			/**
			 * @param input The value to test.
			 * @return Whether input is between min and max.
			 */
			@SuppressWarnings("unchecked")
			public boolean inRange(Object input) {
				// convert to right type
				if (!min.getClass().isInstance(input)) {
					Converter<Object, ?> converter = (Converter<Object, ?>) Converters.getConverter(input.getClass(), min.getClass());
					if (converter == null)
						return false;
					input = converter.convert(input);
					if (input == null)
						return false;
				}
				// compare
				return ((T) input).compareTo(min) > -1 && ((T) input).compareTo(max) < 1;
			}

			/**
			 * @param inputs The values to test.
			 * @return Whether all the inputs are between min and max.
			 */
			public boolean inRange(Object @NotNull [] inputs) {
				if (inputs.length == 0)
					return false;
				for (Object input : inputs) {
					if (!inRange(input))
						return false;
				}
				return true;
			}

			@Override
			public boolean equals(Object obj) {
				// equal to the RANGED singleton for hasModifier checks
				return obj == Modifier.RANGED || ((obj instanceof RangedModifier<?> range) && (this == Modifier.RANGED || range.max == this.max && range.min  == this.min));
			}

			@Override
			public int hashCode() {
				return 439824729; // all should be equal
			}

			@Override
			public String toString() {
				return "RangedModifier(min=" + min + ", max=" + max + ")";
			}

		}
	}

}
