package org.skriptlang.skript.bukkit.particles.elements.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.particles.particleeffects.ParticleEffect;

public class ExprParticleSpeed extends SimplePropertyExpression<ParticleEffect, Number> {

	static {
		register(ExprParticleSpeed.class, Number.class, "speed [value]", "particles");
	}

	@Override
	public @Nullable Number convert(ParticleEffect from) {
		return from.extra();
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, ADD, REMOVE, RESET -> new Class[]{Number.class};
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		ParticleEffect[] particleEffect = getExpr().getArray(event);
		if (particleEffect == null) return;
		double extraDelta = 0;
		if (mode != ChangeMode.RESET) {
			assert delta != null;
			if (delta[0] == null) return;
			extraDelta = ((Number) delta[0]).doubleValue();
		}

		switch (mode) {
			case REMOVE:
				extraDelta = -extraDelta;
				// fallthrough
			case ADD:
				for (ParticleEffect effect : particleEffect)
					effect.extra(effect.extra() + extraDelta);
				break;
			case SET, RESET:
				for (ParticleEffect effect : particleEffect)
					effect.extra(extraDelta);
				break;
		}
	}

	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}

	@Override
	protected String getPropertyName() {
		return "speed";
	}

}
