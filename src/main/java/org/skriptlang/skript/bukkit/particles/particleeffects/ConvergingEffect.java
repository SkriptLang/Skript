package org.skriptlang.skript.bukkit.particles.particleeffects;

import org.bukkit.Particle;
import org.jetbrains.annotations.ApiStatus;

public class ConvergingEffect extends ParticleEffect {
	@ApiStatus.Internal
	public ConvergingEffect(Particle particle) {
		super(particle);
	}

	@Override
	public ConvergingEffect clone() {
		return (ConvergingEffect) super.clone();
	}

}
