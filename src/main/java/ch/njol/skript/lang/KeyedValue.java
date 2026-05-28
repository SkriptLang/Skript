package ch.njol.skript.lang;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

/**
 * A record that represents a key-value pair
 * @param <T> The type of the value associated with the key.
 */
public record KeyedValue<T>(@NotNull String key, @NotNull T value) implements Map.Entry<String, T> {

	public KeyedValue {
		Objects.requireNonNull(key, "key");
		Objects.requireNonNull(value, "value");
	}

	public KeyedValue(Map.Entry<String, T> entry) {
		this(entry.getKey(), entry.getValue());
	}

	@Override
	public String getKey() {
		return key();
	}

	@Override
	public T getValue() {
		return value();
	}

	@Override
	public T setValue(T value) {
		throw new UnsupportedOperationException("KeyedValue is immutable, cannot set value");
	}

	/**
	 * Creates a new {@link KeyedValue} with the same value but a different key.
	 *
	 * @param newKey the new key for the {@link KeyedValue}
	 * @return a new {@link KeyedValue} with the specified key and the same value
	 */
	public KeyedValue<T> withKey(@NotNull String newKey) {
		return new KeyedValue<>(newKey, value());
	}

	/**
	 * Creates a new {@link KeyedValue} with the same key but a different value.
	 *
	 * @param newValue the new value for the {@link KeyedValue}
	 * @param <U>      the type of the new value
	 * @return a new {@link KeyedValue} with the same key and the specified value
	 */
	public <U> KeyedValue<U> withValue(@NotNull U newValue) {
		return new KeyedValue<>(key(), newValue);
	}

	/**
	 * Maps an array of {@link KeyedValue} objects to a new array by applying a mapping function to each value.
	 * <p>
	 * For each non-null element in the source array, the provided mapper function is applied to its value.
	 * If the result of the mapping is non-null, a new {@link KeyedValue} is created with the same key and the mapped value.
	 * If the mapping result is null, the corresponding element in the result array will be null.
	 * <p>
	 * Null elements in the source array are skipped and remain null in the result array.
	 *
	 * @param source the source array of {@link KeyedValue} objects to map; may be null
	 * @param mapper a function to apply to each value in the source array
	 * @return a new array of {@link KeyedValue} objects with mapped values; never null, but may contain null elements
	 */
	public static <T, U> KeyedValue<U>[] map(KeyedValue<T>[] source, Function<T, @Nullable U> mapper) {
		if (source == null)
			//noinspection unchecked
			return new KeyedValue[0];
		//noinspection unchecked
		KeyedValue<U>[] mapped = new KeyedValue[source.length];
		for (int i = 0; i < source.length; i++) {
			if (source[i] == null)
				continue;
			U mappedValue = mapper.apply(source[i].value());
			mapped[i] = mappedValue != null ? source[i].withValue(mappedValue) : null;
		}
		return mapped;
	}

	/**
	 * Zips the given values and keys into a {@link KeyedValue} array.
	 *
	 * @param values the values to zip
	 * @param keys   the keys to zip with the values, or null to use numerical indices (1, 2, 3, ..., n)
	 * @param <T>    the type of the values
	 * @return an array of {@link KeyedValue}s
	 * @throws IllegalArgumentException if the keys are present and the lengths of values and keys do not match
	 */
	public static <T> KeyedValue<T> @NotNull [] zip(@NotNull T @NotNull [] values, @NotNull String @Nullable [] keys) {
		if (keys == null) {
			//noinspection unchecked
			KeyedValue<T>[] keyedValues = new KeyedValue[values.length];
			for (int i = 0; i < values.length; i++)
				keyedValues[i] = new KeyedValue<>(String.valueOf(i + 1), values[i]);
			return keyedValues;
		}
		if (values.length != keys.length)
			throw new IllegalArgumentException("Values and keys must have the same length");
		//noinspection unchecked
		KeyedValue<T>[] keyedValues = new KeyedValue[values.length];
		for (int i = 0; i < values.length; i++)
			keyedValues[i] = new KeyedValue<>(keys[i], values[i]);
		return keyedValues;
	}

	/**
	 * Unzips an array of {@link KeyedValue}s into separate lists of keys and values.
	 *
	 * @param keyedValues An array of {@link KeyedValue}s to unzip.
	 * @param <T> The type of the values in the {@link KeyedValue}s.
	 * @return An {@link UnzippedKeyValues} object containing two lists: one for keys and one for values.
	 */
	public static <T> UnzippedKeyValues<T> unzip(@NotNull KeyedValue<T> @NotNull [] keyedValues) {
		List<String> keys = new ArrayList<>(keyedValues.length);
		List<T> values = new ArrayList<>(keyedValues.length);
		for (KeyedValue<T> keyedValue : keyedValues) {
			keys.add(keyedValue.key());
			values.add(keyedValue.value());
		}
		return new UnzippedKeyValues<>(keys, values);
	}

	/**
	 * Unzips an iterator of {@link KeyedValue}s into separate lists of keys and values.
	 *
	 * @param keyedValues An iterator of {@link KeyedValue}s to unzip.
	 * @param <T> The type of the values in the {@link KeyedValue}s.
	 * @return An {@link UnzippedKeyValues} object containing two lists: one for keys and one for values.
	 */
	public static <T> UnzippedKeyValues<T> unzip(Iterator<KeyedValue<T>> keyedValues) {
		List<String> keys = new ArrayList<>();
		List<T> values = new ArrayList<>();
		while (keyedValues.hasNext()) {
			KeyedValue<T> keyedValue = keyedValues.next();
			keys.add(keyedValue.key());
			values.add(keyedValue.value());
		}
		return new UnzippedKeyValues<>(keys, values);
	}

	/**
	 * A record that represents a pair of lists: one for keys and one for values.
	 * This is used to store the result of unzipping {@link KeyedValue}s into separate lists.
	 * <br>
	 * Both lists are guaranteed to be of the same length, and each key corresponds to the value at the same index.
	 * <br>
	 * If the keys are not provided, numerical indices (1, 2, 3, ..., n) are used as keys.
	 * @param <T> The type of the values in the list.
	 * @param keys A list of keys extracted from the {@link KeyedValue}s.
	 * @param values A list of values extracted from the {@link KeyedValue}s.
	 * @see KeyedValue#unzip(KeyedValue[])
	 * @see #unzip(Iterator)
	 */
	public record UnzippedKeyValues<T>(@NotNull List<@NotNull String> keys, @NotNull List<@NotNull T> values) {

		public UnzippedKeyValues(@Nullable List<@NotNull String> keys, @NotNull List<@NotNull T> values) {
			this.values = Objects.requireNonNull(values, "values");
			this.keys = keys != null ? keys : new ArrayList<>(values.size());
			if (keys == null) {
				// If keys are null, we assume numerical indices (1, 2, 3, ..., n)
				for (int i = 1; i <= values.size(); i++)
					this.keys.add(String.valueOf(i));
			} else if (keys.size() != values.size()) {
				throw new IllegalArgumentException("Keys and values must have the same length");
			}
		}

		public UnzippedKeyValues(@NotNull String @Nullable [] keys, @NotNull T @NotNull [] values) {
			this(keys != null ? Arrays.asList(keys) : null, Arrays.asList(values));
		}

	}

}
