package ch.njol.skript.doc;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;

/**
 * Represents any object that can be documented using methods.
 */
public interface Documentable {

	/**
	 * @return The name.
	 */
	@NotNull String name();

	/**
	 * @return The unmodifiable description.
	 */
	@Unmodifiable @NotNull Collection<String> description();

	/**
	 * @return The unmodifiable version history.
	 */
	@Unmodifiable @NotNull Collection<String> since();

	/**
	 * @return The unmodifiable examples.
	 */
	@Unmodifiable @NotNull Collection<String> examples();

	/**
	 * @return The unmodifiable keywords.
	 */
	@Unmodifiable @NotNull Collection<String> keywords();

	/**
	 * @return The unmodifiable requirements.
	 */
	@Unmodifiable @NotNull Collection<String> requires();

}
