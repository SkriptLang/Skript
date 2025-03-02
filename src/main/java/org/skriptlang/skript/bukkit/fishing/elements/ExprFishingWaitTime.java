package org.skriptlang.skript.bukkit.fishing.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;
import org.bukkit.entity.FishHook;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerFishEvent;
import org.jetbrains.annotations.Nullable;

@Name("Fishing Wait Time")
@Description({
	"Returns the minimum and/or maximum waiting time of the fishing hook. ",
	"Default minimum value is 5 seconds and maximum is 30 seconds, before lure is applied."
})
@Examples({
	"on fishing line cast:",
		"\tset min fish waiting time to 10 seconds",
		"\tset max fishing waiting time to 20 seconds",
})
@Events("Fishing")
@Since("2.10")
public class ExprFishingWaitTime extends SimpleExpression<Timespan> {

	private static final int DEFAULT_MINIMUM_TICKS = 5 * 20;
	private static final int DEFAULT_MAXIMUM_TICKS = 30 * 20;

	static {
		Skript.registerExpression(ExprFishingWaitTime.class, Timespan.class, ExpressionType.EVENT,
			"(min:min[imum]|max[imum]) fish[ing] wait[ing] time");
	}

	private boolean isMin;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern,
						Kleenean isDelayed, ParseResult parseResult) {
		if (!getParser().isCurrentEvent(PlayerFishEvent.class)) {
			Skript.error("The 'fishing wait time' expression can only be used in a fishing event.");
			return false;
		}

		isMin = parseResult.hasTag("min");
		return true;
	}

	@Override
	protected Timespan @Nullable [] get(Event event) {
		if (!(event instanceof PlayerFishEvent fishEvent))
			return null;

		if (isMin) {
			return new Timespan[]{new Timespan(Timespan.TimePeriod.TICK, fishEvent.getHook().getMinWaitTime())};
		}
		return new Timespan[]{new Timespan(Timespan.TimePeriod.TICK, fishEvent.getHook().getMaxWaitTime())};
	}

	@Override
	public @Nullable Class<?>[] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case ADD, REMOVE, SET, RESET -> new Class[]{Timespan.class};
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		if (!(event instanceof PlayerFishEvent fishEvent))
			return;

		FishHook hook = fishEvent.getHook();

		int ticks = mode == ChangeMode.RESET ?
			(isMin ? DEFAULT_MINIMUM_TICKS : DEFAULT_MAXIMUM_TICKS) :
			(int) ((Timespan) delta[0]).getAs(Timespan.TimePeriod.TICK);

		switch (mode) {
			case SET, RESET -> {
				if (isMin) {
					hook.setMinWaitTime(Math.max(0, ticks));
				} else {
					hook.setMaxWaitTime(Math.max(0, ticks));
				}
			}
			case ADD -> {
				if (isMin) {
					hook.setMinWaitTime(Math.max(0, hook.getMinWaitTime() + ticks));
				} else {
					hook.setMaxWaitTime(Math.max(0, hook.getMaxWaitTime() + ticks));
				}
			}
			case REMOVE -> {
				if (isMin) {
					hook.setMinWaitTime(Math.max(0, hook.getMinWaitTime() - ticks));
				} else {
					hook.setMaxWaitTime(Math.max(0, hook.getMaxWaitTime() - ticks));
				}
			}
		}
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends Timespan> getReturnType() {
		return Timespan.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return (isMin ? "minimum" : "maximum") + " fishing waiting time";
	}

}
