package org.skriptlang.skript.bukkit.itemcomponents.equippable;

import ch.njol.skript.lang.SyntaxElement;
import ch.njol.skript.registrations.Feature;
import org.skriptlang.skript.lang.experiment.ExperimentData;
import org.skriptlang.skript.lang.experiment.SimpleExperimentalSyntax;

/**
 * Typed {@link SimpleExperimentalSyntax} for {@link SyntaxElement}s that require {@link Feature#EQUIPPABLE_COMPONENTS}.
 */
public interface EquippableExperimentSyntax extends SimpleExperimentalSyntax {

	ExperimentData EXPERIMENT_DATA = ExperimentData.createSingularData(Feature.EQUIPPABLE_COMPONENTS);

	@Override
	default ExperimentData getExperimentData() {
		return EXPERIMENT_DATA;
	}

}
