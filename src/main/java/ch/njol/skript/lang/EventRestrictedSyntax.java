package ch.njol.skript.lang;

import ch.njol.util.Kleenean;
import org.bukkit.event.Event;

public interface EventRestrictedSyntax {

	/**
	 * Returns all supported events for this syntax element. By default, all events are accepted.
	 * <p>
	 * Before {@link SyntaxElement#init(Expression[], int, Kleenean, SkriptParser.ParseResult)} is called, checks
	 * to see if the current event is supported by this syntax element.
	 * If it is not, an error will be printed and the syntax element will not be initialised.
	 * </p>
	 *
	 * @return All supported event classes.
	 * @see ch.njol.util.coll.CollectionUtils#array(Object[])
	 */
	default Class<? extends Event>[] supportedEvents() {
		//noinspection unchecked
		return (Class<? extends Event>[]) new Class[0];
	}

}
