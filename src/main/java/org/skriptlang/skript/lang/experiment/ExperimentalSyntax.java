package org.skriptlang.skript.lang.experiment;

import ch.njol.skript.lang.SyntaxElement;

/**
 * A {@link SyntaxElement} that requires an {@link Experiment} to be enabled and/or disabled.
 */
public interface ExperimentalSyntax extends SyntaxElement {

	/**
	 * Checks whether the specific {@link Experiment}s are enabled and/or disabled for this {@link SyntaxElement}.
	 * @param experimentSet An {@link ExperimentSet} instance containing currently active {@link Experiment}s in the environment.
	 * @return {@code true} if this {@link SyntaxElement} can be used.
	 */
	boolean isSatisfiedBy(ExperimentSet experimentSet);

}
