package ch.njol.skript.doc;

import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Represents anything that can be categorized.
 */
@FunctionalInterface
public interface Categorizable {

	/**
	 * Returns the documentation category which this module belongs to.
	 *
	 * @return The category of this module.
	 */
	@NotNull Set<Category> category();

}
