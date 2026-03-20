package org.skriptlang.skript.lang.parsing.constraints;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxElement;
import ch.njol.util.Kleenean;
import org.skriptlang.skript.lang.parsing.ParsingContext;
import org.skriptlang.skript.registration.SyntaxInfo;

public interface Constraint {

	/**
	 * Limits parsing to infos which pass this test. This will be called prior to any attempts to parse against the info,
	 * so filtering here is most effectively for performance. However, this method may be called thousands of times over
	 * a parse, so keep the cost of the check minimal.
	 * <br>
	 * May print errors.
	 * @param info The info to check.
	 * @param context The current parsing context.
	 * @return Whether this info could feasibly fit this site.
	 */
	default boolean acceptsInfo(SyntaxInfo<?> info, ParsingContext context) {
		return true;
	}

	/**
	 * Limits parsing to elements which pass this test. Elements are checked against this method immediately after
	 * construction, prior to {@link SyntaxElement#init(Expression[], int, Kleenean, ParseResult)}.
	 * The element will NOT be initialized. This should be used for checks which only require the element instance
	 * or are heavier info-based checks. Returning false will reject the element
	 * and continue parsing without initializing the element.
	 * <br>
	 * Note that simplification has not yet taken place.
	 * <br>
	 * May print errors.
	 *
	 * @param info The info used to create the element.
	 * @param element The parsed but un-initialized element to check.
	 * @param parseResult The parse result from parsing this element.
	 * @param context The current parsing context.
	 * @return Whether the element/info fits this site.
	 */
	default boolean acceptsPreInit(SyntaxInfo<?> info, SyntaxElement element, ParseResult parseResult, ParsingContext context) {
		return true;
	}

	/**
	 * Limits parsing to elements which pass this test. If an element successfully passes init(), it will then be
	 * checked against this method. The element will be fully initialized and simplified. This should be used for checks which require
	 * more information about the element and for checks which are much heavier. Returning false will reject the element
	 * and continue parsing.
	 * <br>
	 * May print errors.
	 * @param element The parsed and initialized element to check.
	 * @param parseResult The parse result from parsing this element.
	 * @param context The current parsing context.
	 * @return Whether the element fits this site.
	 */
	default boolean acceptsPostInit(SyntaxElement element, ParseResult parseResult, ParsingContext context) {
		return true;
	}

	/**
	 * This determines whether this constraint is inherited by child syntax elements.
	 * Constraints can apply to just the current syntax element being parsed, Lifetime.TEMPORARY,
	 * or to all child syntax elements, Lifetime.PERMANENT.
	 * @return The lifetime of this constraint.
	 */
	Lifetime lifetime();

	enum Lifetime {
		/**
		 * The constraint only applies to the current syntax element being parsed.
		 * The constraint will be discarded for child syntax elements.
		 */
		TEMPORARY,
		/**
		 * The constraint applies to the current syntax element being parsed
		 * and all child syntax elements.
		 */
		PERMANENT
	}

}
