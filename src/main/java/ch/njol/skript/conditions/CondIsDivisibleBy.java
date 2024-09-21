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
@Examples({"if 5 is divisible by 5:",
		"if 1964903306 is divisible by 982451653:",
		"if 11 cannot be divided by 10:",
		"if 10007 cannnot be divided by 10007:"})
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
	private Expression<Number> num1;
	@SuppressWarnings("null")
	private Expression<Number> num2;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		num1 = (Expression<Number>) exprs[0];
		num2 = (Expression<Number>) exprs[1];
		setNegated(matchedPattern == 1 || matchedPattern == 3);
		return true;
	}

	@Override
	public boolean check(final Event event) {
		Number number2 = num2.getSingle(event);
		return num1.check(event, new Checker<Number>() {
			@Override
			public boolean check(Number number1) {
				double divided = number1.doubleValue();
				double divisor = number2 != null ? number2.doubleValue() : 0;
				return divided % divisor == 0;
			}
		}, isNegated());
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return num1.toString(event, debug) + " is divisible by " + num2.toString(event, debug);
	}

}
