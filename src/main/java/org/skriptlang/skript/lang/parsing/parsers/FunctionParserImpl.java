package org.skriptlang.skript.lang.parsing.parsers;

import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript.Skript;
import org.skriptlang.skript.common.function.FunctionReference;
import org.skriptlang.skript.common.function.FunctionReferenceParser;

class FunctionParserImpl extends SyntaxParserImpl<FunctionParserImpl> implements FunctionParser<FunctionParserImpl> {

	protected FunctionParserImpl(Skript skript) {
		super(skript);
	}

	public FunctionParserImpl(@NotNull SyntaxParser<?> other) {
		super(other);
	}

	public <T> FunctionReference<T> parse() {
		if (context != ParseContext.DEFAULT && context != ParseContext.EVENT)
			return null;
		int flags = localConstraints != null ? localConstraints.asParseFlags() : SkriptParser.ALL_FLAGS;
		return new FunctionReferenceParser(context, flags).parseFunctionReference(input);
	}

}
