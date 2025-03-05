package ch.njol.skript.lang;

import org.skriptlang.skript.lang.experiment.Experiment;
import org.skriptlang.skript.lang.experiment.ExperimentSet;

/**
 * A syntax element that requires an experimental feature to be enabled.
 */
public interface ExperimentalSyntax extends SyntaxElement {


	/**
	 * Checks whether the required experiments are enabled for this syntax element.
	 *
	 * @param experimentSet An {@link Experiment} instance containing currently active experiments in the environment.
	 * @return {@code true} if the element can be used.
	 */
	boolean isSatisfiedBy(ExperimentSet experimentSet);

}
