package org.skriptlang.skript.lang.parsing.sites;

import org.skriptlang.skript.lang.parsing.constraints.Constraints;

/**
 * An immutable parsing site representing a single statement slot.
 */
public final class StatementSite implements ParsingSite {

	private static final Constraints CONSTRAINTS = Constraints.of();

	private final boolean optional;

	/**
	 * Creates a new non-optional statement site.
	 */
	public StatementSite() {
		this(false);
	}

	/**
	 * Creates a new statement site.
	 * @param optional Whether this site is optional.
	 */
	public StatementSite(boolean optional) {
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
