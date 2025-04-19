package org.skriptlang.skript.bukkit.particles;


import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

/**
 * A class to hold particle metadata prior to spawning
 */
public class ParticleEffect {

	/**
	 * The base {@link Particle} to use. This determines the properties and what data this {@link ParticleEffect} can accept.
	 */
	private Particle particle;

	/**
	 * This determines how many particles to spawn with the given properties. If set to 0, {@link ParticleEffect#offset} may
	 * be used to determine things like colour or velocity, rather than the area where the particles spawn.
	 */
	private int count;

	/**
	 * This, by default, determines a bounding box around the spawn location in which particles are randomly offset.
	 * Dimensions are multiplied by roughly 8, meaning an offset of (1, 1, 1) results in particles spawning in an
	 * 8x8x8 cuboid centered on the spawn location.
	 * Particles are distributed following a Gaussian distribution, clustering towards the center.
	 * <br>
	 * When {@link ParticleEffect#count} is 0, however, this may instead act as a velocity vector, an RGB colour,
	 * or determine the colour of a note particle.
	 * See <a href=https://minecraft.wiki/w/Commands/particle>the wiki on the particle command</a> for more information.
	 */
	private Vector offset;

	/**
	 * This, by default, determines the speed at which a particle moves. It must be positive.
	 * <br>
	 * When {@link ParticleEffect#count} is 0, this instead acts as a multiplier to the velocity provided by {@link ParticleEffect#offset},
	 * or if {@link ParticleEffect#particle} is {@link Particle#SPELL_MOB_AMBIENT} or {@link Particle#SPELL_MOB}, then
	 * this acts as an exponent to the RGB value provided by {@link ParticleEffect#offset}.
	 */
	private float extra;

	/**
	 * This field contains extra data that some particles require. For example, {@link Particle#REDSTONE} requires {@link org.bukkit.Particle.DustOptions}
	 * to determine its size and colour.
	 */
	@Nullable
	private Object data;

	ParticleEffect(Particle particle) {
		this.particle = particle;
		this.count = 1;
		this.extra = 0;
		this.offset = new Vector(0,0,0);
	}

	public void draw(Location location, boolean force) {
		World world = location.getWorld();
		if (world == null)
			return;
		world.spawnParticle(particle, location, count, offset.getX(), offset.getY(), offset.getZ(), extra, data, force);
	}

	public void drawForPlayer(Location location, Player player, boolean force) {
		player.spawnParticle(particle, location, count, offset.getX(), offset.getY(), offset.getZ(), extra, data, force);
	}

	// TODO: Add parent interface for ParticleEffect, PlayerEffect, EntityEffect? Would make spawning easier, maybe.
	// TODO: add getters, setters, maybe builder class? Add spawn method.
}
