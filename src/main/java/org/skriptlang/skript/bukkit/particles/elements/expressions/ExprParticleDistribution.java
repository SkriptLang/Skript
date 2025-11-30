package org.skriptlang.skript.bukkit.particles.elements.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.skriptlang.skript.bukkit.particles.particleeffects.ParticleEffect;

public class ExprParticleDistribution extends SimplePropertyExpression<ParticleEffect, Vector> {

	static {
		register(ExprParticleDistribution.class, Vector.class, "particle distribution", "particles");
	}

	@Override
	public @Nullable Vector convert(ParticleEffect from) {
		return from.isUsingNormalDistribution() ? Vector.fromJOML(from.getDistribution()) : null;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, RESET -> new Class[]{Vector.class};
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		ParticleEffect[] particleEffect = getExpr().getArray(event);
		if (particleEffect == null) return;

		Vector3d newVector = null;
		if (mode != ChangeMode.RESET) {
			assert delta != null;
			if (delta[0] == null) return;
			newVector = ((Vector) delta[0]).toVector3d();
		}

		switch (mode) {
			case SET:
				for (ParticleEffect effect : particleEffect)
					effect.setDistribution(newVector);
				break;
			case RESET:
				for (ParticleEffect effect : particleEffect)
					effect.setDistribution(new Vector3d(0,0,0));
				break;
		}
	}

	@Override
	public Class<? extends Vector> getReturnType() {
		return Vector.class;
	}

	@Override
	protected String getPropertyName() {
		return "particle distribution";
	}

}
