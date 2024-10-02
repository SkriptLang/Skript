package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Date;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Date Ago/Later")
@Description("A date the specified timespan before/after another date.")
@Examples({
	"set {_yesterday} to 1 day ago",
	"set {_hourAfter} to 1 hour after {someOtherDate}",
	"set {_hoursBefore} to 5 hours before {someOtherDate}"
})
@Since("2.2-dev33")
public class ExprDateAgoLater extends SimpleExpression<Date> {

	static {
		Skript.registerExpression(ExprDateAgoLater.class, Date.class, ExpressionType.COMBINED,
			"%timespan% (ago|in the past|before [the] [date] %-date%)",
			"%timespan% (later|(from|after) [the] [date] %-date%)");
	}

	private boolean ago;
	private Expression<Timespan> timespanExpr;
	private @Nullable Expression<Date> dateExpr;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] expressions, int matchedPattern,
						Kleenean isDelayed, ParseResult parseResult) {
		timespanExpr = (Expression<Timespan>) expressions[0];
		dateExpr = (Expression<Date>) expressions[1];

		ago = matchedPattern == 0;
		return true;
	}

	@Override
	protected Date @Nullable [] get(Event event) {
		Timespan timespan = timespanExpr.getSingle(event);
		Date date = dateExpr != null ? dateExpr.getSingle(event) : new Date();
		if (timespan == null || date == null)
			return null;

		return new Date[]{ago ? date.minus(timespan) : date.plus(timespan)};
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends Date> getReturnType() {
		return Date.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return timespanExpr.toString(event, debug) + " " +
			(ago ? (dateExpr != null ? "before " + dateExpr.toString(event, debug) : "ago")
				: (dateExpr != null ? "after " + dateExpr.toString(event, debug) : "later"));
	}

}
