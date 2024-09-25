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
	"if 5 is evenly divisible by 5:",
	"if 11 cannot be evenly divided by 10:",
})
@Since("INSERT VERSION")
public class CondIsDivisibleBy extends Condition {

	static {
		Skript.registerCondition(CondIsDivisibleBy.class,
			"%numbers% (is|are) evenly divisible by %number%",
			"%numbers% (isn't|is not|aren't|are not) evenly divisible by %number%",
			"%numbers% can be evenly divided by %number%",
			"%numbers% (can't|cannot|can not) be evenly divided by %number%");
	}
	@SuppressWarnings("null")
	private Expression<Number> dividendExpression;
	@SuppressWarnings("null")
	private Expression<Number> divisorExpression;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		dividendExpression = (Expression<Number>) exprs[0];
		divisorExpression = (Expression<Number>) exprs[1];
		setNegated(matchedPattern == 1 || matchedPattern == 3);
		return true;
	}

	@Override
	public boolean check(Event event) {
		Number divisorNumber = divisorExpression.getSingle(event);
		return dividendExpression.check(event, new Checker<Number>() {
			@Override
			public boolean check(Number dividendNumber) {
				double dividend = dividendNumber.doubleValue();
				double divisor = divisorNumber.doubleValue();
				return dividend % divisor == 0;
			}
		}, isNegated());
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return divisorExpression.toString(event, debug) + " is divisible by " + dividendExpression.toString(event, debug);
	}

}
