package org.skriptlang.skript.lang.experiment;

import ch.njol.skript.Skript;

/**
 * Something that can have experimental features enabled for.
 * The only intended implementation of this is the {@link org.skriptlang.skript.lang.script.Script},
 * however it is left open for configuration files, etc. that may use this functionality in the future.
 */
@FunctionalInterface
public interface Experimented {

	/**
	 * @param experiment The experimental feature to test.
	 * @return Whether this uses the given feature.
	 */
	boolean hasExperiment(Experiment experiment);

	/**
	 * @param featureName The name of the experimental feature to test.
	 * @return Whether this has a feature with the given name.
	 */
	default boolean hasExperiment(String featureName) {
		return Skript.experiments().find(featureName).isKnown();
	}

}
