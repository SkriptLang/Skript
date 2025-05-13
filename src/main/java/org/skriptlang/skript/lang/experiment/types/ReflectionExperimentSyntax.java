package org.skriptlang.skript.lang.experiment.types;

import ch.njol.skript.lang.SyntaxElement;
import ch.njol.skript.registrations.Feature;
import org.skriptlang.skript.lang.experiment.Experiment;
import org.skriptlang.skript.lang.experiment.ExperimentData;

/**
 * Typed {@link SingularExperimentSyntax} for {@link SyntaxElement}s that require {@link Feature#SCRIPT_REFLECTION}.
 */
public interface ReflectionExperimentSyntax extends SingularExperimentSyntax {

	ExperimentData EXPERIMENT_DATA = ExperimentData.builder().required(Feature.SCRIPT_REFLECTION).build();

	@Override
	default Experiment getExperiment() {
		return EXPERIMENT_DATA.getRequired().iterator().next();
	};

	@Override
	default ExperimentData getExperimentData() {
		return EXPERIMENT_DATA;
	}

}
