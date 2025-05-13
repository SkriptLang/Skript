package org.skriptlang.skript.lang.experiment;

import ch.njol.skript.lang.SyntaxElement;

/**
 * A {@link SyntaxElement} that requires a {@link Experiment}s to be enabled and/or disabled to be used.
 */
public interface ExperimentalSyntax extends SyntaxElement {


	/**
	 * @deprecated Use {@link #getExperimentData()} instead.
	 * Checks whether the required experiments are enabled for this syntax element.
	 *
	 * @param experimentSet An {@link Experiment} instance containing currently active experiments in the environment.
	 * @return {@code true} if the element can be used.
	 */
	@Deprecated(since = "INSERT VERSION", forRemoval = true)
	default boolean isSatisfiedBy(ExperimentSet experimentSet) {
		ExperimentData experimentData = getExperimentData();
		assert experimentData != null;
		return experimentData.checkRequirements(experimentSet);
	};

	/**
	 * Get the {@link ExperimentData} required for this {@link SyntaxElement}.
	 */
	default ExperimentData getExperimentData() {
		return null;
	};

}
