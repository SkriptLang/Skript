package org.skriptlang.skript.lang.experiment;

import ch.njol.skript.lang.SyntaxElement;

/**
 * A {@link SyntaxElement} that requires one or more {@link Experiment}s to be enabled and/or disabled.
 * @see ExperimentData
 */
public interface ExperimentalSyntax extends SyntaxElement {

	/**
	 * Checks whether the active {@link Experiment}s satisfy the requirements of this syntax.
	 * @param experimentSet An {@link ExperimentSet} instance containing currently active {@link Experiment}s in the environment.
	 * @return {@code true} if this {@link SyntaxElement} can be used.
	 */
	boolean isSatisfiedBy(ExperimentSet experimentSet);

}
