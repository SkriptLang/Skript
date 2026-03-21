package org.skriptlang.skript.lang.parsing.parsers;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxElement;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.Skript;
import org.skriptlang.skript.lang.parsing.constraints.Constraints;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry.Key;

import java.util.Iterator;

/**
 * A parser with configurable constraints and context for parsing Skript syntax elements.
 * Implementations should use {@link SyntaxParserImpl} as a base class, which provides default implementations for this interface.
 * <p>
 * This is generally not intended to be used directly for parsing; instead, specialized parsers such as {@link ExpressionParser}
 * or {@link StatementParser} should be used for specific parsing tasks. This parser serves as a common foundation
 * for those specialized parsers, though it can also be used directly for general parsing if needed.
 * <p>
 * Implementing interfaces should provide static factory methods similar to those in this interface, allowing parsers
 * to transition between different parser types while retaining configuration.
 *
 * @param <P> the type of the parser subclass
 * @see SyntaxParserImpl
 * @see ExpressionParser
 */
public interface SyntaxParser<P extends SyntaxParser<P>> {

	@Contract("_ -> new")
	static @NotNull SyntaxParser<?> from(Skript skript) {
		return new SyntaxParserImpl<>(skript);
	}

	@Contract("_ -> new")
	static @NotNull SyntaxParser<?> from(SyntaxParser<?> other) {
		return new SyntaxParserImpl<>(other);
	}

	/**
	 * Attempts to parse this parser's input against the given syntaxes.
	 * Prints parse errors (i.e. must start a ParseLog before calling this method)
	 * {@link #parse(Key)} is preferred for parsing against a specific syntax.
	 * <p>
	 * Implementations should throw an exception if the parser is not ready to parse.
	 *
	 * @param candidateSyntaxes The iterator of {@link SyntaxInfo} objects to parse against.
	 * @return A parsed {@link SyntaxElement} with its {@link SyntaxElement#init(Expression[], int, Kleenean, ParseResult)}
	 * 			method having been run and returned true. If no successful parse can be made, null is returned.
	 * @param <E> The type of {@link SyntaxElement} that will be returned.
	 */
	@Contract(pure = true)
	<E extends SyntaxElement> @Nullable E parse(@NotNull Iterator<? extends SyntaxInfo<? extends E>> candidateSyntaxes);

	/**
	 * Attempts to parse this parser's input against the given syntax type.
	 * Prints parse errors (i.e. must start a ParseLog before calling this method).
	 *
	 * @param expectedTypeKey A {@link Key} that determines what
	 *                           kind of syntax is expected as a result of the parsing.
	 * @return A parsed {@link SyntaxElement} with its {@link SyntaxElement#init(Expression[], int, Kleenean, ParseResult)}
	 * 			method having been run and returned true. If no successful parse can be made, null is returned.
	 * @param <E> The type of {@link SyntaxElement} that will be returned.
	 */
	@Contract(pure = true)
	<E extends SyntaxElement, I extends SyntaxInfo<? extends E>> @Nullable E parse(Key<I> expectedTypeKey);

	/**
	 * @return the input string to be parsed
	 */
	@Contract(pure = true)
	String input();

	/**
	 * Sets the input string to be parsed.
	 * @param input the new input string
	 * @return this parser instance
	 */
	@Contract("_ -> this")
	P input(@NotNull String input);

	/**
	 * @return the current parsing constraints this parser imposes. Does not include constraints from earlier parses.
	 */
	@Contract(pure = true)
	Constraints constraints();

	/**
	 * Sets the parsing constraints that this parser imposes. Only applies to this parser and its children.
	 * @param constraints the new parsing constraints
	 * @return this parser instance
	 */
	@Contract("_ -> this")
	P constraints(@NotNull Constraints constraints);

	/**
	 * @return the current parse context
	 */
	@Contract(pure = true)
	ParseContext parseContext();

	/**
	 * Sets the parse context.
	 * @param context the new parse context
	 * @return this parser instance
	 */
	@Contract("_ -> this")
	P parseContext(@NotNull ParseContext context);

	/**
	 * @return the default error message to use if parsing fails
	 */
	@Contract(pure = true)
	@Nullable String defaultError();

	/**
	 * Sets the default error message to use if parsing fails.
	 * @param defaultError the new default error message
	 * @return this parser instance
	 */
	@Contract("_ -> this")
	P defaultError(@Nullable String defaultError);

	/**
	 * @return the Skript instance associated with this parser
	 */
	@Contract(pure = true)
	Skript skript();

}


