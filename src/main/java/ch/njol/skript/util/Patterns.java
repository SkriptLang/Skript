package ch.njol.skript.util;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxElement;
import ch.njol.util.Kleenean;

/**
 * A helper class useful when an expression/condition/effect/etc. needs to associate additional data with each pattern.
 * 
 * @author Peter Güttinger
 */
public class Patterns<T> {
	
	private final String[] patterns;
	private final Object[] ts;
	
	/**
	 * @param info An array which must be like {{String, T}, {String, T}, ...}
	 */
	public Patterns(final Object[][] info) {
		patterns = new String[info.length];
		ts = new Object[info.length];
		for (int i = 0; i < info.length; i++) {
			if (info[i].length != 2 || !(info[i][0] instanceof String) || info[i][1] == null)
				throw new IllegalArgumentException("given array is not like {{String, T}, {String, T}, ...}");
			patterns[i] = (String) info[i][0];
			ts[i] = info[i][1];
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
	@SuppressWarnings({"unchecked", "null"})
	public T getInfo(final int matchedPattern) {
		return (T) ts[matchedPattern];
	}
	
}
