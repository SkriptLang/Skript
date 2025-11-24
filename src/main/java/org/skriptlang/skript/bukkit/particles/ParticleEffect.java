package org.skriptlang.skript.bukkit.particles;


import ch.njol.skript.Skript;
import ch.njol.skript.classes.EnumParser;
import ch.njol.skript.lang.Debuggable;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.StringMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Map;

/**
 * A class to hold particle metadata prior to spawning
 */
public class ParticleEffect implements Debuggable {

	private static final ParticleParser ENUM_PARSER = new ParticleParser();

	public static ParticleEffect parse(String input, ParseContext context) {
		Particle particle = ENUM_PARSER.parse(input.toLowerCase(Locale.ENGLISH), context);
		if (particle == null)
			return null;
		if (particle.getDataType() != Void.class) {
			Skript.error("The " + Classes.toString(particle) + " requires data and cannot be parsed directly. Use the Particle With Data expression instead.");
			return null;
		}
		return new ParticleEffect(particle);
	}

	public static String toString(Particle particle, int flags) {
		return ENUM_PARSER.toString(particle, flags);
	}

	public static String[] getAllNamesWithoutData() {
		return ENUM_PARSER.getPatternsWithoutData();
	}

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
	 * or if {@link ParticleEffect#particle} is {@link Particle#ENTITY_EFFECT}, then
	 * this acts as an exponent to the RGB value provided by {@link ParticleEffect#offset}.
	 */
	private float extra;

	/**
	 * This determines whether the particle should be visible to players at long range.
	 */
	private boolean force;

	/**
	 * This field contains extra data that some particles require. For example, {@link Particle#DUST} requires {@link org.bukkit.Particle.DustOptions}
	 * to determine its size and colour.
	 */
	@Nullable
	private Object data;

	public ParticleEffect(Particle particle) {
		this.particle = particle;
		this.count = 1;
		this.extra = 0;
		this.offset = new Vector(0,0,0);
		this.force = false;
	}

	public void draw(Location location, boolean force) {
		if (this.particle.getDataType() != Void.class && !this.particle.getDataType().isInstance(data))
			return; // data is not compatible with the particle type
		World world = location.getWorld();
		if (world == null)
			return;
		world.spawnParticle(particle, location, count, offset.getX(), offset.getY(), offset.getZ(), extra, data, force);
	}

	public void drawForPlayer(Location location, Player player, boolean force) {
		if (this.particle.getDataType() != Void.class && !this.particle.getDataType().isInstance(data))
			return; // data is not compatible with the particle type
		player.spawnParticle(particle, location, count, offset.getX(), offset.getY(), offset.getZ(), extra, data, force);
	}

	public Particle getParticle() {
		return particle;
	}

	public ParticleEffect setParticle(Particle particle) {
		this.particle = particle;
		return this;
	}

	public int getCount() {
		return count;
	}

	public ParticleEffect setCount(int count) {
		this.count = count;
		return this;
	}

	public Vector getOffset() {
		return offset;
	}

	public ParticleEffect setOffset(Vector offset) {
		this.offset = offset;
		return this;
	}

	public float getExtra() {
		return extra;
	}

	public ParticleEffect setExtra(float extra) {
		this.extra = extra;
		return this;
	}

	public @Nullable Object getData() {
		return data;
	}

	public ParticleEffect setData(@Nullable Object data) {
		if (data != null && !this.particle.getDataType().isInstance(data))
			throw new IllegalArgumentException("Data type " + data.getClass().getName() + " is not compatible with particle type " + this.particle.name() + " (expected " + this.particle.getDataType().getName() + ")");
		this.data = data;
		return this;
	}

	public boolean isForce() {
		return force;
	}

	public ParticleEffect setForce(boolean force) {
		this.force = force;
		return this;
	}

	@Override
	public String toString() {
		return toString(null, false);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return ENUM_PARSER.toString(particle, 0) + (data != null ? " with data" + Classes.toString(data, debug ? StringMode.DEBUG : StringMode.MESSAGE) : "");
	}

	private static class ParticleParser extends EnumParser<Particle> {

		public ParticleParser() {
			super(Particle.class, "particle");
		}

		public String @NotNull [] getPatternsWithoutData() {
			return parseMap.entrySet().stream()
					.filter(entry -> {
						Particle particle = entry.getValue();
						return particle.getDataType() == Void.class;
					})
					.map(Map.Entry::getKey)
					.toArray(String[]::new);
		}

	}
}
