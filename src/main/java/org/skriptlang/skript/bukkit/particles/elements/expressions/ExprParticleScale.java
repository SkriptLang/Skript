package org.skriptlang.skript.bukkit.particles.elements.expressions;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.particles.particleeffects.ScalableEffect;

public class ExprParticleScale extends SimplePropertyExpression<ScalableEffect, Number> {

	static {
		register(ExprParticleScale.class, Number.class, "scale [value]", "scalableparticles");
	}

	@Override
	public @Nullable Number convert(ScalableEffect from) {
		return from.scale();
	}

	@Override
	public Class<?> @Nullable [] acceptChange(Changer.ChangeMode mode) {
		return switch (mode) {
			case SET, ADD, REMOVE, RESET -> new Class[]{Number.class};
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, Changer.ChangeMode mode) {
		ScalableEffect[] scalableEffect = getExpr().getArray(event);
		if (scalableEffect == null) return;
		double scaleDelta = 1;
		if (mode != Changer.ChangeMode.RESET) {
			assert delta != null;
			scaleDelta = ((Number) delta[0]).doubleValue();
		}

		switch (mode) {
			case REMOVE:
				scaleDelta = -scaleDelta;
				// fallthrough
			case ADD:
				for (ScalableEffect effect : scalableEffect)
					effect.scale(effect.scale() + scaleDelta);
				break;
			case SET, RESET:
				for (ScalableEffect effect : scalableEffect)
					effect.scale(scaleDelta);
				break;
		}
	}

	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}

	@Override
	protected String getPropertyName() {
		return "scale";
	}

}
