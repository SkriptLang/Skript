package org.skriptlang.skript.lang.parsing.constraints;

import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxElement;
import org.skriptlang.skript.lang.experiment.ExperimentSet;
import org.skriptlang.skript.lang.experiment.ExperimentalSyntax;
import org.skriptlang.skript.lang.parsing.ParsingContext;
import org.skriptlang.skript.registration.SyntaxInfo;

/**
 * A constraint that ensures a syntax element is only used when the required experiments are enabled.
 */
public class ExperimentConstraint implements Constraint {

	@Override
	public boolean acceptsPreInit(SyntaxInfo<?> info, SyntaxElement element, ParseResult parseResult, ParsingContext context) {
		if (!(element instanceof ExperimentalSyntax experimentalSyntax))
			return true;
		ExperimentSet experiments = context.getExperimentSet();
		if (experiments == null)
			return true;
		return experimentalSyntax.isSatisfiedBy(experiments);
	}

	@Override
	public Lifetime lifetime() {
		return Lifetime.PERMANENT;
	}
}
