package org.skriptlang.skript.lang.experiment;

import ch.njol.skript.lang.SyntaxElement;

/**
 * A {@link SyntaxElement} that requires {@link Experiment}s to be enabled and/or disabled to be used.
 * When implementing this interface, should be overriding {@link #isSatisfiedBy(ExperimentSet)} or {@link #getExperimentData()}.
 */
public interface ExperimentalSyntax extends SyntaxElement {

	/**
	 * Checks whether the required experiments are enabled for this syntax element.
	 *
	 * @param experimentSet An {@link Experiment} instance containing currently active experiments in the environment.
	 * @return {@code true} if the element can be used.
	 */
	boolean isSatisfiedBy(ExperimentSet experimentSet);

	/**
	 * Get the {@link ExperimentData} required for this {@link SyntaxElement}.
	 */
	default ExperimentData getExperimentData() {
		return new ExperimentData();
	};

}
