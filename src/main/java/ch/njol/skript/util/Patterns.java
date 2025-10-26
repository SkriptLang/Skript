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
			patterns[i] = (String) info[i][0];
			types[i] = info[i][1];
			matchedPatterns.computeIfAbsent(info[i][1], list -> new ArrayList<>()).add(i);
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
	public T getInfo(int matchedPattern) {
		Object object = types[matchedPattern];
		if (object == null)
			return null;
		//noinspection unchecked
		return (T) object;
	}

	/**
	 * Retrieves all pattern indices that are associated with {@code type}.
	 * <p>
	 *     These indices represent the positions of matched patterns registered
	 *     for the provided typed object.
	 * </p>
	 *
	 * @param type The typed object to look up.
	 * @return An array of matched pattern indices, or {@code null} if no patterns are registered for the given type.
	 */
	public Integer @Nullable [] getMatchedPatterns(@Nullable T type) {
		if (matchedPatterns.containsKey(type))
			return matchedPatterns.get(type).toArray(Integer[]::new);
		return null;
	}

	/**
	 * Retrieves the index of a specific matched pattern for the give {@code type}, based on the provided {@code arrayIndex}.
	 * <p>
	 *     This method looks up all matched pattern indices for the specified type and returns the index at {@code arrayIndex},
	 *     if it exists.
	 * </p>
	 *
	 * @param type The object whose registered patterns should be retrieved.
	 * @param arrayIndex The position in the matched pattern array to retrieve/
	 * @return An {@link Optional} containing the matched pattern index at the specified position, 
	 *			or {@link Optional#empty()} if no patterns are registered for {@code type}
	 *			or if the array does not contain enough elements.
	 * @see #getMatchedPatterns(Object) 	
	 */
	public Optional<Integer> getMatchedPattern(@Nullable T type, int arrayIndex) {
		Integer[] patternIndices = getMatchedPatterns(type);
		if (patternIndices == null || patternIndices.length < arrayIndex + 1)
			return Optional.empty();
		return Optional.of(patternIndices[arrayIndex]);
	}
	
}
