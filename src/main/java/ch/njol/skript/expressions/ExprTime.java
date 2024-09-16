package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Time;
import ch.njol.skript.util.Timeperiod;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Time")
@Description({
	"The <a href='classes.html#time'>time</a> of a world.",
	"Use the \"minecraft <a href='classes.html#timespan'>timespan</a>\" syntax to change the time according " +
	"to Minecraft's time intervals.",
	"Since Minecraft uses discrete intervals for time (ticks), " +
	"changing the time by real-world minutes or real-world seconds only changes it approximately.",
	"Removing an amount of time from a world's time will move the clock forward a day."
})
@Examples({
	"set time of world \"world\" to 2:00",
	"add 2 minecraft hours to time of world \"world\"",
	"add 54 real seconds to time of world \"world\" # approximately 1 minecraft hour"
})
@Since("1.0")
public class ExprTime extends PropertyExpression<World, Time> {

	// 18000 is the offset to allow for using "add 2:00" without going to a new day
	// and causing unexpected behaviour
	private static final int TIME_TO_TIMESPAN_OFFSET = 18000;

	static {
		Skript.registerExpression(ExprTime.class, Time.class, ExpressionType.PROPERTY,
			"[the] time[s] [([with]in|of) %worlds%]", "%worlds%'[s] time[s]");
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parser) {
		setExpr((Expression<World>) expressions[0]);
		return true;
	}

	@Override
	protected Time[] get(Event event, World[] worlds) {
		return get(worlds, world -> new Time((int) world.getTime()));
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(final ChangeMode mode) {
		switch (mode) {
			case ADD:
			case REMOVE:
				// allow time to avoid conversion to timespan, which causes all sorts of headaches
				return CollectionUtils.array(Time.class, Timespan.class);
			case SET:
				return CollectionUtils.array(Time.class, Timeperiod.class);
			default:
				return null;
		}
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		if (getExpr() == null || delta == null)
			return;

		Object time = delta[0];
		if (time == null)
			return;

		World[] worlds = getExpr().getArray(event);

		long ticks = 0;
		if (time instanceof Time) {
			if (mode != ChangeMode.SET) {
				ticks = ((Time) time).getTicks() - TIME_TO_TIMESPAN_OFFSET;
			} else {
				ticks = ((Time) time).getTicks();
			}
		} else if (time instanceof Timespan) {
			ticks = ((Timespan) time).getAs(Timespan.TimePeriod.TICK);
		} else if (time instanceof Timeperiod) {
			ticks = ((Timeperiod) time).start;
		}

		for (World world : worlds) {
			switch (mode) {
				case ADD:
					world.setTime(world.getTime() + ticks);
					break;
				case REMOVE:
					world.setTime(world.getTime() - ticks);
					break;
				case SET:
					world.setTime(ticks);
					break;
			}
		}
	}

	@Override
	public Class<Time> getReturnType() {
		return Time.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the time in " + getExpr().toString(event, debug);
	}

}
