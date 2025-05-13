package org.skriptlang.skript.lang.experiment.types;

import ch.njol.skript.lang.SyntaxElement;
import ch.njol.skript.registrations.Feature;
import org.skriptlang.skript.lang.experiment.Experiment;

/**
 * Typed {@link SingularExperimental} for {@link SyntaxElement}s that require {@link Feature#QUEUES}.
 */
public interface QueueExperimental extends SingularExperimental {

	@Override
	default Experiment getExperiment() {
		return Feature.QUEUES;
	};

}
