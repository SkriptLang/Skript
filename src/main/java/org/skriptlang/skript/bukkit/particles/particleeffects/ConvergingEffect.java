package org.skriptlang.skript.bukkit.particles.particleeffects;

import org.bukkit.Particle;
import org.jetbrains.annotations.ApiStatus;
import org.skriptlang.skript.bukkit.particles.ParticleUtils;

/**
 * A particle effect where particles converge towards a point.
 * Currently used only for `all converging particles`
 * @see ParticleUtils#isConverging(Particle)
 */
public class ConvergingEffect extends ParticleEffect {

	/**
	 * Internal constructor.
	 * Use {@link ParticleEffect#of(Particle)} instead.
	 * @param particle The particle type
	 */
	@ApiStatus.Internal
	public ConvergingEffect(Particle particle) {
		super(particle);
	}

	/**
	 * @return a copy of this converging effect
	 */
	@Override
	public ConvergingEffect copy() {
		return (ConvergingEffect) super.copy();
	}

}
