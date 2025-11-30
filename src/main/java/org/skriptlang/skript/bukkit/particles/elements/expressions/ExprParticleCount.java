package org.skriptlang.skript.bukkit.particles.elements.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.particles.particleeffects.ParticleEffect;

public class ExprParticleCount extends SimplePropertyExpression<ParticleEffect, Number> {

	static {
		register(ExprParticleCount.class, Number.class, "particle count", "particles");
	}

	@Override
	public Number convert(ParticleEffect from) {
		return from.count();
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
		int countDelta = 0;
		if (mode != ChangeMode.RESET) {
			assert delta != null;
			if (delta[0] == null) return;
			countDelta = ((Number) delta[0]).intValue();
		}

		switch (mode) {
			case REMOVE:
				countDelta = -countDelta;
				// fallthrough
			case ADD:
				for (ParticleEffect effect : particleEffect)
					effect.count(Math.clamp(effect.count() + countDelta, 0, 1000));
				break;
			case SET:
				for (ParticleEffect effect : particleEffect)
					effect.count(Math.clamp(countDelta, 0, 1000)); // Limit count to 1000 to prevent unintended crashing
				break;
			case RESET:
				for (ParticleEffect effect : particleEffect)
					effect.count(0);
				break;
		}
	}

	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}

	@Override
	protected String getPropertyName() {
		return "particle count";
	}

}
