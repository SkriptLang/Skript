package org.skriptlang.skript.lang.experiment;

import org.skriptlang.skript.lang.experiment.ExperimentData;
import org.skriptlang.skript.lang.experiment.ExperimentSet;
import org.skriptlang.skript.lang.experiment.ExperimentalSyntax;

/**
 * An {@link ExperimentalSyntax} utilizing {@link ExperimentData} to ensure the set requirements are met.
 */
public interface SimpleExperimentalSyntax extends ExperimentalSyntax {

	@Override
	default boolean isSatisfiedBy(ExperimentSet experimentSet) {
		return getExperimentData().checkRequirementsAndError(experimentSet);
	};

	/**
	 * The {@link ExperimentData} used to check that the current {@link ExperimentSet} meets the requirements.
	 */
	ExperimentData getExperimentData();

}
