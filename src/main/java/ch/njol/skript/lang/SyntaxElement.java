package ch.njol.skript.lang;

import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;

/**
 * Represents a general part of the syntax.
 */
public interface SyntaxElement {

	/**
	 * Called just after the constructor.
	 * 
	 * @param expressions all %expr%s included in the matching pattern in the order they appear in the pattern. If an optional value was left out, it will still be included in this list
	 *            holding the default value of the desired type, which usually depends on the event.
	 * @param matchedPattern The index of the pattern which matched
	 * @param isDelayed Whether this expression is used after a delay or not (i.e. if the event has already passed when this expression will be called)
	 * @param parseResult Additional information about the match.
	 * @return Whether this expression was initialised successfully. An error should be printed prior to returning false to specify the cause.
	 * @see ParserInstance#isCurrentEvent(Class...)
	 */
	boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult);

	/**
	 * @see ParserInstance#get()
	 */
	default ParserInstance getParser() {
		return ParserInstance.get();
	}

	/**
	 * Returns all supported events for this syntax element. By default, all events are accepted.
	 * <p>
	 * Before {@link #init(Expression[], int, Kleenean, ParseResult)} is called, checks
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
