package org.skriptlang.skript.lang.experiment.types;

import ch.njol.skript.lang.SyntaxElement;
import ch.njol.skript.registrations.Feature;
import org.skriptlang.skript.lang.experiment.Experiment;
import org.skriptlang.skript.lang.experiment.ExperimentData;

/**
 * Typed {@link SingularExperimentSyntax} for {@link SyntaxElement}s that require {@link Feature#QUEUES}.
 */
public interface QueueExperimental extends SingularExperimentSyntax {

	ExperimentData EXPERIMENT_DATA = new ExperimentData().required(Feature.QUEUES);

	@Override
	default Experiment getExperiment() {
		return Feature.QUEUES;
	};

	@Override
	default ExperimentData getExperimentData() {
		return EXPERIMENT_DATA;
	}

}
