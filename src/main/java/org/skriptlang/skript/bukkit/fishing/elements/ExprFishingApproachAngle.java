package org.skriptlang.skript.bukkit.fishing.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.entity.FishHook;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerFishEvent;
import org.jetbrains.annotations.Nullable;

@Name("Fishing Approach Angle")
@Description({
	"Returns the angle at which the fish will approach the fishing hook, after the wait time.",
	"The angle is in degrees, with 0 being positive Z, 90 being negative X, 180 being negative Z, and 270 being positive X.",
	"By default, returns a value between 0 and 360 degrees."
})
@Examples({
	"on fish approach:",
		"\tif any:",
			"\t\tmaximum fishing approach angle is bigger than 300.5 degrees",
			"\t\tmin fishing approach angle is smaller than 59.5 degrees",
		"\tthen:",
			"\t\tcancel event"
})
@Events("Fishing")
@Since("2.10")
public class ExprFishingApproachAngle extends SimpleExpression<Float> {

	private static final float DEFAULT_MINIMUM_DEGREES = 0;
	private static final float DEFAULT_MAXIMUM_DEGREES = 360;

	static {
		Skript.registerExpression(ExprFishingApproachAngle.class, Float.class, ExpressionType.EVENT,
			"(min:min[imum]|max[imum]) fish[ing] approach[ing] angle");
	}

	private boolean isMin;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern,
						Kleenean isDelayed, ParseResult parseResult) {
		if (!getParser().isCurrentEvent(PlayerFishEvent.class)) {
			Skript.error("The 'fishing approach angle' expression can only be used in a fishing event.");
			return false;
		}

		isMin = parseResult.hasTag("min");
		return true;
	}

	@Override
	protected Float @Nullable [] get(Event event) {
		if (!(event instanceof PlayerFishEvent fishEvent))
			return null;

		if (isMin) {
			return new Float[]{fishEvent.getHook().getMinLureAngle()};
		} else {
			return new Float[]{fishEvent.getHook().getMaxLureAngle()};
		}
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, RESET, ADD, REMOVE -> new Class[]{Float.class};
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		if (!(event instanceof PlayerFishEvent fishEvent))
			return;

		FishHook hook = fishEvent.getHook();

		float angle = mode == ChangeMode.RESET ?
			(isMin ? DEFAULT_MINIMUM_DEGREES : DEFAULT_MAXIMUM_DEGREES) :
			(Float) delta[0];

		switch (mode) {
			case SET, RESET -> {
				if (isMin) {
					hook.setMinLureAngle(clamp(angle));
				} else {
					hook.setMaxLureAngle(clamp(angle));
				}
			}
			case ADD -> {
				if (isMin) {
					hook.setMinLureAngle(clamp(hook.getMinLureAngle() + angle));
				} else {
					hook.setMaxLureAngle(clamp(hook.getMaxLureAngle() + angle));
				}
			}
			case REMOVE -> {
				if (isMin) {
					hook.setMinLureAngle(clamp(hook.getMinLureAngle() - angle));
				} else {
					hook.setMaxLureAngle(clamp(hook.getMaxLureAngle() - angle));
				}
			}
		}
	}

	private float clamp(float value) {
		return Math.min(Math.max(value, 0f), 360f);
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends Float> getReturnType() {
		return Float.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return (isMin ? "minimum" : "maximum") + " fishing approach angle";
	}

}
