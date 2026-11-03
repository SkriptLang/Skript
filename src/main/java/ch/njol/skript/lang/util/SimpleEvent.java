package ch.njol.skript.lang.util;

import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

/**
 * A very basic SkriptEvent which returns true for all events (i.e. all registered events).
 */
public class SimpleEvent extends SkriptEvent {

	private final String toString;

	/**
	 * Creates a new SimpleEvent with the default toString of 'simple event'.
	 * Prefer using {@link #SimpleEvent(String)} to provide a more descriptive toString.
	 */
	public SimpleEvent() {
		this("simple event");
	}

	/**
	 * Creates a new SimpleEvent with a custom toString.
	 *
	 * @param string the string to return in toString
	 */
	public SimpleEvent(String string) {
		this.toString = string;
	}

	@Override
	public boolean check(Event event) {
		return true;
	}

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parser) {
		if (args.length != 0)
			throw new SkriptAPIException("Invalid use of SimpleEvent");
		return true;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return toString;
	}

}
