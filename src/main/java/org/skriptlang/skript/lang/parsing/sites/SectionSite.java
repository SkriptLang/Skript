package org.skriptlang.skript.lang.parsing.sites;

import org.skriptlang.skript.lang.parsing.constraints.Constraints;

public class SectionSite extends AbstractParsingSite {

	private boolean optional;

	@Override
	protected Constraints buildConstraints() {
		return Constraints.of();
	}

	@Override
	public boolean isOptional() {
		return optional;
	}

	public SectionSite optional(boolean optional) {
		this.optional = optional;
		return this;
	}

}
