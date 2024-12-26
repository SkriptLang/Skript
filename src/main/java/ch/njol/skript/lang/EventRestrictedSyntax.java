package ch.njol.skript.lang;

import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;

/**
 * A syntax element that restricts the events it can be used in.
 */
public interface EventRestrictedSyntax {

	/**
	 * Returns all supported events for this syntax element.
	 * <p>
	 * Before {@link SyntaxElement#init(Expression[], int, Kleenean, SkriptParser.ParseResult)} is called, checks
	 * to see if the current event is supported by this syntax element.
	 * If it is not, an error will be printed and the syntax element will not be initialised.
	 * </p>
	 *
	 * @return All supported event classes.
	 * @see CollectionUtils#array(Object[])
	 */
	Class<? extends Event>[] supportedEvents();

}
