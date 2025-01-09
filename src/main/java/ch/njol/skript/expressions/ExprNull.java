package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

public class ExprNull extends SimpleExpression<Object> {

	static {
		//if (TestMode.ENABLED)
			Skript.registerExpression(ExprNull.class, Object.class, ExpressionType.SIMPLE,
				"best (1|one)",
				"best (2|two)",
				"best (3|three)",
				"best (4|four)");
	}

	private int pattern;
	private String expr;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		pattern = matchedPattern;
		expr = parseResult.expr;
		return true;
	}

	@Override
	protected Object @Nullable [] get(Event event) {
		return null;
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<?> getReturnType() {
		return Object.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return expr;
	}

}
