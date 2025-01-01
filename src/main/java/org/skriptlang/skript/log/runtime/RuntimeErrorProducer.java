package org.skriptlang.skript.log.runtime;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SyntaxElement;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Level;

/**
 * A RuntimeErrorProducer can throw runtime errors in a standardized and controlled manner.
 */
public interface RuntimeErrorProducer {

	/**
	 * Gets the source of the errors produced by the implementing class.
	 * Most extending interfaces should provide a default implementation of this method for ease of use.
	 * @see SyntaxRuntimeErrorProducer
	 * @return The source of the error.
	 */
	@Contract(" -> new")
	@NotNull ErrorSource getErrorSource();

	/**
	 * Gets the text that should be underlined within the line contents. This should match the text the user wrote that
	 * was parsed as the syntax that threw the runtime issue. For example, if the skull expression in
	 * {@code give skull of player to all players} throws a runtime error, this method should return
	 * {@code "skull of player"}
	 * <br>
	 * An example implementation for {@link Expression}s is to store {@link SkriptParser.ParseResult#expr} during
	 * {@link SyntaxElement#init(Expression[], int, Kleenean, SkriptParser.ParseResult)} and return that.
	 * <br>
	 * For other syntax types, this may vary. Effects, for example, may underline the whole line.
	 *
	 * @return The text to underline in the line that produced a runtime error. This may be null if no highlighting
	 * 			is desired or possible.
	 */
	@Nullable String toHighlight();

	/**
	 * Dispatches a runtime error with the given text.
	 * Metadata will be provided along with the message, including line number, the docs name of the producer,
	 * and the line content.
	 * <br>
	 * Implementations should ensure they call super() to print the error.
	 *
	 * @param message The text to display as the error message.
	 */
	default void error(String message) {
		getRuntimeErrorManager().error(
			new RuntimeError(Level.SEVERE, getErrorSource(), message, toHighlight())
		);
	}

	/**
	 * Dispatches a runtime warning with the given text.
	 * Metadata will be provided along with the message, including line number, the docs name of the producer,
	 * and the line content.
	 * <br>
	 * Implementations should ensure they call super() to print the warning.
	 *
	 * @param message The text to display as the error message.
	 */
	default void warning(String message) {
		getRuntimeErrorManager().error(
			new RuntimeError(Level.WARNING, getErrorSource(), message, toHighlight())
		);
	}

	/**
	 * @return The manager this producer will send errors to.
	 */
	default RuntimeErrorManager getRuntimeErrorManager() {
		return Skript.getRuntimeErrorManager();
	}

}
