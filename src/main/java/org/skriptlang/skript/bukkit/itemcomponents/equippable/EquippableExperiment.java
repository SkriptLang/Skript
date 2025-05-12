package org.skriptlang.skript.bukkit.itemcomponents.equippable;

import ch.njol.skript.registrations.Feature;
import org.skriptlang.skript.lang.experiment.ExperimentSet;
import org.skriptlang.skript.lang.experiment.ExperimentalSyntax;

public interface EquippableExperiment extends ExperimentalSyntax {

	@Override
	default boolean isSatisfiedBy(ExperimentSet experimentSet) {
		return experimentSet.hasExperiment(Feature.EQUIPPABLE_COMPONENTS);
	}

}
