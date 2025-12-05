package ch.njol.skript.doc;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Set;

/**
 * Represents anything that can be categorized.
 */
@FunctionalInterface
public interface Categorizable {

	/**
	 * Returns the documentation categories which this object belongs to.
	 *
	 * @return The categories of this object.
	 */
	@Unmodifiable @NotNull Set<Category> categories();

}
