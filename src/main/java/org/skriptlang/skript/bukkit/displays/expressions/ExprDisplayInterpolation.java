package org.skriptlang.skript.bukkit.displays.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Timespan.TimePeriod;
import ch.njol.util.Kleenean;
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
@RequiredPlugins("Spigot 1.19.4+")
@Since("INSERT VERSION")
public class ExprDisplayInterpolation extends SimplePropertyExpression<Display, Timespan> {

	static {
		if (Skript.isRunningMinecraft(1, 19, 4))
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
			case DELETE, REMOVE_ALL -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		Display[] displays = getExpr().getArray(event);
		int ticks = 0;
		if (delta != null)
			ticks = (int) ((Timespan) delta[0]).getAs(TimePeriod.TICK);

		switch (mode) {
			case REMOVE:
				ticks = -ticks;
			case ADD:
				for (Display display : displays) {
					if (delay) {
						int value = Math.max(0, display.getInterpolationDelay() + ticks);
						display.setInterpolationDelay(value);
					} else {
						int value = Math.max(0, display.getInterpolationDuration() + ticks);
						display.setInterpolationDuration(value);
					}
				}
				break;
			case RESET:
			case SET:
				ticks = Math.max(0, ticks);
				for (Display display : displays) {
					if (delay) {
						display.setInterpolationDelay(ticks);
					} else {
						display.setInterpolationDuration(ticks);
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
