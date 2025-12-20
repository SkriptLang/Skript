package ch.njol.skript.examples;

import java.util.Collection;

/**
 * Provides example scripts for installation.
 */
@FunctionalInterface
public interface ExampleScriptProvider {

	/**
	 * Returns the example scripts supplied by this provider.
	 *
	 * @return a collection of example scripts
	 */
	Collection<ExampleScript> scripts();

}
