package org.skriptlang.skript.bukkit.fishing.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.config.Node;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;
import org.bukkit.entity.FishHook;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerFishEvent;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.log.runtime.SyntaxRuntimeErrorProducer;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Fishing Wait Time")
@Description({
	"Returns the minimum and/or maximum waiting time of the fishing hook. ",
	"Default minimum value is 5 seconds and maximum is 30 seconds, before lure is applied."
})
@Examples({
	"on fishing line cast:",
		"\tset min waiting time to 10 seconds",
		"\tset max waiting time to 20 seconds",
})
@Events("Fishing")
@Since("2.10")
public class ExprFishingWaitTime extends SimpleExpression<Timespan> implements SyntaxRuntimeErrorProducer {

	private static final int DEFAULT_MINIMUM_TICKS = 5 * 20;
	private static final int DEFAULT_MAXIMUM_TICKS = 30 * 20;

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.EXPRESSION, SyntaxInfo.Expression
			.builder(ExprFishingWaitTime.class, Timespan.class)
			.priority(EventValueExpression.DEFAULT_PRIORITY)
			.addPattern("(min:min[imum]|max[imum]) fish[ing] wait[ing] time")
			.build()
		);
	}

	private Node node;
	private String expr;
	private boolean isMin;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!getParser().isCurrentEvent(PlayerFishEvent.class)) {
			Skript.error("The 'fishing wait time' expression can only be used in a fishing event.");
			return false;
		}
		node = getParser().getNode();
		expr = parseResult.expr;
		isMin = parseResult.hasTag("min");
		return true;
	}

	@Override
	protected Timespan @Nullable [] get(Event event) {
		if (event instanceof PlayerFishEvent fishEvent) {
			if (isMin) {
				return new Timespan[]{new Timespan(Timespan.TimePeriod.TICK, fishEvent.getHook().getMinWaitTime())};
			}
			return new Timespan[]{new Timespan(Timespan.TimePeriod.TICK, fishEvent.getHook().getMaxWaitTime())};
		} else {
			error("The 'fishing wait time' expression can only be used in a fishing event.", expr);
			return null;
		}
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
		if (!(event instanceof PlayerFishEvent fishEvent)) {
			error("The 'fishing wait time' expression can only be used in a fishing event.", expr);
			return;
		}

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
	public Node getNode() {
		return node;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return (isMin ? "minimum" : "maximum") + " fishing waiting time";
	}

}
