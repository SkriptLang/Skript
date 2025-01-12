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
public class ExprFishingBiteTime extends SimpleExpression<Timespan> implements SyntaxRuntimeErrorProducer {

	public static void register(SyntaxRegistry registry) {
		if (Skript.methodExists(FishHook.class, "getTimeUntilBite"))
			registry.register(SyntaxRegistry.EXPRESSION, SyntaxInfo.Expression
				.builder(ExprFishingBiteTime.class, Timespan.class)
				.addPattern("fish[ing] bit(e|ing) [wait] time")
				.priority(EventValueExpression.DEFAULT_PRIORITY)
				.build()
			);
	}

	private Node node;
	private String expr;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!getParser().isCurrentEvent(PlayerFishEvent.class)) {
			Skript.error("The 'fishing bite time' expression can only be used in a fishing event.");
			return false;
		}
		node = getParser().getNode();
		expr = parseResult.expr;
		return true;
	}

	@Override
	protected Timespan @Nullable [] get(Event event) {
		if (event instanceof PlayerFishEvent fishEvent) {
			return new Timespan[]{new Timespan(Timespan.TimePeriod.TICK, fishEvent.getHook().getTimeUntilBite())};
		} else {
			error("The 'fishing bite time' expression can only be used in a fishing event.", expr);
			return null;
		}
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
		if (!(event instanceof PlayerFishEvent fishEvent)) {
			error("The 'fishing bite time' expression can only be used in a fishing event.", expr);
			return;
		}

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
	public Node getNode() {
		return node;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "fishing bite time";
	}

}
