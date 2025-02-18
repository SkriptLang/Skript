package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Pi")
@Description("Returns the mathematical constant pi. (approx. 3.1415926535)")
@Examples("set {_tau} to pi * 2")
@Since("2.7")
public class LitPi extends SimpleLiteral<Double> {

	static {
		Skript.registerExpression(LitPi.class, Double.class, ExpressionType.SIMPLE, "(pi|π)");
	}

	public LitPi() {
		super(Math.PI, false);
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		return true;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "pi";
	}
	
}
