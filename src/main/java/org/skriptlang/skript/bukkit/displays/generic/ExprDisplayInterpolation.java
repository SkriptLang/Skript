package org.skriptlang.skript.bukkit.displays.generic;

import ch.njol.skript.classes.Changer.ChangeMode;
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
import ch.njol.util.Math2;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Display;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Display Interpolation Delay/Duration")
@Description({
	"Returns or changes the interpolation delay/duration of <a href='classes.html#display'>displays</a>.",
	"Interpolation duration is the amount of time a display will take to interpolate, or shift, between its current state and a new state.",
	"Interpolation delay is the amount of ticks before client-side interpolation will commence." +
	"Setting to 0 seconds will make it immediate.",
	"Resetting either value will return that value to 0."
})
@Examples("set interpolation delay of the last spawned text display to 2 ticks")
@Since("2.10")
public class ExprDisplayInterpolation extends SimplePropertyExpression<Display, Timespan> {

	static {
		registerDefault(ExprDisplayInterpolation.class, Timespan.class, "interpolation (:delay|duration)[s]", "displays");
	}

	private boolean delay;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		delay = parseResult.hasTag("delay");
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}

	@Override
	@Nullable
	public Timespan convert(Display display) {
		return new Timespan(Timespan.TimePeriod.TICK, delay ? display.getInterpolationDelay() : display.getInterpolationDuration());
	}

	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case ADD, REMOVE, SET -> CollectionUtils.array(Timespan.class);
			case RESET -> CollectionUtils.array();
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		Display[] displays = getExpr().getArray(event);
		long ticks = 0;
		if (delta != null)
			ticks = Math2.fit(Integer.MIN_VALUE, ((Timespan) delta[0]).getAs(TimePeriod.TICK), Integer.MAX_VALUE);

		switch (mode) {
			case REMOVE:
				ticks = -ticks;
				//$FALL-THROUGH$
			case ADD:
				for (Display display : displays) {
					if (delay) {
						int value = (int) Math2.fit(0, display.getInterpolationDelay() + ticks, Integer.MAX_VALUE);
						display.setInterpolationDelay(value);
					} else {
						int value = (int) Math2.fit(0, display.getInterpolationDuration() + ticks, Integer.MAX_VALUE);
						display.setInterpolationDuration(value);
					}
				}
				break;
			case RESET:
			case SET:
				ticks = Math2.fit(0, ticks, Integer.MAX_VALUE);
				for (Display display : displays) {
					if (delay) {
						display.setInterpolationDelay((int) ticks);
					} else {
						display.setInterpolationDuration((int) ticks);
					}
				}
				break;
		}
	}

	@Override
	public Class<? extends Timespan> getReturnType() {
		return Timespan.class;
	}

	@Override
	protected String getPropertyName() {
		return "interpolation " + (delay ? "delay" : "duration");
	}

}
