package ch.njol.skript.doc;

import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Represents anything that can be categorized.
 */
@FunctionalInterface
public interface Categorizable {

	/**
	 * Returns the documentation categories which this module belongs to.
	 *
	 * @return The categories of this module.
	 */
	@NotNull Set<Category> categories();

}
