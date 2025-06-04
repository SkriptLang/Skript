package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.time.ZoneId;

@Name("All Timezones")
@Description("Returns a list of all timezones that can be used in the <a href='#ExprNow'>now</a> expression.")
@Example("set {_timezones::*} to all timezones")
@Since("INSERT VERSION")
public class ExprAllTimezones extends SimpleExpression<String> {

	static {
		Skript.registerExpression(ExprAllTimezones.class, String.class, ExpressionType.SIMPLE, "all [of [the]] time[ ]zones");
	}

	private static String[] timezones = ZoneId.getAvailableZoneIds().toArray(new String[0]);

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		return true;
	}

	@Override
	protected String @Nullable [] get(Event event) {
		return timezones;
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "all timezones";
	}

}
