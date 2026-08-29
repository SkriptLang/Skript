package org.skriptlang.skript.bukkit.misc.elements.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.util.Time;
import ch.njol.skript.util.Timeperiod;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Time")
@Description("""
	The <a href='#time'>time</a> of a world or player.
	Use the minecraft <a href='#timespan'>timespan</a> syntax to change the time according
	to Minecraft's time intervals.
	Since Minecraft uses discrete intervals for time (ticks),
	changing the time by real-world minutes or real-world seconds only changes it approximately.
	Removing an amount of time from a world or player's time will move the clock forward a day.
	""")
@Example("set time of world \"world\" to 2:00")
@Example("add 2 minecraft hours to time of world \"world\"")
@Example("add 54 real seconds to time of world \"world\" # approximately 1 minecraft hour")
@Example("set client time of player to 2:00")
@Since("1.0, INSERT VERSION (player support)")
public class ExprTime extends PropertyExpression<Object, Time> {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(SyntaxRegistry.EXPRESSION, SyntaxInfo.Expression.builder(ExprTime.class, Time.class)
			.supplier(ExprTime::new)
			.addPatterns(
				"[the] time[s] [([with]in|of) %worlds/players%]", 
				"%worlds/players%'[s] time[s]",
				"[the] (custom|client) time [of %players%]",
				"%players%'[s] (custom|client) time")
			.build());
	}

	// 18000 is the offset to allow for using "add 2:00" without going to a new day
	// and causing unexpected behavior
	private static final int TIME_TO_TIMESPAN_OFFSET = 18000;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parser) {
		setExpr(expressions[0]);
		return true;
	}

	@Override
	protected Time[] get(Event event, Object[] source) {
		return get(source, object -> {
			if (object instanceof Player player) {
				return new Time((int) player.getPlayerTime());
			} else if (object instanceof World world) {
				return new Time((int) world.getTime());
			}
			return null;
		});
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case ADD, REMOVE ->
				// allow time to avoid conversion to timespan, which causes all sorts of headaches
				CollectionUtils.array(Time.class, Timespan.class);
			case SET -> CollectionUtils.array(Time.class, Timeperiod.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		if (delta == null)
			return;

		Object time = delta[0];
		if (time == null)
			return;

		long ticks = 0;
		switch (time) {
			case Time time1 -> {
				if (mode != ChangeMode.SET) {
					ticks = time1.getTicks() - TIME_TO_TIMESPAN_OFFSET;
				} else {
					ticks = time1.getTicks();
				}
			}
			case Timespan timespan -> ticks = timespan.getAs(Timespan.TimePeriod.TICK);
			case Timeperiod timeperiod -> ticks = timeperiod.start;
			default -> {
			}
		}

		Object[] objects = getExpr().getArray(event);
		for (Object object : objects) {
			if (object instanceof World world) {
				switch (mode) {
					case ADD -> world.setTime(world.getTime() + ticks);
					case REMOVE -> world.setTime(world.getTime() - ticks);
					case SET -> world.setTime(ticks);
				}
			}
			if (object instanceof Player player) {
				switch (mode) {
					case ADD -> player.setPlayerTime(player.getPlayerTime() + ticks, true);
					case REMOVE -> player.setPlayerTime(player.getPlayerTime() - ticks, true);
					case SET -> player.setPlayerTime(ticks, true);
				}
			}
		}
	}

	@Override
	public Class<Time> getReturnType() {
		return Time.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return new SyntaxStringBuilder(event, debug)
			.append("the time of", getExpr())
			.toString();
	}

}
