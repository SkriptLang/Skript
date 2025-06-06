package ch.njol.skript.test.runner;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

public class ExprTestLoopPeeking extends SimpleExpression<Integer> {

	static {
		Skript.registerExpression(ExprTestLoopPeeking.class, Integer.class, ExpressionType.SIMPLE,
			"test loop peeking disabled",
			"test loop peeking enabled");
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
