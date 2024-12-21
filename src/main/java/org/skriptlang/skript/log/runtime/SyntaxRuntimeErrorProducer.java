package org.skriptlang.skript.log.runtime;

import ch.njol.skript.config.Node;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SyntaxElement;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.NotNull;

/**
 * A runtime error producer intended for use with {@link SyntaxElement}s. Uses {@link Node}s to determine source.
 */
public interface SyntaxRuntimeErrorProducer extends RuntimeErrorProducer {

	/**
	 * Returns the source {@link Node} for any errors the implementing class emits.
	 * <br>
	 * Used for accessing the line contents via {@link Node#getKey()}
	 * and the line number via {@link Node#getLine()}.
	 * <br>
	 * A standard implementation is to store the Node during
	 * {@link SyntaxElement#init(Expression[], int, Kleenean, SkriptParser.ParseResult)}
	 * via {@link ParserInstance#getNode()}.
	 * @return The source that produced a runtime error.
	 */
	Node getNode();

	@Override
	default @NotNull ErrorSource getErrorSource() {
		return ErrorSource.fromNodeAndElement(getNode(), (SyntaxElement) this);
	}

}
