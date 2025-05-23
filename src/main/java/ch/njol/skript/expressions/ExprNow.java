package ch.njol.skript.expressions;

import ch.njol.skript.doc.*;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Date;
import ch.njol.util.Kleenean;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Name("Now")
@Description({
	"The current <a href='classes.html#date'>system time</a> of the server. "
		+ "Use <a href='#ExprTime'>time</a> to get the <a href='classes.html#time'>Minecraft time</a> of a world.",
	"Optionally specify a timezone to get the current date at a timezone. The returned value might not be equal to"
		+ "'now' without timezones. If a timezone is invalid no value will be returned.",
	"Use <a href='#ExprAllTimezones'>all timezones</a> to get a list of valid timezones."
})
@Example("broadcast \"Current server time: %now%\"")
@Example("""
	set {_date} to now in timezone "Europe/Istanbul"
	set {_clock} to {_date} formatted as "kk:mm"
	send "It is currently %{_clock}% in Istanbul!" to player
	""")
@Since("1.4, INSERT VERSION (timezones)")
public class ExprNow extends SimpleExpression<Date> {
	
	static {
		Skript.registerExpression(ExprNow.class, Date.class, ExpressionType.SIMPLE, "now [timezone:in time[ ]zone %-string%]");
	}

	private boolean usingTimezone;
	private Expression<String> timezone;
	
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		usingTimezone = parseResult.hasTag("timezone");
		timezone = (Expression<String>) exprs[0];
		return true;
	}
	
	@Override
	protected Date[] get(Event event) {
		if (usingTimezone) {
			String timezone = this.timezone.getSingle(event);
			if (timezone == null) {
				return new Date[0];
			}

			ZoneId targetZoneId;
			try {
				targetZoneId = ZoneId.of(timezone);
			} catch (DateTimeException e) { // invalid zone format
				return new Date[0];
			}

			ZoneId localZoneId = ZoneId.systemDefault();
			Instant shiftedNow = ZonedDateTime.now(targetZoneId)
				.toLocalDateTime()
				.atZone(localZoneId)
				.toInstant();
			java.util.Date javaDate = java.util.Date.from(shiftedNow);
			Date date = Date.fromJavaDate(javaDate);
			return new Date[]{ date };
 		}
		return new Date[]{ new Date() };
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
		if (usingTimezone) {
			return "now in timezone " + timezone.toString(event, debug);
		}
		return "now";
	}

}
