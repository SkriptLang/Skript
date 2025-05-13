package org.skriptlang.skript.lang.experiment.types;

import ch.njol.skript.lang.SyntaxElement;
import org.skriptlang.skript.lang.experiment.Experiment;
import org.skriptlang.skript.lang.experiment.ExperimentData;
import org.skriptlang.skript.lang.experiment.ExperimentSet;
import org.skriptlang.skript.lang.experiment.ExperimentalSyntax;

/**
 * Interface for expressing a {@link SyntaxElement} is an {@link ExperimentalSyntax} and requires only one
 * {@link Experiment} to be enabled.
 */
public interface SingularExperimentSyntax extends ExperimentalSyntax {

	@Override
	default boolean isSatisfiedBy(ExperimentSet experimentSet) {
		return getExperimentData().checkRequirementsAndError(experimentSet);
	};

	/**
	 * Get the required {@link Experiment} for this {@link SyntaxElement}
	 */
	Experiment getExperiment();

	@Override
	default ExperimentData getExperimentData() {
		return ExperimentData.builder().required(getExperiment()).build();
	};

}
