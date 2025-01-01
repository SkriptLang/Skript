package org.skriptlang.skript.log.runtime;

import ch.njol.skript.config.Node;
import ch.njol.skript.doc.Name;
import ch.njol.skript.lang.SyntaxElement;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A more versatile set of information about the source of an error.
 * Aims to avoid relying specifically on {@link Node}s for information.
 *
 * @param syntaxType A string representing the type of syntax. See {@link SyntaxElement#getSyntaxTypeName()}.
 * @param syntaxName The name of the syntax emitting the error.
 * @param lineNumber The line number of the syntax emitting the error.
 * @param lineText The raw code of the line.
 * @param script The name of the script in which the line exists.
 */
public record ErrorSource(
	String syntaxType,
	String syntaxName,
	int lineNumber,
	String lineText,
	String script
) {

	/**
	 * Creates an error source using information from a given node and syntax element.
	 * @param node The node to use for line, line number, and script name.
	 * @param element The element to use for the type and name.
	 * @return A new error source.
	 */
	public static @NotNull ErrorSource fromNodeAndElement(@Nullable Node node, @NotNull SyntaxElement element) {
		String elementName = element.getClass().getAnnotation(Name.class).value().trim().replaceAll("\n", "");
		if (node == null) {
			return new ErrorSource(element.getSyntaxTypeName(), elementName, 0, "-unknown-", "-unknown-");
		}
		String code = node.save().trim();
		return new ErrorSource(element.getSyntaxTypeName(), elementName, node.getLine(), code, node.getConfig().getFileName());
	}

	/**
	 * @return The code location (line number and script name) of the source of the error.
	 *         Used for hash maps.
	 */
	@Contract(" -> new")
	public @NotNull Location location() {
		return new Location(script, lineNumber);
	}

	/**
	 * A code location (line number and script name).
	 * Used for hash maps.
	 */
	public record Location(String script, int lineNumber) { }

}
