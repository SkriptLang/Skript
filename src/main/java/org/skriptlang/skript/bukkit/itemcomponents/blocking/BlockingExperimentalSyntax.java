package org.skriptlang.skript.bukkit.itemcomponents.blocking;

import ch.njol.skript.lang.SyntaxElement;
import ch.njol.skript.registrations.Feature;
import org.skriptlang.skript.lang.experiment.ExperimentData;
import org.skriptlang.skript.lang.experiment.SimpleExperimentalSyntax;

/**
 * Typed {@link SimpleExperimentalSyntax} for {@link SyntaxElement}s that require {@link Feature#BLOCKING_COMPONENTS}.
 */
public interface BlockingExperimentalSyntax extends SimpleExperimentalSyntax {

	ExperimentData EXPERIMENT_DATA = ExperimentData.createSingularData(Feature.BLOCKING_COMPONENTS);

	@Override
	default ExperimentData getExperimentData() {
		return EXPERIMENT_DATA;
	}

}
