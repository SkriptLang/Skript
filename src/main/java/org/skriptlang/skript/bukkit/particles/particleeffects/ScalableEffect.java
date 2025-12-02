package org.skriptlang.skript.bukkit.particles.particleeffects;

import com.google.common.base.Function;
import org.bukkit.Particle;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * A particle effect that can be scaled.
 * Currently used for `sweep attack` and `explosion` particles.
 */
public class ScalableEffect extends ParticleEffect {

	private double scale;
	private final ScalingFunction scalingFunction;

	/**
	 * Enum representing different scaling functions for scalable particles.
	 */
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

	/**
	 * Gets the appropriate scaling function for the given particle.
	 *
	 * @param particle The particle type
	 * @return The scaling function
	 * @throws IllegalArgumentException if the particle is not scalable
	 */
	private ScalingFunction getScalingFunction(@NotNull Particle particle) {
		return switch (particle) {
			case SWEEP_ATTACK -> ScalingFunction.SWEEP;
			case EXPLOSION -> ScalingFunction.EXPLOSION;
			default -> throw new IllegalArgumentException("Particle " + particle.name() + " is not a scalable effect.");
		};
	}

	/**
	 * Internal constructor.
	 * Use {@link ParticleEffect#of(Particle)} instead.
	 * @param particle The particle type
	 */
	@ApiStatus.Internal
	public ScalableEffect(Particle particle) {
		super(particle);
		this.scale = 1.0f;
		this.scalingFunction = getScalingFunction(particle);
	}

	/**
	 * Checks if the effect will use the offset as scale.
	 * The scale is only applied if the count is 0.
	 * @return true if the effect will use the scale, false otherwise
	 */
	public boolean hasScale() {
		return this.count() == 0;
	}

	/**
	 * Sets the scale of the particles by setting the offset and count.
	 * This will set the count to 0 to ensure the scale is applied.
	 * @param scale the scale value
	 * @return this effect for chaining
	 */
	public ParticleEffect scale(double scale) {
		this.scale = scale;
		count(0);
		offset(scalingFunction.apply(scale), 0, 0);
		return this;
	}

	/**
	 * Gets the current scale of the particles.
	 * @return the scale value
	 */
	public double scale() {
		return scale;
	}

	/**
	 * @return a copy of this scalable effect
	 */
	@Override
	public ScalableEffect copy() {
		ScalableEffect copy = (ScalableEffect) super.copy();
		copy.scale = this.scale;
		return copy;
	}

}
