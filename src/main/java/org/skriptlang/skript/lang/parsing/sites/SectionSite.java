package org.skriptlang.skript.lang.parsing.sites;

import org.skriptlang.skript.lang.parsing.constraints.Constraints;

/**
 * An immutable parsing site representing a section slot (a block of statements).
 */
public final class SectionSite implements ParsingSite {

	private static final Constraints CONSTRAINTS = Constraints.of();

	private final boolean optional;

	/**
	 * Creates a new non-optional section site.
	 */
	public SectionSite() {
		this(false);
	}

	/**
	 * Creates a new section site.
	 * @param optional Whether this site is optional.
	 */
	public SectionSite(boolean optional) {
		this.optional = optional;
	}

	@Override
	public Constraints constraints() {
		return CONSTRAINTS;
	}

	@Override
	public boolean isOptional() {
		return optional;
	}

}
