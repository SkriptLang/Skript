package org.skriptlang.skript.lang.parsing.parsers;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript.Skript;

public interface StatementParser<P extends StatementParser<P>> extends SyntaxParser<P> {

	@Contract("_ -> new")
	static @NotNull StatementParser<?> from(Skript skript) {
		return new StatementParserImpl(skript);
	}

	@Contract("_ -> new")
	static @NotNull StatementParser<?> from(SyntaxParser<?> other) {
		return new StatementParserImpl(other);
	}

}
