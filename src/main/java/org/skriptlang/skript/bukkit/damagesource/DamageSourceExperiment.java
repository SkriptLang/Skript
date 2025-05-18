package org.skriptlang.skript.bukkit.damagesource;

import ch.njol.skript.registrations.Feature;
import org.skriptlang.skript.lang.experiment.ExperimentSet;
import org.skriptlang.skript.lang.experiment.ExperimentalSyntax;

/**
 * A {@link ExperimentalSyntax} for using {@link Feature#DAMAGE_SOURCE}
 */
public interface DamageSourceExperiment extends ExperimentalSyntax {

	// TODO: Change this to implement `SimpleExperimentalSyntax` when PR containing is merged

	@Override
	default boolean isSatisfiedBy(ExperimentSet experimentSet) {
		return experimentSet.hasExperiment(Feature.DAMAGE_SOURCE);
	};

}
