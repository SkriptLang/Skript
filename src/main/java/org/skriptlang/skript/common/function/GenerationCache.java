package org.skriptlang.skript.common.function;

import ch.njol.skript.lang.function.FunctionRegistry;
import com.google.common.base.Preconditions;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * A cache which discards its entries whenever the registered functions change.
 * Can be used to memoize things which are expensive to work out from the function registry, such as
 * which overload a name resolves to or which signature a set of arguments binds to.
 *
 * @param <K> The key type.
 * @param <V> The cached value type.
 */
@ApiStatus.Internal
public final class GenerationCache<K, V> {

	private final int capacity;

	private final Map<K, Entry<V>> entries = new ConcurrentHashMap<>();

	/**
	 * The generation {@link #entries} was last emptied at.
	 */
	private volatile long generation = Long.MIN_VALUE;

	/**
	 * A cached value together with the generation it was worked out against.
	 *
	 * @param value      The cached value.
	 * @param generation The {@link FunctionRegistry#generation()} it was worked out against.
	 */
	private record Entry<V>(V value, long generation) {

	}

	private GenerationCache(int capacity) {
		this.capacity = capacity;
	}

	/**
	 * @param capacity How many entries to hold at most. Must be positive.
	 * @return A cache which stops taking new entries once it holds {@code capacity} of them.
	 */
	public static <K, V> @NotNull GenerationCache<K, V> bounded(int capacity) {
		Preconditions.checkArgument(capacity > 0, "capacity must be positive");
		return new GenerationCache<>(capacity);
	}

	/**
	 * Returns the value cached for {@code key}, checking if it is not cached or was worked
	 * out against overloads which have since changed.
	 * <p>
	 * A {@code compute} which returns null caches nothing to avoid caching failed function resolutions.
	 * We always want to recompute a failed resolution, since the overloads may have changed and it may now succeed.
	 * </p>
	 *
	 * @param key     The key.
	 * @param compute How to work out the value, called only if it is not already cached.
	 * @return The value, or null if {@code compute} returned null.
	 */
	public @Nullable V get(@NotNull K key, @NotNull Function<? super K, ? extends V> compute) {
		// read before the value is evaluated, so that another change landing during evaluation
		// doesn't cause the value to be cached under the new generation.
		// if the generation is different when we cache the value, it will be discarded and recomputed next time.
		long current = FunctionRegistry.getRegistry().generation();
		discardStale(current);

		Entry<V> existing = entries.get(key);
		if (existing != null && existing.generation() == current) {
			return existing.value();
		}

		// just compute the value if we are at capacity, since we don't want to evict an existing entry
		if (existing == null && entries.size() >= capacity) {
			return compute.apply(key);
		}

		// update the entry in the cache. If another thread has already updated it, we will get the present value back and return that instead.
		// if the generation changes during .apply, it will be discarded next time and recomputed, so we don't need to worry about that here.
		Entry<V> updated = entries.compute(key, (ignored, present) -> {
			if (present != null && present.generation() == current) {
				return present; // another thread refreshed it first
			}

			V computed = compute.apply(key);
			return computed != null ? new Entry<>(computed, current) : null;
		});

		return updated != null ? updated.value() : null;
	}

	/**
	 * Empties the cache if the registered functions have changed since it was last emptied.
	 *
	 * @param current The generation the registry is at.
	 */
	private void discardStale(long current) {
		if (generation == current) {
			return;
		}

		entries.clear();
		generation = current;
	}

}
