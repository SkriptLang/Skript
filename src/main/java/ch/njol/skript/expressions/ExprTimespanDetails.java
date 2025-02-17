package ch.njol.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Timespan.TimePeriod;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

@Name("Timespan Details")
@Description("Retrieve specific information of a <a href=\"/classes.html#timespan\">timespan</a> such as hours/minutes/etc.")
@Examples({
	"set {_t} to difference between now and {Payouts::players::%uuid of player%::last-date}",
	"send \"It has been %days of {_t}% day(s) since last payout.\""
})
@Since("2.9.0")
public class ExprTimespanDetails extends SimplePropertyExpression<Timespan, Long> {

	static {
		register(ExprTimespanDetails.class, Long.class, "(:(tick|second|minute|hour|day|week|month|year))s", "timespans");
	}

	@SuppressWarnings("NotNullFieldNotInitialized")
	private TimePeriod type;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		type = TimePeriod.valueOf(parseResult.tags.get(0).toUpperCase(Locale.ENGLISH));
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}

	@Override
	@Nullable
	public Long convert(Timespan time) {
		return time.getAs(Timespan.TimePeriod.MILLISECOND) / type.getTime();
	}

	@Override
	public Class<? extends Long> getReturnType() {
		return Long.class;
	}

	@Override
	protected String getPropertyName() {
		return type.name().toLowerCase(Locale.ENGLISH);
	}

}
