package org.skriptlang.skript.lang.experiment.types;

import ch.njol.skript.lang.SyntaxElement;
import ch.njol.skript.registrations.Feature;
import org.skriptlang.skript.lang.experiment.Experiment;
import org.skriptlang.skript.lang.experiment.ExperimentData;

/**
 * Typed {@link SingularExperimentSyntax} for {@link SyntaxElement}s that require {@link Feature#QUEUES}.
 */
public interface QueueExperimentSyntax extends SingularExperimentSyntax {

	ExperimentData EXPERIMENT_DATA = ExperimentData.builder().required(Feature.QUEUES).build();

	@Override
	default Experiment getExperiment() {
		return EXPERIMENT_DATA.getRequired().toArray(Experiment[]::new)[0];
	};

	@Override
	default ExperimentData getExperimentData() {
		return EXPERIMENT_DATA;
	}

}
