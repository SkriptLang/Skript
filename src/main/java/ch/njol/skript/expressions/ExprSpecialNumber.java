package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Special Number")
@Description("Special number values, namely NaN, Infinity and -Infinity")
@Examples({"if {_number} is infinity value:"})
@Since("2.2-dev32d")
public class ExprSpecialNumber extends SimpleExpression<Number> {
	private int value;

	static {
		Skript.registerExpression(
			ExprSpecialNumber.class,
			Number.class,
			ExpressionType.SIMPLE, 
			"(0¦NaN|1¦[(2¦-|2¦minus)](infinity|\u221e)) value",
			"value of (0¦NaN|1¦[(2¦-|2¦minus)](infinity|\u221e))"
		);
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		this.value = parseResult.mark;
		return true;
	}

	@Override
	protected Number[] get(Event e) {
		return new Number[]{value == 0 ? Double.NaN : value == 1 ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY};
	}

	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return value == 0 ? "NaN value" : value == 1 ? "infinity value" : "-infinity value";
	}
}
