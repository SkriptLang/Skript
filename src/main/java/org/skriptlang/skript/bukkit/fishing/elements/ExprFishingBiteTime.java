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

@Name("Fishing Bite Time")
@Description({
	"Returns the time it takes a fish to bite the fishing hook, after it started approaching the hook.",
	"May return a timespan of 0 seconds. If modifying the value, it should be at least 1 tick.",
})
@Examples({
	"on fish approach:",
	"\tset fishing bite time to 5 seconds",
})
@RequiredPlugins("Paper 1.20.6")
@Events("Fishing")
@Since("2.10")
public class ExprFishingBiteTime extends SimpleExpression<Timespan> {

	static {
		if (Skript.methodExists(FishHook.class, "getTimeUntilBite"))
			Skript.registerExpression(ExprFishingBiteTime.class, Timespan.class, ExpressionType.EVENT,
				"fish[ing] bit(e|ing) [wait] time");
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern,
						Kleenean isDelayed, ParseResult parseResult) {
		if (!getParser().isCurrentEvent(PlayerFishEvent.class)) {
			Skript.error("The 'fishing bite time' expression can only be used in a fishing event.");
			return false;
		}
		return true;
	}

	@Override
	protected Timespan @Nullable [] get(Event event) {
		if (!(event instanceof PlayerFishEvent fishEvent))
			return null;

		return new Timespan[]{new Timespan(Timespan.TimePeriod.TICK, fishEvent.getHook().getTimeUntilBite())};
	}

	@Override
	public @Nullable Class<?>[] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case ADD, REMOVE, SET -> new Class[]{Timespan.class};
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		if (!(event instanceof PlayerFishEvent fishEvent))
			return;

		FishHook hook = fishEvent.getHook();

		int ticks = (int) ((Timespan) delta[0]).getAs(Timespan.TimePeriod.TICK);

		switch (mode) {
			case SET -> hook.setTimeUntilBite(Math.max(1, ticks));
			case ADD -> hook.setTimeUntilBite(Math.max(1, hook.getTimeUntilBite() + ticks));
			case REMOVE -> hook.setTimeUntilBite(Math.max(1, hook.getTimeUntilBite() - ticks));
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
		return "fishing bite time";
	}

}
