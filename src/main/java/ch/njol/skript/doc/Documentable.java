package ch.njol.skript.doc;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.skriptlang.skript.docs.Documentation;

import java.util.List;

/**
 * Represents any object that can be documented using methods.
 * @deprecated Use {@link org.skriptlang.skript.docs.DocumentationDocumentable} instead.
 */
@Deprecated(forRemoval = true, since = "INSERT VERSION")
public interface Documentable {

	private Documentation asDocumentation() {
		if (this instanceof org.skriptlang.skript.docs.DocumentationDocumentable documentable) {
			return documentable.documentation();
		}
		throw new IllegalStateException("Missing override of Documentable method");
	}

	/**
	 * @return The name.
	 */
	default @NotNull String name() {
		return asDocumentation().name();
	}

	/**
	 * @return The unmodifiable description.
	 */
	default @Unmodifiable @NotNull List<String> description() {
		return List.of(asDocumentation().description().split("\n"));
	}

	/**
	 * @return The unmodifiable version history.
	 */
	default @Unmodifiable @NotNull List<String> since() {
		return List.copyOf(asDocumentation().since());
	}

	/**
	 * @return The unmodifiable examples.
	 */
	default @Unmodifiable @NotNull List<String> examples() {
		return List.copyOf(asDocumentation().examples());
	}

	/**
	 * @return The unmodifiable keywords.
	 */
	default @Unmodifiable @NotNull List<String> keywords() {
		return List.copyOf(asDocumentation().keywords());
	}

	/**
	 * @return The unmodifiable requirements.
	 */
	default @Unmodifiable @NotNull List<String> requires() {
		return List.copyOf(asDocumentation().requirements());
	}

}
