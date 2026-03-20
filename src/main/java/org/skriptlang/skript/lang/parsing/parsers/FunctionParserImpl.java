package org.skriptlang.skript.lang.parsing.parsers;

import ch.njol.skript.lang.function.FunctionReference;
import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript.Skript;

class FunctionParserImpl extends SyntaxParserImpl<FunctionParserImpl> implements FunctionParser<FunctionParserImpl> {

	protected FunctionParserImpl(Skript skript) {
		super(skript);
	}

	public FunctionParserImpl(@NotNull SyntaxParser<?> other) {
		super(other);
	}

	public <T> FunctionReference<T> parse() {
		return null;
	}
}
