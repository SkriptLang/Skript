package ch.njol.skript.lang;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.util.coll.iterator.ArrayIterator;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Represents an expression that is able to return a set of keys linked to its values.
 * This can be used to return index-linked values to store in a list variable,
 * using the {@link ChangeMode#SET} {@link Changer} or passed to a function argument.
 * An expression can provide a set of keys to use, rather than numerical indices.
 * <br/>
 * Index-linking is not (currently) used with other change modes.
 * <br/>
 * <br/>
 * <h2>Contract</h2>
 * <ul>
 *     <li>Neither {@link #getArrayKeys(Event)} nor {@link #getAllKeys(Event)} should ever be called without
 *     a corresponding {@link #getArray(Event)} or {@link #getAll(Event)} call.</li>
 *     <li>{@link #getArrayKeys(Event)} and {@link #getAllKeys(Event)} should only be called iff {@link #canReturnKeys()}
 *     returns {@code true}.</li>
 *     <li>A caller may ask only for values and does not have to invoke either {@link #getArrayKeys(Event)} or
 *     {@link #getAllKeys(Event)}.</li>
 *     <li>{@link #getArrayKeys(Event)} might be called after the corresponding {@link #getArray(Event)}</li>
 *     <li>{@link #getAllKeys(Event)} might be called after the corresponding {@link #getAll(Event)}</li>
 * </ul>
 * <br/>
 * <h2>Advice on Caching</h2>
 * As long as callers are built sensibly and follow API advice, it should be safe to cache a key-list during a values
 * call.
 * E.g. if an expression is returning data from a map, it could request the whole entry-set during
 * {@link #getArray(Event)}
 * and return the keys during {@link #getArrayKeys(Event)} (provided the cache is weak, safe and event-linked).
 * This is not necessary, but it may be helpful for some expressions where the set of keys could potentially change
 * between repeated calls, or is expensive to access.
 * <br/>
 * <br/>
 * <h3>Caveats</h3>
 * <ol>
 *     <li>The caller may <i>never</i> ask for {@link #getArrayKeys(Event)}.
 *     The cache should be disposed of in a timely manner.</li>
 *     <li>It is (theoretically) possible for two separate calls to occur simultaneously
 *     (asking for the value/key sets separately) so it is recommended to link any cache system to the event instance
 *     .</li>
 * </ol>
 * Note that the caller may <i>never</i> ask for {@link #getArrayKeys(Event)} and so the cache should be disposed of
 * in a timely manner.
 * <br/>
 * <pre>{@code
 * Map<Event, Collection<String>> cache = new WeakHashMap<>();
 *
 * public Object[] getArray(Event event) {
 *     Set<Entry<String, T>> entries = something.entrySet();
 *     cache.put(event, List.copyOf(something.keySet()));
 *     return something.values().toArray(...);
 * }
 *
 * public String[] getArrayKeys(Event event) {
 *     if (!cache.containsKey(event))
 *         throw new IllegalStateException();
 *     return cache.remove(event).toArray(new String[0]);
 *     // this should never be absent/null
 * }
 * }</pre>
 *
 * @see Expression
 * @see KeyReceiverExpression
 */
public interface KeyProviderExpression<T> extends Expression<T> {

	/**
	 * A set of keys, matching the length and order of the immediately-previous
	 * {@link #getArray(Event)} values array.
	 * <br/>
	 * This should <b>only</b> be called immediately after a {@link #getArray(Event)} invocation,
	 * and iff {@link #canReturnKeys()} returns {@code true}.
	 * If it is called without a matching values request (or after a delay) then the behaviour
	 * is undefined, in which case:
	 * <ul>
	 *     <li>the method may throw an error,</li>
	 *     <li>the method may return keys not matching any previous values,</li>
	 *     <li>or the method may return nothing at all.</li>
	 * </ul>
	 *
	 * @param event The event context
	 * @return A set of keys, of the same length as {@link #getArray(Event)}
	 * @throws IllegalStateException If this was not called directly after a {@link #getArray(Event)} call
	 * or if {@link #canReturnKeys()} returns {@code false}
	 */
	@NotNull String @NotNull [] getArrayKeys(Event event) throws IllegalStateException;

	/**
	 * A set of keys, matching the length and order of the immediately-previous
	 * {@link #getAll(Event)} values array.
	 * <br/>
	 * This should <b>only</b> be called immediately after a {@link #getAll(Event)} invocation,
	 * and iff {@link #canReturnKeys()} returns {@code true}.
	 * If it is called without a matching values request (or after a delay) then the behaviour
	 * is undefined, in which case:
	 * <ul>
	 *     <li>the method may throw an error,</li>
	 *     <li>the method may return keys not matching any previous values,</li>
	 *     <li>or the method may return nothing at all.</li>
	 * </ul>
	 *
	 * @param event The event context
	 * @return A set of keys, of the same length as {@link #getAll(Event)}
	 * @throws IllegalStateException If this was not called directly after a {@link #getAll(Event)} call
	 * or if {@link #canReturnKeys()} returns {@code false}
	 */
	default @NotNull String @NotNull [] getAllKeys(Event event) {
		return this.getArrayKeys(event);
	}

	/**
	 * Returns an iterator over the entries of this expression, where each entry is a key-value pair.
	 * <br/>
	 * This should <b>only</b> be called iff {@link #canReturnKeys()} returns {@code true}.
	 *
	 * @param event The event context
	 * @return An iterator over the key-value pairs of this expression
	 */
	default Iterator<Map.Entry<String, T>> keyedIterator(Event event) {
		return new ArrayIterator<>(zip(getArray(event), getArrayKeys(event)));
	}

	/**
	 * Keyed expressions should never be single.
	 */
	@Override
	default boolean isSingle() {
		return false;
	}

	/**
	 * Returns whether this expression can return keys.
	 * <br/>
	 * If this returns false, then {@link #getArrayKeys(Event)} and {@link #getAllKeys(Event)} should never be called.
	 *
	 * @return Whether this expression can return keys
	 */
	default boolean canReturnKeys() {
		return true;
	}

	/**
	 * While all keyed expressions may <i>offer</i> their keys,
	 * some may prefer that they are not used unless strictly required (e.g. variables).
	 *
	 * @return Whether the caller is recommended to ask for keys after asking for values
	 */
	default boolean areKeysRecommended() {
		return true;
	}

	@Override
	default boolean isLoopOf(String input) {
		return canReturnKeys() && isIndexLoop(input);
	}

	default boolean isIndexLoop(String input) {
		return input.equalsIgnoreCase("index");
	}

	/**
	 * Zips the given values and keys into a map entry array.
	 *
	 * @param values the values to zip
	 * @param keys the keys to zip with the values, or null to use numerical indices (1, 2, 3, ..., n)
	 * @return an array of map entries, where each entry is a key-value pair
	 * @param <T> the type of the values
	 * @throws IllegalArgumentException if the keys are present and the lengths of values and keys do not match
	 */
	static <T> Map.Entry<String, T> @NotNull [] zip(@NotNull T @NotNull [] values, @NotNull String @Nullable [] keys) {
		if (keys == null) {
			//noinspection unchecked
			Map.Entry<String, T>[] entries = new Map.Entry[values.length];
			for (int i = 0; i < values.length; i++)
				entries[i] = Map.entry(String.valueOf(i + 1), values[i]);
			return entries;
		}
		if (values.length != keys.length)
			throw new IllegalArgumentException("Values and keys must have the same length");
		//noinspection unchecked
		Map.Entry<String, T>[] entries = new Map.Entry[values.length];
		for (int i = 0; i < values.length; i++)
			entries[i] = Map.entry(keys[i], values[i]);
		return entries;
	}

	/**
	 * Unzips an array of map entries into separate lists of keys and values.
	 *
	 * @param entries An array of map entries to unzip.
	 * @param <T> The type of the values in the map entries.
	 * @return An {@link Unzipped} object containing two lists: one for keys and one for values.
	 */
	static <T> Unzipped<T> unzip(@NotNull Map.Entry<String, T> @NotNull [] entries) {
		List<String> keys = new ArrayList<>(entries.length);
		List<T> values = new ArrayList<>(entries.length);
		for (Map.Entry<String, T> entry : entries) {
			keys.add(entry.getKey());
			values.add(entry.getValue());
		}
		return new Unzipped<>(keys, values);
	}

	/**
	 * Unzips an iterator of map entries into separate lists of keys and values.
	 *
	 * @param entries An iterator of map entries to unzip.
	 * @param <T> The type of the values in the map entries.
	 * @return An {@link Unzipped} object containing two lists: one for keys and one for values.
	 */
	static <T> Unzipped<T> unzip(Iterator<Map.Entry<String, T>> entries) {
		List<String> keys = new ArrayList<>();
		List<T> values = new ArrayList<>();
		while (entries.hasNext()) {
			Map.Entry<String, T> entry = entries.next();
			keys.add(entry.getKey());
			values.add(entry.getValue());
		}
		return new Unzipped<>(keys, values);
	}

	/**
	 * Checks if the given expression can return keys.
	 *
	 * @param expression the expression to check
	 * @return true if the expression can return keys, false otherwise
	 * @see #canReturnKeys()
	 */
	static boolean canReturnKeys(Expression<?> expression) {
		return expression instanceof KeyProviderExpression<?> provider && provider.canReturnKeys();
	}

	/**
	 * Checks if the given expression can return keys and whether it is recommended to use them.
	 *
	 * @param expression the expression to check
	 * @return true if the expression can return keys, and it is recommended to use them, false otherwise
	 * @see #areKeysRecommended()
	 * @see #canReturnKeys()
	 */
	static boolean areKeysRecommended(Expression<?> expression) {
		return canReturnKeys(expression) && ((KeyProviderExpression<?>) expression).areKeysRecommended();
	}

	/**
	 * A record that represents a pair of lists: one for keys and one for values.
	 * This is used to store the result of unzipping map entries into separate lists.
	 * <br>
	 * Both lists are guaranteed to be of the same length, and each key corresponds to the value at the same index.
	 *
	 * @param <T> The type of the values in the list.
	 * @param keys A list of keys extracted from the map entries.
	 * @param values A list of values extracted from the map entries.
	 * @see #unzip(Map.Entry[])
	 * @see #unzip(Iterator)
	 */
	record Unzipped<T>(List<String> keys, List<T> values) {}

}
