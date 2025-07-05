package ch.njol.skript.test.runner;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.NoDoc;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@NoDoc
public class LitLongMaxValue extends SimpleLiteral<Long> {

	static {
		if (TestMode.ENABLED) {
			Skript.registerExpression(LitLongMaxValue.class, Long.class, ExpressionType.SIMPLE, "long max[imum] value");
		}
	}

	public LitLongMaxValue() {
		super(Long.MAX_VALUE, false);
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		return true;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "long max value";
	}

}
