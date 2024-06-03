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
import java.util.NoSuchElementException;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerFishEvent;
import org.eclipse.jdt.annotation.Nullable;

@Name("FishState")
@Description({"Returns fish state in FishEvent"})
@Since("2.8.9")
public class ExprFishingState extends SimpleExpression<PlayerFishEvent.State> {
	private int pattern;
	static {
		Skript.registerExpression(ExprFishingState.class, PlayerFishEvent.State.class, ExpressionType.SIMPLE,
			"1¦([(event|fish[ing])( |-)]state)|2¦fishing|3¦caught fish|4¦caught entity|5¦in ground|6¦reel in|7¦bite|8¦lured");
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
		pattern = parseResult.mark;
		if (pattern == 8) {
			try {
				PlayerFishEvent.State test = PlayerFishEvent.State.LURED;
			} catch (NoSuchElementException e) {
				Skript.error("The fishing state 'lured' is not supported in your server platform. It need PaperAPI");
				return false;
			}
		}
		return true;
	}

	@Override
	protected PlayerFishEvent.@Nullable State[] get(Event event) {
		switch (pattern) {
			case 2:
				return new PlayerFishEvent.State[]{PlayerFishEvent.State.FISHING};
			case 3:
				return new PlayerFishEvent.State[]{PlayerFishEvent.State.CAUGHT_FISH};
			case 4:
				return new PlayerFishEvent.State[]{PlayerFishEvent.State.CAUGHT_ENTITY};
			case 5:
				return new PlayerFishEvent.State[]{PlayerFishEvent.State.IN_GROUND};
			case 6:
				return new PlayerFishEvent.State[]{PlayerFishEvent.State.REEL_IN};
			case 7:
				return new PlayerFishEvent.State[]{PlayerFishEvent.State.BITE};
			case 8:
				return new PlayerFishEvent.State[]{PlayerFishEvent.State.LURED};
		}
		if (event instanceof PlayerFishEvent) {
			return new PlayerFishEvent.State[] {((PlayerFishEvent) event).getState()};
		}
		return null;
	}
}
