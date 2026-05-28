package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import ch.njol.skript.lang.simplification.SimplifiedLiteral;

@Name("Percent of")
@Description("Returns a percentage of one or more numbers.")
@Example("set damage to 10% of victim's health")
@Example("set damage to 125 percent of damage")
@Example("set {_result} to {_percent} percent of 999")
@Example("set {_result::*} to 10% of {_numbers::*}")
@Example("set experience to 50% of player's total experience")
@Since("2.8.0")
public class ExprPercent extends SimpleExpression<Number> {

	static {
		Skript.registerExpression(ExprPercent.class, Number.class, ExpressionType.COMBINED, "%number%(\\%| percent) of %numbers%");
	}

	private Expression<Number> percent;
	private Expression<Number> numbers;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		percent = (Expression<Number>) exprs[0];
		numbers = (Expression<Number>) exprs[1];
		return true;
	}

	@Override
	protected @Nullable Number[] get(Event event) {
		Number percent = this.percent.getSingle(event);
		Number[] numbers = this.numbers.getArray(event);
		if (percent == null || numbers.length == 0)
			return null;

		Number[] results = new Number[numbers.length];
		for (int i = 0; i < numbers.length; i++) {
			results[i] = numbers[i].doubleValue() * percent.doubleValue() / 100;
		}

		return results;
	}

	@Override
	public boolean isSingle() {
		return numbers.isSingle();
	}

	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}

	@Override
	public Expression<? extends Number> simplify() {
		if (percent instanceof Literal<Number> && numbers instanceof Literal<Number>)
			return SimplifiedLiteral.fromExpression(this);
		return this;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return percent.toString(event, debug) + " percent of " + numbers.toString(event, debug);
	}

}
