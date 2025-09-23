package ch.njol.skript.doc;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.List;

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
	@Unmodifiable @NotNull List<String> description();

	/**
	 * @return The unmodifiable version history.
	 */
	@Unmodifiable @NotNull List<String> since();

	/**
	 * @return The unmodifiable examples.
	 */
	@Unmodifiable @NotNull List<String> examples();

	/**
	 * @return The unmodifiable keywords.
	 */
	@Unmodifiable @NotNull List<String> keywords();

	/**
	 * @return The unmodifiable requirements.
	 */
	@Unmodifiable @NotNull List<String> requires();

}
