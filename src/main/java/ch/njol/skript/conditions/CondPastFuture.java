package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.ExprNow;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionList;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Date;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("In The Past/Future")
@Description({
	"Checks whether a date is in the past or future.",
	"Note that using the 'now' expression will not be in the past or future when used directly in the condition."
})
@Examples({
	"set {_date} to now",
	"wait 5 seconds",
	"if {_date} is in the past:",
		"\t# this will be true",
	"",
	"if now is in the future:",
		"\t# this will be false",
	"",
	"set {_dates::*} to 1 day from now, 12 days from now, and 1 year from now",
	"if {_dates::*} are in the future:",
		"\t# this will be true",
	"if {_dates::*} have passed:",
		"\t# this will be false"
})
@Since("2.10")
public class CondPastFuture extends Condition {

	static {
		Skript.registerCondition(CondPastFuture.class,
				"%dates% (is|are)[negated:(n't| not)] in the (past|:future)",
				"%dates% ha(s|ve)[negated:(n't| not)] passed");
	}

	private Expression<Date> dates;
	private boolean isFuture;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setNegated(parseResult.hasTag("negated"));
		// we default to past, so we only need to check if it's future
		isFuture = parseResult.hasTag("future");
		dates = (Expression<Date>) expressions[0];
		return true;
	}

	@Override
	public boolean check(Event event) {
		// now should never be in the past or future
		if (dates instanceof ExprNow)
			return isNegated();

		// This may not be worth checking
		if (dates instanceof ExpressionList<Date> list) {
			for (Expression<? extends Date> dateExpression : list.getExpressions()) {
				if (dateExpression instanceof ExprNow && list.getAnd())
					return isNegated();
			}
		}

		// using the same 'now' date for all checks is flawed, because the input dates are evaluated during the
		// check, so it could cause 'now is in the future' to be true, when it should be false.
		// This is still possible, but it's less likely to happen as the two dates are created closer together.
		if (isFuture)
			return dates.check(event, date -> date.compareTo(new Date()) > 0, isNegated());
		return dates.check(event, date -> date.compareTo(new Date()) < 0, isNegated());
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return dates.toString(event, debug) + (dates.isSingle() ? " is"  : " are") + " in the" + (isFuture ? " future" : " past");
	}

}
