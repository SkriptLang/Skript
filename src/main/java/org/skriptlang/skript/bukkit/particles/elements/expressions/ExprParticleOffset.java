package org.skriptlang.skript.bukkit.particles.elements.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.skriptlang.skript.bukkit.particles.particleeffects.ParticleEffect;

public class ExprParticleOffset extends SimplePropertyExpression<ParticleEffect, Vector> {

	static {
		register(ExprParticleOffset.class, Vector.class, "particle offset", "particles");
	}

	@Override
	public @Nullable Vector convert(ParticleEffect from) {
		return Vector.fromJOML(from.offset());
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, ADD, REMOVE, RESET -> new Class[]{Vector.class};
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		ParticleEffect[] particleEffect = getExpr().getArray(event);
		if (particleEffect == null) return;
		Vector3d vectorDelta = null;
		if (mode != ChangeMode.RESET) {
			assert delta != null;
			if (delta[0] == null) return;
			vectorDelta = ((Vector) delta[0]).toVector3d();
		}

		switch (mode) {
			case REMOVE:
				vectorDelta.mul(-1);
				// fallthrough
			case ADD:
				for (ParticleEffect effect : particleEffect)
					effect.offset(vectorDelta.add(effect.offset()));
				break;
			case SET:
				for (ParticleEffect effect : particleEffect)
					effect.offset(vectorDelta);
				break;
			case RESET:
				for (ParticleEffect effect : particleEffect)
					effect.offset(0, 0, 0);
				break;
		}
	}

	@Override
	public Class<? extends Vector> getReturnType() {
		return Vector.class;
	}

	@Override
	protected String getPropertyName() {
		return "particle offset";
	}

}
