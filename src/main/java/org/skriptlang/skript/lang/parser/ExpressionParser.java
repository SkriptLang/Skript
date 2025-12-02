package org.skriptlang.skript.lang.parser;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript.Skript;

public interface ExpressionParser<P extends ExpressionParser<P>> extends SyntaxParser<P> {


	@Contract("_ -> new")
	static @NotNull ExpressionParser<?> from(Skript skript) {
		return new ExpressionParserImpl(skript);
	}

	@Contract("_ -> new")
	static @NotNull ExpressionParser<?> from(SyntaxParser<?> other) {
		return new ExpressionParserImpl(other);
	}


}
