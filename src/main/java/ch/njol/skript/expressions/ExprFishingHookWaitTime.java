package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.FishHook;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Fishing Hook Wait Time")
@Description({
	"Returns the minimum and/or maximum waiting time of the fishing hook. " +
		"Default minimum value is 5 seconds and maximum is 30 seconds, before lure is applied.",
	"Changing values outside the default range, like setting the max wait time to " +
		"less than the min wait time, will set both the min and max " +
		"waiting time to the same value."
})
@Examples({
	"on fishing line cast:",
		"\tset min waiting time of fishing hook to 10 seconds",
		"\tset max waiting time of fishing hook to 20 seconds",
	"",
	"on rod cast:",
		"\tset max waiting time of fishing hook to 1 second # Will also force setting the minimum to 1 second"
})
@Events("Fishing")
@Since("INSERT VERSION")
public class ExprFishingHookWaitTime extends SimplePropertyExpression<FishHook, Timespan> {

	static {
		register(ExprFishingHookWaitTime.class, Timespan.class,
			"(min:min[imum]|max[imum]) wait[ing] time", "fishinghooks");
	}

	private static final int DEFAULT_MINIMUM_TIME = 5 * 20;
	private static final int DEFAULT_MAXIMUM_TIME = 30 * 20;
	private boolean isMin;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		isMin = parseResult.hasTag("min");

		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public @Nullable Timespan convert(FishHook fishHook) {
		return new Timespan(Timespan.TimePeriod.TICK, isMin ? fishHook.getMinWaitTime() : fishHook.getMaxWaitTime());
	}

	@Override
	public Class<? extends Timespan> getReturnType() {
		return Timespan.class;
	}

	@Override
	protected String getPropertyName() {
		return (isMin ? "minimum" : "maximum") + " waiting time";
	}

	@Override
	public @Nullable Class<?>[] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case DELETE, REMOVE_ALL -> null;
			default -> CollectionUtils.array(Timespan.class);
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		if (delta[0] == null || !(delta[0] instanceof Timespan timespan))
			return;

		int ticks = mode == ChangeMode.RESET ?
			(isMin ? DEFAULT_MINIMUM_TIME : DEFAULT_MAXIMUM_TIME) :
			(int) timespan.getAs(Timespan.TimePeriod.TICK);

		switch (mode) {
			case ADD -> setWaitingTime(event, ticks, false);
			case SET, RESET -> setWaitingTime(event, ticks, true);
			case REMOVE -> setWaitingTime(event, -ticks, false);
		}
	}

	private void setWaitingTime(Event event, int value, boolean isSet) {
		for (FishHook hook : getExpr().getArray(event)) {
			if (isMin) {
				int newValue = Math.max((isSet ? 0 : hook.getMinWaitTime()) + value, 0);
				if (hook.getMaxWaitTime() < newValue)
					hook.setMaxWaitTime(newValue);

				hook.setMinWaitTime(newValue);
			} else {
				int newValue = Math.max((isSet ? 0 : hook.getMaxWaitTime()) + value, 0);
				if (hook.getMinWaitTime() > newValue)
					hook.setMinWaitTime(newValue);

				hook.setMaxWaitTime(newValue);
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return (isMin ? "minimum" : "maximum") + " waiting time of " + getExpr().toString(event, debug);
	}

}
