package ch.njol.skript.lang;

import com.google.common.base.Preconditions;
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
	 * Adds an object to the string.
	 * @param object The object to add.
	 * @return This SyntaxStringBuilder.
	 */
	public SyntaxStringBuilder append(Object object) {
		Preconditions.checkNotNull(object);
		joiner.add(object.toString());
		return this;
	}

	/**
	 * Adds a {@link Debuggable} object to the string, which is usually an expression or literal.
	 * @param debuggable The {@link Debuggable} to add.
	 * @return This SyntaxStringBuilder.
	 */
	public SyntaxStringBuilder append(@NotNull Debuggable debuggable) {
		Preconditions.checkNotNull(debuggable);
		joiner.add(debuggable.toString(event, debug));
		return this;
	}

	@Override
	public String toString() {
		return joiner.toString();
	}

}
