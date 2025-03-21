package ch.njol.skript.expressions;

import ch.njol.skript.registrations.EventValues;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.WrapperExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

@Name("Former/Future State")
@Description({
	"Represents the value of an expression before an event happened or the value it will have directly after the event, e.g. the old or new level respectively in a <a href='./events.html#level_change'>level change event</a>.",
	"Note: The past, future and present states of an expression are sometimes called 'time states' of an expression.",
	"Note 2: If you don't specify whether to use the past or future state of an expression that has different values, its default value will be used which is usually the value after the event."
})
@Examples({
	"on teleport:",
		"\tformer world was \"world_nether\" # or 'world was'",
		"\tworld will be \"world\" # or 'world after the event is'",
	"on tool change:",
		"\tpast tool is an axe",
		"\tthe tool after the event will be air",
	"on weather change:",
		"\tset {weather::%world%::old} to past weather",
		"\tset {weather::%world%::current} to the new weather"
})
@Since("1.1")
public class ExprTimeState extends WrapperExpression<Object> {

	static {
		Skript.registerExpression(ExprTimeState.class, Object.class, ExpressionType.PROPERTY,
			"[the] (former|past|old) [state] [of] %~objects%", "%~objects% before [the event]",
			"[the] (future|to-be|new) [state] [of] %~objects%", "%~objects%(-to-be| after[(wards| the event)])");
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		Expression<?> expr = expressions[0];
		if (isDelayed == Kleenean.TRUE) {
			Skript.error("Cannot use time states after the event has already passed");
			return false;
		}
		if (!expr.setTime(matchedPattern >= 2 ? EventValues.TIME_FUTURE : EventValues.TIME_PAST)) {
			Skript.error(expr + " does not have a " + (matchedPattern >= 2 ? "future" : "past") + " state");
			return false;
		}
		setExpr(expr);
		return true;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the " + (getTime() == EventValues.TIME_PAST ? "past" : "future") + " state of " + getExpr().toString(event, debug);
	}

	@Override
	public boolean setTime(int time) {
		return time == getTime();
	}

}
