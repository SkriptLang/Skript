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
	private final Object[] ts;
	private final Map<Object, List<Integer>> matchedPatterns = new HashMap<>();

	/**
	 * @param info An array which must be like {{String, T}, {String, T}, ...}
	 */
	public EntityPatterns(Object[][] info) {
		patterns = new String[info.length];
		ts = new Object[info.length];
		for (int i = 0; i < info.length; i++) {
			if (info[i].length != 2 || !(info[i][0] instanceof String))
				throw new IllegalArgumentException("given array is not like {{String, T}, {String, T}, ...}");
			Object object = info[i][1];
			if (object == null)
				object = EntityPatternNone.NONE;
			patterns[i] = (String) info[i][0];
			ts[i] = object;
			matchedPatterns.computeIfAbsent(object, list -> new ArrayList<>()).add(i);
		}
	}

	public String[] getPatterns() {
		return patterns;
	}

	/**
	 * @param matchedPattern The pattern to get the data to as given in {@link SyntaxElement#init(Expression[], int, Kleenean, ParseResult)}
	 * @return The info associated with the matched pattern
	 * @throws ClassCastException If the item in the source array is not of the requested type
	 */
	public @Nullable T getInfo(int matchedPattern) {
		Object object = ts[matchedPattern];
		if (object == null || object == EntityPatternNone.NONE)
			return null;
		//noinspection unchecked
		return (T) object;
	}

	/**
	 * Gets all pattern indices that correlate to {@code t}.
	 * @param t The typed object.
	 * @return An array of pattern indices.
	 */
	public Integer[] getMatchedPatterns(@Nullable T t) {
		Object object = t;
		if (object == null)
			object = EntityPatternNone.NONE;
		return matchedPatterns.get(object).toArray(Integer[]::new);
	}

	private enum EntityPatternNone {
		NONE
	}

}
