package ch.njol.skript.entity;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxElement;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntityPatterns<T> {

	private final String[] patterns;
	private final Object[] types;
	private final Map<Object, List<Integer>> matchedPatterns = new HashMap<>();

	/**
	 * Creates a new {@link EntityPatterns} with a provided {@link Object[][]} in the form of
	 * <code>
	 *     {{String, T}, {String, T}, ...}
	 *     {{pattern, correlating object}}
	 * </code>
	 */
	public EntityPatterns(Object[][] info) {
		patterns = new String[info.length];
		types = new Object[info.length];
		for (int i = 0; i < info.length; i++) {
			if (info[i].length != 2 || !(info[i][0] instanceof String))
				throw new IllegalArgumentException("given array is not like {{String, T}, {String, T}, ...}");
			Object object = info[i][1];
			if (object == null)
				object = EntityPatternNone.NONE;
			patterns[i] = (String) info[i][0];
			types[i] = object;
			matchedPatterns.computeIfAbsent(object, list -> new ArrayList<>()).add(i);
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
		Object object = types[matchedPattern];
		if (object == null || object == EntityPatternNone.NONE)
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
	public Integer[] getMatchedPatterns(@Nullable T type) {
		Object object = type;
		if (object == null)
			object = EntityPatternNone.NONE;
		return matchedPatterns.get(object).toArray(Integer[]::new);
	}

	private enum EntityPatternNone {
		NONE
	}

}
