package ch.njol.skript.util;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxElement;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A helper class useful when an expression/condition/effect/etc. needs to associate additional data with each pattern.
 */
public class Patterns<T> {
	
	private final String[] patterns;
	private final Object[] types;
	private final Map<Object, List<Integer>> matchedPatterns = new HashMap<>();

	/**
	 * Creates a new {@link Patterns} with a provided {@link Object[][]} in the form of
	 * <code>
	 *     {{String, T}, {String, T}, ...}
	 *     {{pattern, correlating object}}
	 * </code>
	 */
	public Patterns(Object[][] info) {
		patterns = new String[info.length];
		types = new Object[info.length];
		for (int i = 0; i < info.length; i++) {
			if (info[i].length != 2 || !(info[i][0] instanceof String))
				throw new IllegalArgumentException("given array is not like {{String, T}, {String, T}, ...}");
			Optional<?> optional = Optional.ofNullable(info[i][1]);
			patterns[i] = (String) info[i][0];
			types[i] = optional;
			matchedPatterns.computeIfAbsent(optional, list -> new ArrayList<>()).add(i);
		}
	}

	/**
	 * Returns an array of the registered patterns.
	 * @return An {@link java.lang.reflect.Array} of {@link String}s.
	 */
	public String[] getPatterns() {
		return patterns;
	}
	
	/**
	 * Returns the typed object {@link T} correlating to {@code matchedPattern}.
	 *
	 * @param matchedPattern The pattern to get the data to as given in {@link SyntaxElement#init(Expression[], int, Kleenean, ParseResult)}
	 * @return The info associated with the matched pattern
	 * @throws ClassCastException If the item in the source array is not of the requested type
	 */
	public @Nullable T getInfo(int matchedPattern) {
		Optional<?> optional = (Optional<?>) types[matchedPattern];
		Object object = optional.orElse(null);
		if (object == null)
			return null;
		//noinspection unchecked
		return (T) object;
	}

	/**
	 * Gets all pattern indices that correlate to {@code type}.
	 *
	 * @param type The typed object.
	 * @return An array of pattern indices.
	 */
	public Integer @Nullable [] getMatchedPatterns(@Nullable T type) {
		Optional<?> optional = Optional.ofNullable(type);
		if (matchedPatterns.containsKey(optional))
			return matchedPatterns.get(optional).toArray(Integer[]::new);
		return null;
	}

	/**
	 * Gets an {@link java.lang.reflect.Array} of all patterns
	 * 		registered to {@code type} and the index of the pattern using {@code placement}.
	 * If there are no patterns registered to {@code type} or the array size is less than {@code placement}
	 * 		returns {@code 0}.
	 *
	 * @param type The typed object to grab all patterns registered to.
	 * @param placement The placement of the array for the index of the pattern.
	 * @return The index of the pattern or {@code 0} if no registered patterns for {@code type}
	 * 			or array size is less than {@code placement}.
	 */
	public int getMatchedPattern(@Nullable T type, int placement) {
		return getMatchedPattern(type, placement, 0);
	}

	/**
	 * Gets an {@link java.lang.reflect.Array} of all patterns
	 * 		registered to {@code type} and the index of the pattern using {@code placement}.
	 * If there are no patterns registered to {@code type} or the array size is less than {@code placement}
	 * 		returns {@code fallback}.
	 *
	 * @param type The typed object to grab all patterns registered to.
	 * @param placement The placement of the array for the index of the pattern.
	 * @param fallback The value to fall back to.
	 * @return The index of the pattern or {@code null} if no registered patterns for {@code type}
	 * 			or array size is less than {@code placement}.
	 */
	public int getMatchedPattern(@Nullable T type, int placement, int fallback) {
		Integer[] placements = getMatchedPatterns(type);
		if (placements == null || placements.length < placement + 1)
			return fallback;
		return placements[placement];
	}
	
}
