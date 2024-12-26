package ch.njol.skript.expressions;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import ch.njol.skript.lang.Literal;
import ch.njol.util.Math2;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;

@Name("Random Numbers")
@Description({
		"A given amount of random numbers or integers between two given numbers. Use 'number' if you want any number with decimal parts, or use use 'integer' if you only want whole numbers.",
		"Please note that the order of the numbers doesn't matter, i.e. <code>random number between 2 and 1</code> will work as well as <code>random number between 1 and 2</code>."
})
@Examples({
		"set the player's health to a random number between 5 and 10",
		"send \"You rolled a %random integer from 1 to 6%!\" to the player",
		"set {_chances::*} to 5 random integers between 5 and 96",
		"set {_decimals::*} to 3 random numbers between 2.7 and -1.5"
})
@Since("1.4, INSERT VERSION (Multiple random numbers)")
public class ExprRandomNumber extends SimpleExpression<Number> {

	static {
		Skript.registerExpression(ExprRandomNumber.class, Number.class, ExpressionType.COMBINED,
				"[a|%-integer%] random (:integer|number)[s] (from|between) %number% (to|and) %number%");
	}

	@Nullable
	private Expression<Integer> amount;
	private Expression<Number> lower, upper;
	private boolean isInteger;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		amount = (Expression<Integer>) exprs[0];
		lower = (Expression<Number>) exprs[1];
		upper = (Expression<Number>) exprs[2];
		isInteger = parseResult.hasTag("integer");
		return true;
	}

	@Override
	@Nullable
	protected Number[] get(Event event) {
		Number lowerNumber = lower.getSingle(event);
		Number upperNumber = upper.getSingle(event);
		if (upperNumber == null || lowerNumber == null || !Double.isFinite(lowerNumber.doubleValue()) || !Double.isFinite(upperNumber.doubleValue()))
			return new Number[0];

		Integer amount = this.amount == null ? Integer.valueOf(1) : this.amount.getSingle(event);
		if (amount == null || amount <= 0)
			return new Number[0];

		double lower = Math.min(lowerNumber.doubleValue(), upperNumber.doubleValue());
		double upper = Math.max(lowerNumber.doubleValue(), upperNumber.doubleValue());
		Random random = ThreadLocalRandom.current();
		if (isInteger) {
			Long[] longs = new Long[amount];
			long floored_upper = Math2.floor(upper);
			long ceiled_lower = Math2.ceil(lower);

			// catch issues like `integer between 0.5 and 0.6`
			if (upper - lower < 1 && ceiled_lower - floored_upper <= 1) {
				if (floored_upper == ceiled_lower || lower == ceiled_lower) {
					Arrays.fill(longs, ceiled_lower);
					return longs;
				}
				if (upper == floored_upper) {
					Arrays.fill(longs, floored_upper);
					return longs;
				}
				return new Long[0];
			}

			for (int i = 0; i < amount; i++)
				longs[i] = Math2.ceil(lower) + Math2.mod(random.nextLong(), floored_upper - ceiled_lower + 1);
			return longs;
		// non-integers
		} else {
			Double[] doubles = new Double[amount];
			for (int i = 0; i < amount; i++)
				doubles[i] = Math.min(lower + random.nextDouble() * (upper - lower), upper);
			return doubles;
		}
	}

	@Override
	public boolean isSingle() {
		if (amount instanceof Literal)
			return ((Literal<Integer>) amount).getSingle() == 1;
		return amount == null;
	}

	@Override
	public Class<? extends Number> getReturnType() {
		return isInteger ? Long.class : Double.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return (amount == null ? "a" : amount.toString(event, debug)) + " random " + (isInteger ? "integer" : "number") +
				(amount == null ? "" : "s") + " between " + lower.toString(event, debug) + " and " + upper.toString(event, debug);
	}

}
