package org.skriptlang.skript.bukkit.particles.particleeffects;


import org.bukkit.Particle;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Vector3d;

public class DirectionalEffect extends ParticleEffect {
	@ApiStatus.Internal
	public DirectionalEffect(Particle particle) {
		super(particle);
	}

	public boolean hasVelocity() {
		return count() == 0;
	}

	public Vector3d velocity() {
		return offset();
	}

	public DirectionalEffect velocity(Vector3d velocity) {
		count(0);
		offset(velocity);
		return this;
	}

	@Override
	public DirectionalEffect clone() {
		return (DirectionalEffect) super.clone();
	}

}
