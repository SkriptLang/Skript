package org.skriptlang.skript.lang.parsing.sites;

import org.skriptlang.skript.lang.parsing.constraints.Constraints;

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
