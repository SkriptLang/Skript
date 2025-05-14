package org.skriptlang.skript.lang.experiment;

import ch.njol.skript.lang.SyntaxElement;

/**
 * A {@link SyntaxElement} expressing the usage of {@link Experiment}s.
 */
public interface ExperimentalSyntax extends SyntaxElement {

	/**
	 * Allows full access to the current {@link ExperimentSet} for manual checking of requirements.
	 * @param experimentSet An {@link ExperimentSet} containing the data of all enabled {@link Experiment}s.
	 * @return {@code true} if this {@link SyntaxElement} can be used.
	 */
	boolean isSatisfiedBy(ExperimentSet experimentSet);

}
