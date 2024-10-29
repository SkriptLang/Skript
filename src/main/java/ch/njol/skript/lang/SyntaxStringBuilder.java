package ch.njol.skript.lang;

import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.StringJoiner;

/**
 * Utility class to build syntax strings, used in {@link Debuggable#toString(Event, boolean)}.
 */
public class SyntaxStringBuilder {

	private final boolean debug;
	private final @Nullable Event event;
	private final StringJoiner joiner = new StringJoiner(" ");

	/**
	 * Creates a new SyntaxStringBuilder.
	 *
	 * @param event The event to get information from. This is always null if debug == false.
	 * @param debug If true this should print more information, if false this should print what is shown to the end user
	 */
	public SyntaxStringBuilder(@Nullable Event event, boolean debug) {
		this.event = event;
		this.debug = debug;
	}

	/**
	 * Adds a boolean to the string.
	 * @param b The boolean to add.
	 * @return This SyntaxStringBuilder.
	 */
	public SyntaxStringBuilder add(boolean b) {
		joiner.add(Boolean.toString(b));
		return this;
	}

	/**
	 * Adds an integer to the string.
	 * @param i The integer to add.
	 * @return This SyntaxStringBuilder.
	 */
	public SyntaxStringBuilder add(int i) {
		joiner.add(Integer.toString(i));
		return this;
	}

	/**
	 * Adds a long to the string.
	 * @param l The long to add.
	 * @return This SyntaxStringBuilder.
	 */
	public SyntaxStringBuilder add(long l) {
		joiner.add(Long.toString(l));
		return this;
	}

	/**
	 * Adds a double to the string.
	 * @param d The double to add.
	 * @return This SyntaxStringBuilder.
	 */
	public SyntaxStringBuilder add(double d) {
		joiner.add(Double.toString(d));
		return this;
	}

	/**
	 * Adds a float to the string.
	 * @param f The float to add.
	 * @return This SyntaxStringBuilder.
	 */
	public SyntaxStringBuilder add(float f) {
		joiner.add(Float.toString(f));
		return this;
	}

	/**
	 * Adds a char to the string.
	 * @param c The char to add.
	 * @return This SyntaxStringBuilder.
	 */
	public SyntaxStringBuilder add(char c) {
		joiner.add(Character.toString(c));
		return this;
	}

	/**
	 * Adds a string to the string.
	 * @param string The string to add.
	 * @return This SyntaxStringBuilder.
	 */
	public SyntaxStringBuilder add(@NotNull String string) {
		joiner.add(string);
		return this;
	}

	/**
	 * Adds a {@link Debuggable} object to the string, which is usually an expression or literal.
	 * @param debuggable The {@link Debuggable} to add.
	 * @return This SyntaxStringBuilder.
	 */
	public SyntaxStringBuilder add(@NotNull Debuggable debuggable) {
		joiner.add(debuggable.toString(event, debug));
		return this;
	}

	@Override
	public String toString() {
		return joiner.toString();
	}
}
