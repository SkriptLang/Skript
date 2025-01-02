package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Is Evenly Divisible By")
@Description("Check if a number is evenly divisible by another number.")
@Examples({
	"if 5 is evenly divisible by 5:",
	"if 11 cannot be evenly divided by 10:",
})
@Since("2.10")
public class CondIsDivisibleBy extends Condition {

	static {
		Skript.registerCondition(CondIsDivisibleBy.class,
			"%numbers% (is|are) evenly divisible by %number%",
			"%numbers% (isn't|is not|aren't|are not) evenly divisible by %number%",
			"%numbers% can be evenly divided by %number%",
			"%numbers% (can't|can[ ]not) be evenly divided by %number%");
	}
	@SuppressWarnings("null")
	private Expression<Number> dividend;
	@SuppressWarnings("null")
	private Expression<Number> divisor;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		dividend = (Expression<Number>) exprs[0];
		divisor = (Expression<Number>) exprs[1];
		setNegated(matchedPattern == 1 || matchedPattern == 3);
		return true;
	}

	@Override
	public boolean check(Event event) {
		Number divisorNumber = divisor.getSingle(event);
		if (divisorNumber == null)
			return isNegated();
		double divisor = divisorNumber.doubleValue();
		return dividend.check(event, dividendNumber -> (dividendNumber.doubleValue() % divisor == 0), isNegated());
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return dividend.toString(event, debug) + " is " + (isNegated() ? "not " : "")
			+ "evenly divisible by " + divisor.toString(event, debug);
	}

}
