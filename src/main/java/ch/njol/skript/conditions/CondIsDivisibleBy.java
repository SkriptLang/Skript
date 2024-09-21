package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Checker;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Divisible By")
@Description("Check if a number is divisible by another number.")
@Examples({
	"if 5 is divisible by 5:",
	"if 11 cannot be divided by 10:",
})
@Since("INSERT VERSION")
public class CondIsDivisibleBy extends Condition {

	static {
		Skript.registerCondition(CondIsDivisibleBy.class,
			"%numbers% (is|are) divisible by %number%",
			"%numbers% (isn't|is not|aren't|are not) divisible by %number%",
			"%numbers% can be [evenly] divided by %number%",
			"%numbers% (can't|cannot|can not) be [evenly] divided by %number%");
	}

	@SuppressWarnings("null")
	private Expression<Number> divisorExpression;
	@SuppressWarnings("null")
	private Expression<Number> dividedExpression;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		divisorExpression = (Expression<Number>) exprs[0];
		dividedExpression = (Expression<Number>) exprs[1];
		setNegated(matchedPattern == 1 || matchedPattern == 3);
		return true;
	}

	@Override
	public boolean check(Event event) {
		Number divisorNumber = dividedExpression.getSingle(event);
		return divisorExpression.check(event, new Checker<Number>() {
			@Override
			public boolean check(Number dividedNumber) {
				double divided = dividedNumber.doubleValue();
				double divisor = divisorNumber != null ? divisorNumber.doubleValue() : null;
				return divided % divisor == 0;
			}
		}, isNegated());
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return divisorExpression.toString(event, debug) + " is divisible by " + dividedExpression.toString(event, debug);
	}

}
