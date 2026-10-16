package org.skriptlang.skript.test.elements.expressions;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class ExprTestLoopPeeking extends SimpleExpression<Integer> {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(SyntaxRegistry.EXPRESSION,
			SyntaxInfo.Expression.simple(ExprTestLoopPeeking.class, ExprTestLoopPeeking::new, Integer.class,
				"test loop peeking disabled",
				"test loop peeking enabled"));
	}

	private boolean toPeek;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		toPeek = matchedPattern == 1;
		return true;
	}

	@Override
	protected Integer[] get(Event event) {
		return new Integer[]{1, 2, 3, 4, 5};
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<Integer> getReturnType() {
		return Integer.class;
	}

	@Override
	public boolean supportsLoopPeeking() {
		return toPeek;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "test loop peeking";
	}

}
