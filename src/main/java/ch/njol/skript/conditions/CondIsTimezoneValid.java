package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.time.DateTimeException;
import java.time.ZoneId;

@Name("Is Timezone Valid")
@Description("Checks if a timezone is valid.")
@Example("""
	set {_timezone} to "America/New_York"
	if {_timezone} is a valid timezone:
		set {_date} to now in timezone {_timezone}
	""")
@Since("INSERT VERSION")
public class CondIsTimezoneValid extends Condition {

	static {
		Skript.registerCondition(CondIsTimezoneValid.class, "%strings% (are|is [a[n]]) [negate:in]valid time[ ]zone[s]");
	}

	private Expression<String> timezones;
	private boolean isNegated;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		timezones = (Expression<String>) expressions[0];
		isNegated = parseResult.hasTag("negate");
		return true;
	}

	@Override
	public boolean check(Event event) {
		return timezones.check(event, CondIsTimezoneValid::isValidTimezone, isNegated);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return timezones.toString(event, debug) + " are " + (isNegated ? "in" : "") + "valid timezones";
	}

	private static boolean isValidTimezone(String timezone) {
		try {
			ZoneId.of(timezone);
		} catch (DateTimeException e) {
			return false;
		}

		return true;
	}

}
