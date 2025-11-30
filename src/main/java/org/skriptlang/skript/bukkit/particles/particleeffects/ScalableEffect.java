package org.skriptlang.skript.bukkit.particles.particleeffects;

import com.destroystokyo.paper.ParticleBuilder;
import com.google.common.base.Function;
import org.bukkit.Particle;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

public class ScalableEffect extends ParticleEffect {

	private double scale;
	private final ScalingFunction scalingFunction;

	// Sweep: scale = 1.0 - offsetX * 0.5 -> offsetX = (1.0 - scale) * 2.0
	// Explosion: scale = 2.0 * (1.0 - offsetX * 0.5) -> offsetX = (2.0 - scale)
	private enum ScalingFunction {
		SWEEP(scale -> 2 - (2 * scale)),
		EXPLOSION(scale -> 2 - scale);

		private final Function<Double, Double> scaleToOffsetX;

		ScalingFunction(Function<Double, Double> scalingFunction) {
			this.scaleToOffsetX = scalingFunction;
		}

		public double apply(double scale) {
			return scaleToOffsetX.apply(scale);
		}
	}

	private ScalingFunction getScalingFunction(@NotNull Particle particle) {
		return switch (particle) {
			case SWEEP_ATTACK -> ScalingFunction.SWEEP;
			case EXPLOSION -> ScalingFunction.EXPLOSION;
			default -> throw new IllegalArgumentException("Particle " + particle.name() + " is not a scalable effect.");
		};
	}

	@ApiStatus.Internal
	public ScalableEffect(Particle particle) {
		super(particle);
		this.scale = 1.0f;
		this.scalingFunction = getScalingFunction(particle);
	}

	public boolean hasScale() {
		return this.count() == 0;
	}

	public ParticleEffect scale(double scale) {
		this.scale = scale;
		count(0);
		offset(scalingFunction.apply(scale), 0, 0);
		return this;
	}

	public double scale() {
		return scale;
	}

	@Override
	public ParticleBuilder clone() {
		ScalableEffect clone = (ScalableEffect) super.clone();
		clone.scale = this.scale;
		return clone;
	}
}
