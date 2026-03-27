package org.skriptlang.skript.lang.parsing.parsers;

import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript.Skript;

public class StatementParserImpl extends SyntaxParserImpl<StatementParserImpl> implements StatementParser<StatementParserImpl> {


	protected StatementParserImpl(Skript skript) {
		super(skript);
	}

	public StatementParserImpl(@NotNull SyntaxParser<?> other) {
		super(other);
	}
}
