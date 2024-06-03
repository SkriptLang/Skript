package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerFishEvent;
import org.eclipse.jdt.annotation.Nullable;

@Name("FishState")
@Description({"Returns fish state in FishEvent"})
@Since("2.8.9")
public class ExprFishingState extends SimpleExpression<PlayerFishEvent.State> {
	static {
		Skript.registerExpression(ExprFishingState.class, PlayerFishEvent.State.class, ExpressionType.SIMPLE, "[(event|fish[ing])( |-)]state");
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (event == null)
			return "the fishing state";
		return Classes.getDebugMessage(getSingle(event));
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends PlayerFishEvent.State> getReturnType() {
		return PlayerFishEvent.State.class;
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		if (!getParser().isCurrentEvent(PlayerFishEvent.class)) {
			Skript.error("The 'fishing state' expression can only be used in fish event");
			return false;
		}
		return true;
	}

	@Override
	protected @Nullable PlayerFishEvent.State[] get(Event event) {
		if (event instanceof PlayerFishEvent) {
			return new PlayerFishEvent.State[] {((PlayerFishEvent) event).getState()};
		}
		return null;
	}
}
