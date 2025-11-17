package org.skriptlang.skript.lang.parser;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript.Skript;

public interface FunctionParser<P extends FunctionParser<P>> extends SyntaxParser<P> {

	@Contract("_ -> new")
	static @NotNull FunctionParser<?> from(Skript skript) {
		return new FunctionParserImpl(skript);
	}

	@Contract("_ -> new")
	static @NotNull FunctionParser<?> from(SyntaxParser<?> other) {
		return new FunctionParserImpl(other);
	}

}
