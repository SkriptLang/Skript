package ch.njol.skript.lang;

import com.google.common.base.Preconditions;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.StringJoiner;

/**
 * Utility class to build syntax strings, primarily intended for use
 * in {@link Debuggable#toString(Event, boolean)} implementations.
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
	 * Adds an object to the string.
	 * @param object The object to add.
	 */
	public void append(Object object) {
		Preconditions.checkNotNull(object);
		joiner.add(object.toString());
	}

	/**
	 * Adds a {@link Debuggable} object to the string, which is usually an expression or literal.
	 * @param debuggable The {@link Debuggable} to add.
	 */
	public void append(@NotNull Debuggable debuggable) {
		Preconditions.checkNotNull(debuggable);
		joiner.add(debuggable.toString(event, debug));
	}

	/**
	 * Adds multiple objects to the string.
	 * @param objects The objects to add.
	 */
	public void append(@NotNull Object... objects) {
		for (Object object : objects) {
			append(object);
		}
	}

	@Override
	public String toString() {
		return joiner.toString();
	}

}
