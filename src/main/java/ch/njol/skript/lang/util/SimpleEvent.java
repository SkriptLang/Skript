package ch.njol.skript.lang.util;

import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

/**
 * A very basic SkriptEvent which returns true for all events (i.e. all registered events).
 * 
 * @author Peter Güttinger
 */
public class SimpleEvent extends SkriptEvent {

	public SimpleEvent() {}

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
		return "simple event";
	}

}
