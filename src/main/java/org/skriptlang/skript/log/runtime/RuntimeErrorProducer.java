package org.skriptlang.skript.log.runtime;

import ch.njol.skript.Skript;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

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
			new RuntimeError(Level.SEVERE, getErrorSource(), message, null)
		);
	}

	/**
	 * Dispatches a runtime error with the given text and syntax highlighting.
	 * Metadata will be provided along with the message, including line number, the docs name of the producer,
	 * and the line content.
	 * <br>
	 * Implementations should ensure they call super() to print the error.
	 *
	 * @param message The text to display as the error message.
	 * @param highlight The text to highlight in the parsed syntax.
	 */
	default void error(String message, String highlight) {
		getRuntimeErrorManager().error(
			new RuntimeError(Level.SEVERE, getErrorSource(), message, highlight)
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
			new RuntimeError(Level.WARNING, getErrorSource(), message, null)
		);
	}

	/**
	 * Dispatches a runtime warning with the given text and syntax highlighting.
	 * Metadata will be provided along with the message, including line number, the docs name of the producer,
	 * and the line content.
	 * <br>
	 * Implementations should ensure they call super() to print the warning.
	 *
	 * @param message The text to display as the error message.
	 * @param highlight The text to highlight in the parsed syntax.
	 */
	default void warning(String message, String highlight) {
		getRuntimeErrorManager().error(
			new RuntimeError(Level.WARNING, getErrorSource(), message, highlight)
		);
	}

	/**
	 * @return The manager this producer will send errors to.
	 */
	default RuntimeErrorManager getRuntimeErrorManager() {
		return Skript.getRuntimeErrorManager();
	}

}
