package org.skriptlang.skript.bukkit.itemcomponents.consumable;

import ch.njol.skript.lang.SyntaxElement;
import ch.njol.skript.registrations.Feature;
import org.skriptlang.skript.lang.experiment.ExperimentData;
import org.skriptlang.skript.lang.experiment.SimpleExperimentalSyntax;

/**
 * Typed {@link SimpleExperimentalSyntax} for {@link SyntaxElement}s that require {@link Feature#CONSUME_EFFECTS}.
 */
public interface ConsumeEffectExperimentalSyntax extends SimpleExperimentalSyntax {

	ExperimentData EXPERIMENT_DATA = ExperimentData.createSingularData(Feature.CONSUME_EFFECTS);

	@Override
	default ExperimentData getExperimentData() {
		return EXPERIMENT_DATA;
	}

}
