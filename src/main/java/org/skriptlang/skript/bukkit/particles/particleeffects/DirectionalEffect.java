package org.skriptlang.skript.bukkit.particles.particleeffects;


import org.bukkit.Particle;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Vector3d;

/**
 * A particle effect where particles have a specific direction of travel/velocity.
 * Allows getting and setting the velocity of the particles. Setting the velocity will set the count to 0.
 * Velocity is only applied if the count is 0.
 */
public class DirectionalEffect extends ParticleEffect {

	/**
	 * Internal constructor.
	 * Use {@link ParticleEffect#of(Particle)} instead.
	 * @param particle The particle type
	 */
	@ApiStatus.Internal
	public DirectionalEffect(Particle particle) {
		super(particle);
	}

	/**
	 * Checks if the effect will use the offset as velocity.
	 * Velocity is only applied if the count is 0.
	 * @return true if the effect will use the offset as velocity, false otherwise
	 */
	public boolean hasVelocity() {
		return count() == 0;
	}

	/**
	 * Alias for {@link #offset()} when the effect is directional.
	 * Prefer using this method when dealing with directional effects that have count = 0.
	 * @return the velocity vector
	 */
	public Vector3d velocity() {
		return offset();
	}

	/**
	 * Sets the velocity of the particles by setting the offset and count.
	 * This will set the count to 0 to ensure the velocity is applied.
	 * @param velocity the velocity vector
	 * @return this effect for chaining
	 */
	public DirectionalEffect velocity(Vector3d velocity) {
		count(0);
		offset(velocity);
		return this;
	}

	/**
	 * @return a copy of this directional effect
	 */
	@Override
	public DirectionalEffect copy() {
		return (DirectionalEffect) super.copy();
	}

}
