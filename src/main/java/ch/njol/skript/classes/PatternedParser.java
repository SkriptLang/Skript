package ch.njol.skript.classes;

import ch.njol.util.StringUtils;

/**
 * A {@link Parser} that is based off of a set list of valid patterns to match.
 */
public abstract class PatternedParser<T> extends Parser<T> {

	/**
	 * Get all literal patterns.
	 */
	public abstract String[] getPatterns();

	/**
	 * Get a single {@link String} combining {@link #getPatterns()}.
	 */
	public String getCombinedPatterns() {
		String[] patterns = getPatterns();
		return StringUtils.join(patterns, ", ");
	}

}
