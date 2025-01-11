package ch.njol.skript.timings;

import ch.njol.skript.config.Node;

/**
 * Represents an element that can be timed by {@link Timings}
 */
public interface Timeable {

	Node getNode();

}
