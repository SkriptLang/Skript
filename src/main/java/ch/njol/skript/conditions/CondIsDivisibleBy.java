package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Node;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.log.runtime.SyntaxRuntimeErrorProducer;

@Name("Is Evenly Divisible By")
@Description("""
	Checks if a number is evenly divisible by another number.
	An optional tolerance can be provided to counteract floating point error. The default tolerance is 1e-10.
	Any input smaller than the tolerance is considered to be 0.
	This means divisors that are too small will always return false, and dividends that are too small will always return true.
	""")
@Example("if 5 is evenly divisible by 5:")
@Example("if 11 cannot be evenly divided by 10:")
@Example("if 0.3 can be evenly divided by 0.1 with a tolerance of 0.0000001:")
@Since("2.10, 2.12 (tolerance)")
public class CondIsDivisibleBy extends Condition implements SyntaxRuntimeErrorProducer {

	static {
		Skript.registerCondition(CondIsDivisibleBy.class,
			"%numbers% (is|are) evenly divisible by %number% [with [a] tolerance [of] %-number%]",
			"%numbers% (isn't|is not|aren't|are not) evenly divisible by %number% [with [a] tolerance [of] %-number%]",
			"%numbers% can be evenly divided by %number% [with [a] tolerance [of] %-number%]",
			"%numbers% (can't|can[ ]not) be evenly divided by %number% [with [a] tolerance [of] %-number%]");
	}

	private Expression<Number> dividend;
	private Expression<Number> divisor;
	private @Nullable Expression<Number> epsilon;
	private Node node;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		dividend = (Expression<Number>) exprs[0];
		divisor = (Expression<Number>) exprs[1];
		epsilon = (Expression<Number>) exprs[2];
		setNegated(matchedPattern == 1 || matchedPattern == 3);
		node = getParser().getNode();
		return true;
	}

	@Override
	public boolean check(Event event) {
		Number divisorNumber = divisor.getSingle(event);
		if (divisorNumber == null)
			return isNegated();
		double divisor = divisorNumber.doubleValue();
		if (divisor == 0) {
			// Division by zero never passes
			return isNegated();
		}

		Number epsilonNumber = epsilon != null ? epsilon.getSingle(event) : Skript.EPSILON;
		if (epsilonNumber == null) {
			epsilonNumber = Skript.EPSILON;
		}
		double epsilon = epsilonNumber.doubleValue();

		if (epsilon <= 0 || Double.isNaN(epsilon)) {
			error("Tolerance must be a positive, non-zero number, but was " + epsilonNumber + ".");
			return isNegated();
		}

		if (divisor < epsilon) {
			// If the divisor is smaller than the tolerance, we consider it to be 0
			return isNegated();
		}

		return dividend.check(event, dividendNumber -> {
			double remainder = Math.abs(dividendNumber.doubleValue() % divisor);
			return remainder <= epsilon || remainder >= divisor - epsilon;
		}, isNegated());
	}

	@Override
	public Node getNode() {
		return node;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return dividend.toString(event, debug) + " is " + (isNegated() ? "not " : "")
			+ "evenly divisible by " + divisor.toString(event, debug);
	}

}
