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
	 * @return A string naming the type of syntax this is. e.g. "expression", "section".
	 */
	@Contract(pure = true)
	@NotNull String getSyntaxTypeName();

	/**
	 * Whether this syntax element consumes annotations.
	 * Consuming means the annotations are discarded for the following syntax.
	 * <p>
	 * <b>Most syntax should leave this as 'true'.</b>
	 * <p>
	 * If the return value is true (as expected), annotations placed before this element WILL NOT be available to
	 * the lines (or statements) following it.
	 *
	 * <pre>{@code
	 * 	on event:
	 *      @annotation
	 * 		my effect # can see @annotation
	 * 		my effect # cannot see @annotation
	 * }</pre>
	 *
	 * If the return value is false, annotations placed before this element WILL be available to
	 * the lines (or statements) following it.
	 *
	 * <pre>{@code
	 * 	on event:
	 *      @annotation
	 * 		my effect # can see @annotation
	 * 		my effect # can see @annotation
	 * }</pre>
	 *
	 * This behaviour is used by meta-syntax (including @annotations themselves).
	 *
	 * @return True if annotations will be discarded, false if they should be kept for the next statement.
	 * @see org.skriptlang.skript.lang.script.Annotation
	 */
	default boolean consumeAnnotations() {
		return true;
	}

}
