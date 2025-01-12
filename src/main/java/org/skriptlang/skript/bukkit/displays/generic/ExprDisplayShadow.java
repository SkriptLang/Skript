package org.skriptlang.skript.bukkit.displays.generic;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Display;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Display Shadow Radius/Strength")
@Description("Returns or changes the shadow radius/strength of <a href='classes.html#display'>displays</a>.")
@Examples("set shadow radius of the last spawned text display to 1.75")
@Since("2.10")
public class ExprDisplayShadow extends SimplePropertyExpression<Display, Float> {

	public static void register(SyntaxRegistry registry) {
		registerDefault(registry, ExprDisplayShadow.class, Float.class, "shadow (:radius|strength)", "displays");
	}

	private boolean radius;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		radius = parseResult.hasTag("radius");
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public @Nullable Float convert(Display display) {
		return radius ? display.getShadowRadius() : display.getShadowStrength();
	}

	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case ADD, SET, REMOVE -> CollectionUtils.array(Number.class);
			case RESET -> CollectionUtils.array();
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		Display[] displays = getExpr().getArray(event);
		float change = delta == null ? 0F : ((Number) delta[0]).floatValue();
		if (delta != null && (Float.isInfinite(change) || Float.isNaN(change))) {
			error("Cannot change the shadow " + (radius ? "radius" : "strength") + " to an infinite or NaN value.", delta[0].toString());
			return;
		}

		switch (mode) {
			case REMOVE:
				change = -change;
				//$FALL-THROUGH$
			case ADD:
				for (Display display : displays) {
					if (radius) {
						float value = Math.max(0F, display.getShadowRadius() + change);
						if (Float.isInfinite(value))
							continue;
						display.setShadowRadius(value);
					} else {
						float value = Math.max(0F, display.getShadowStrength() + change);
						if (Float.isInfinite(value))
							continue;
						display.setShadowStrength(value);
					}
				}
				break;
			case RESET:
				if (!radius)
					change = 1; // default strength is 1
			case SET:
				change = Math.max(0F, change);
				for (Display display : displays) {
					if (radius) {
						display.setShadowRadius(change);
					} else {
						display.setShadowStrength(change);
					}
				}
				break;
		}
	}

	@Override
	public Class<? extends Float> getReturnType() {
		return Float.class;
	}

	@Override
	protected String getPropertyName() {
		return radius ? "radius" : "strength";
	}

}
