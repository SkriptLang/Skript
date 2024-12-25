package ch.njol.skript.conditions;

import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Date;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
@Name("Time")
@Description("Tests whether a given <a href='classes.html#date'>real time</a> was more or less than some <a href='classes.html#timespan'>time span</a> ago.")
@Examples({"command /command-with-cooldown:",
		"	trigger:",
		"		{command::%player's uuid%::last-usage} was less than a minute ago:",
		"			message \"Please wait a minute between uses of this command.\"",
		"			stop",
		"		set {command::%player's uuid%::last-usage} to now",
		"		# ... actual command trigger here ..."})
@Since("2.0")
public class CondDate extends Condition {
	
	static {
		Skript.registerCondition(CondDate.class,
				"%date% (was|were)( more|(n't| not) less) than %timespan% [ago]",
				"%date% (was|were)((n't| not) more| less) than %timespan% [ago]");
	}
	
	@SuppressWarnings("null")
	private Expression<Date> date;
	@SuppressWarnings("null")
	private Expression<Timespan> delta;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		date = (Expression<Date>) exprs[0];
		delta = (Expression<Timespan>) exprs[1];
		setNegated(matchedPattern == 1);
		return true;
	}
	
	@Override
	public boolean check(final Event e) {
		final long now = System.currentTimeMillis();
		return date.check(e,
				date -> delta.check(e,
						timespan -> now - date.getTimestamp() >= timespan.getAs(Timespan.TimePeriod.MILLISECOND)
				), isNegated());
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return date.toString(e, debug) + " was " + (isNegated() ? "less" : "more") + " than " + delta.toString(e, debug) + " ago";
	}
	
}
