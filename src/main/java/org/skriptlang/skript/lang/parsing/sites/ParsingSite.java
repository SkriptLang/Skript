package org.skriptlang.skript.lang.parsing.sites;

import org.skriptlang.skript.lang.parsing.constraints.Constraints;

/**
 * Represents a location in a syntax pattern where a specific type of element is expected to be parsed.
 * Each site carries the {@link Constraints} that the parsed element must satisfy,
 * and whether the element is optional.
 */
public interface ParsingSite {

	/**
	 * @return the list of constraints applicable to this parsing site.
	 */
	Constraints constraints();

	/**
	 * @return whether this parsing site is optional (i.e. the element may be absent).
	 */
	boolean isOptional();

}
