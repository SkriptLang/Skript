package ch.njol.skript.lang;

import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a general part of the syntax.
 */
public interface SyntaxElement {

	/**
	 * Called immediately after the constructor. This should be used to do any work that need to be done prior to
	 * downstream initialization. This is not intended to be used by syntaxes directly, but by parent classes to do
	 * work prior to the initialization of the child classes.
	 *
	 * @return Whether this expression was pre-initialised successfully.
	 * 			An error should be printed prior to returning false to specify the cause.
	 */
	default boolean preInit() {
		return true;
	}

	/**
	 * Called just after the constructor and {@link #preInit}.
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
	 * @return A string naming the type of syntax this is. e.g. "expression", "section".
	 */
	@Contract(pure = true)
	@NotNull String getSyntaxTypeName();

}
