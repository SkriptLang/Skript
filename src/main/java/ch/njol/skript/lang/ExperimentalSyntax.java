package ch.njol.skript.lang;

import ch.njol.skript.Skript;
import ch.njol.util.Kleenean;
import org.skriptlang.skript.lang.experiment.Experiment;
import org.skriptlang.skript.lang.experiment.ExperimentSet;

/**
 * A syntax element that requires an experimental feature to be enabled.
 * When implementing this interface, it is required to override {@link #requiredExperiment()} or {@link #isSatisfiedBy(ExperimentSet)}
 */
public interface ExperimentalSyntax extends SyntaxElement {

	/**
	 * Returns the experiment {@link Experiment} required for this syntax to be used.
	 * <p>
	 * Before {@link SyntaxElement#init(Expression[], int, Kleenean, SkriptParser.ParseResult)} is called, checks
	 * to see if the experiment required has been enabled.
	 * If it is not, an error will be printed and the syntax element will not be initialised.
	 * </p>
	 *
	 * @return The required experiment.
	 */
	default Experiment requiredExperiment() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Checks whether the required experiments are enabled for this syntax element.
	 * <p>
	 * By default, this method checks for the required {@link Experiment} defined by {@link #requiredExperiment()}
	 * in the provided array of experiments. If the required experiment is found, the method returns {@code true}.
	 * </p>
	 * <p>
	 * Implementations may override this method to perform custom checks for enabled or disabled experiments
	 * and declare specific requirements for syntax availability.
	 * When overriding this method, it is recommended to use {@link Skript#error(String)} to inform users
	 * why the syntax element cannot be used.
	 * </p>
	 *
	 * @param experimentSet An {@link Experiment} instance containing currently active experiments in the environment.
	 * @return {@code true} if the element can be used.
	 */
	default boolean isSatisfiedBy(ExperimentSet experimentSet) {
		Experiment required = requiredExperiment();
		for (Experiment experiment : experimentSet)
			if (experiment.equals(required))
				return true;
		Skript.error("This syntax element is experimental. To enable this experiment, add "
			+ "'using " + required.codeName() + "' at the top of this file.");
		return false;
	}

}
