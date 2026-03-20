package org.skriptlang.skript.lang.parsing.parsers;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ExprInfo;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.Skript;
import org.skriptlang.skript.lang.parsing.sites.ExpressionSite;

public interface ExpressionParser<P extends ExpressionParser<P>> extends SyntaxParser<P> {

	@Contract("_ -> new")
	static @NotNull ExpressionParser<?> from(Skript skript) {
		return new ExpressionParserImpl(skript);
	}

	@Contract("_ -> new")
	static @NotNull ExpressionParser<?> from(SyntaxParser<?> other) {
		return new ExpressionParserImpl(other);
	}

	/**
	 * Parse an expression with the given return types.
	 * @param returnTypes The types the parsed expression may return.
	 * @param <T> Supertype of the return types.
	 * @return An expression returning one or more of the given types, or null if parsing failed.
	 */
	@SuppressWarnings("unchecked")
	default <T> @Nullable Expression<? extends T> parse(Class<? extends T>... returnTypes) {
		return parse(new ExpressionSite(returnTypes));
	}

	/**
	 * Parses an expression based on the provided ExprInfo.
	 * @param info the ExprInfo containing the constraints for the expression to be parsed
	 * @return the parsed Expression, or null if parsing failed
	 * @deprecated Prefer {@link #parse(ExpressionSite)} instead.
	 */
	@Deprecated(since = "INSERT VERSION")
	default <T> @Nullable Expression<? extends T> parse(ExprInfo info) {
		return parse(new ExpressionSite(info));
	}

	/**
	 * Parses an expression based on the provided expression parsing site.
	 * @param site The site this expression is being parsed for.
	 * @return An expression that meets the requirements of the given site, or null if parsing failed.
	 */
	<T> @Nullable Expression<? extends T> parse(ExpressionSite site);

}
