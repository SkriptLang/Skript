package org.skriptlang.skript.bukkit.particles.particleeffects;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.EnumParser;
import ch.njol.skript.lang.Debuggable;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import com.destroystokyo.paper.ParticleBuilder;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.joml.Vector3i;
import org.skriptlang.skript.bukkit.particles.ParticleUtils;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A wrapper around Paper's ParticleBuilder to provide additional functionality
 * and a more fluent API for spawning particle effects. Categories of particles
 * with special behaviors may extend this class.
 * <br>
 * Particle behavior depends a lot on whether the count is zero or not. If count is
 * zero, the offset and extra parameters are used to define a normal distribution
 * for randomly offsetting particle positions. If count is greater than zero, the offset
 * may be used for a number of special behaviors depending on the particle type.
 * For example, {@link DirectionalEffect}s will use the offset as a velocity vector, multiplied
 * by the extra parameter. {@link ScalableEffect}s will use the offset to determine scale.
 */
public class ParticleEffect extends ParticleBuilder implements Debuggable {

	/**
	 * Creates the appropriate ParticleEffect subclass based on the particle type.
	 * @param particle The particle type
	 * @return The appropriate ParticleEffect instance
	 */
	@Contract("_ -> new")
	public static @NotNull ParticleEffect of(Particle particle) {
		if (ParticleUtils.isConverging(particle)) {
			return new ConvergingEffect(particle);
		} else if (ParticleUtils.usesVelocity(particle)) {
			return new DirectionalEffect(particle);
		} else if (ParticleUtils.isScalable(particle)) {
			return new ScalableEffect(particle);
		}
		return new ParticleEffect(particle);
	}

	/**
	 * Creates the appropriate ParticleEffect with the properties of the provided {@link ParticleBuilder}
	 * @param builder The builder to copy values from
	 * @return The appropriate ParticleEffect instance with the properties copied from the builder
	 */
	@Contract("_ -> new")
	public static @NotNull ParticleEffect of(@NotNull ParticleBuilder builder) {
		Particle particle = builder.particle();
		ParticleEffect effect = ParticleEffect.of(particle);
		effect.count(builder.count());
		effect.data(builder.data());
		Location loc;
		if ((loc = builder.location()) != null)
			effect.location(loc);
		effect.offset(builder.offsetX(), builder.offsetY(), builder.offsetZ());
		effect.extra(builder.extra());
		effect.force(builder.force());
		effect.receivers(builder.receivers());
		effect.source(builder.source());
		return effect;
	}

	// Skript parsing dependencies

	/**
	 * Parser for particles without data
	 */
	private static final ParticleParser ENUM_PARSER = new ParticleParser();

	private static final Pattern LEADING_NUMBER_PATTERN = Pattern.compile("(\\d+) (.+)");

	/**
	 * Parses a particle effect from a string input. Prints errors if the particle requires data.
	 * @param input the input string
	 * @param context the parse context
	 * @return the parsed ParticleEffect, or null if parsing failed
	 */
	public static @Nullable ParticleEffect parse(String input, ParseContext context) {
		Matcher matcher = LEADING_NUMBER_PATTERN.matcher(input);
		int count = 1;
		if (matcher.matches()) {
			count = Math.clamp(Integer.parseInt(matcher.group(1)), 0, 16_384); // drawing more than the maximum display count of 16,384 is likely unintended and can crash users.
			input = matcher.group(2);
		}
		Particle particle = ENUM_PARSER.parse(input.toLowerCase(Locale.ENGLISH), context);
		if (particle == null)
			return null;
		if (particle.getDataType() != Void.class) {
			Skript.error("The " + Classes.toString(particle) + " requires data and cannot be parsed directly. Use the Particle With Data expression instead.");
			return null;
		}
		return ParticleEffect.of(particle).count(count);
	}

	/**
	 * Converts a Particle to its string representation.
	 * @param particle the particle
	 * @param flags parsing flags
	 * @return the string representation
	 */
	public static String toString(Particle particle, int flags) {
		return ENUM_PARSER.toString(particle, flags);
	}

	/**
	 * Gets all particle names that do not require data.
	 * @return array of particle names
	 */
	public static String @NotNull [] getAllNamesWithoutData() {
		return ENUM_PARSER.getPatternsWithoutData();
	}

	// Instance code

	/**
	 * Internal constructor.
	 * Use {@link ParticleEffect#of(Particle)} instead.
	 * @param particle The particle type
	 */
	protected ParticleEffect(Particle particle) {
		super(particle);
	}

	@Override
	public ParticleEffect spawn() {
		if (dataType() != Void.class && !dataType().isInstance(data()))
			return this; // data is not compatible with the particle type
		return (ParticleEffect) super.spawn();
	}

	/**
	 * Ease of use method to spawn at a location. Modifies the location value of this effect.
	 * @param location the location to spawn at.
	 * @return This effect, with the location value modified.
	 */
	public ParticleEffect spawn(Location location) {
		this.location(location)
			.spawn();
		return this;
	}

	/**
	 * @return The offset of this particle as a JOML vector
	 */
	public Vector3d offset() {
		return new Vector3d(offsetX(), offsetY(), offsetZ());
	}

	/**
	 * Set the offset from a JOML vector
	 * @param offset the new offset
	 * @return This effect, with the offset modified.
	 */
	public ParticleEffect offset(@NotNull Vector3d offset) {
		return (ParticleEffect) super.offset(offset.x(), offset.y(), offset.z());
	}

	/**
	 * Set the receiver radii from a JOML vector
	 * @param radii the new radii to check for receivers in
	 * @return This effect, with the receivers modified.
	 */
	public ParticleEffect receivers(@NotNull Vector3i radii) {
		return (ParticleEffect) super.receivers(radii.x(), radii.y(), radii.z());
	}

	/**
	 * Set the receiver radii from a JOML vector. Values are truncated to ints.
	 * @param radii the new radii to check for receivers in
	 * @return This effect, with the receivers modified.
	 */
	public ParticleEffect receivers(@NotNull Vector3d radii) {
		return (ParticleEffect) super.receivers((int) radii.x(), (int) radii.y(), (int) radii.z());
	}

	/**
	 * @return Whether this effect will use its offset value as a normal distribution (count > 0)
	 */
	public boolean isUsingNormalDistribution() {
		return count() != 0;
	}

	/**
	 * An alias for the offset. Prefer using this when working with particles that have counts greater than 0.
	 * When {@link #isUsingNormalDistribution()} is false, the returned value will not be the distribution and
	 * will instead depend on the particle's specific behavior when count = 0.
	 * @return the distribution of this particle. The distribution is defined as 3 normal distributions in the x/y/z axes,
	 * 	       with the returned vector containing the standard deviations. The mean will always be 0.
	 */
	public Vector3d distribution() {
		return offset();
	}

	/**
	 * Sets the distribution for this particle. The distribution is defined as 3 normal distributions in the x/y/z axes,
	 * with the provided vector containing the standard deviations. The mean will always be 0.
	 * Sets the count to 1 if it was 0.
	 * @param distribution The new standard deviations to use.
	 */
	public ParticleEffect distribution(Vector3d distribution) {
		if (!isUsingNormalDistribution()) {
			count(1);
		}
		return offset(distribution);
	}

	@Override
	public <T> ParticleEffect data(@Nullable T data) {
		if (data != null && !dataType().isInstance(data)) {
			return this; // do not allow incompatible data types
		}
		return (ParticleEffect) super.data(data);
	}

	/**
	 * Helper method to check if this effect accepts the provided data. Depends on the current particle.
	 * @param data The data to check.
	 * @return Whether the data is of the right class.
	 */
	public boolean acceptsData(@Nullable Object data) {
		if (data == null) return true;
		return dataType().isInstance(data);
	}

	/**
	 * Alias for {@code this.particle().getDataType()}
	 * @return The data type of the current particle.
	 */
	public Class<?> dataType() {
		return particle().getDataType();
	}

	/**
	 * @return a copy of this effect.
	 */
	public ParticleEffect copy() {
		return (ParticleEffect) this.clone();
	}

	@Override
	public String toString() {
		return toString(null, false);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return ENUM_PARSER.toString(particle(), 0);
	}

	/**
	 * A custom {@link EnumParser} that excludes particles with data from being parsed directly.
	 */
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

	//<editor-fold desc="Fluent overrides" defaultstate="collapsed">

	@Override
	public ParticleEffect particle(Particle particle) {
		return (ParticleEffect) super.particle(particle);
	}

	@Override
	public ParticleEffect allPlayers() {
		return (ParticleEffect) super.allPlayers();
	}

	@Override
	public ParticleEffect receivers(@Nullable List<Player> receivers) {
		return (ParticleEffect) super.receivers(receivers);
	}

	@Override
	public ParticleEffect receivers(@Nullable Collection<Player> receivers) {
		return (ParticleEffect) super.receivers(receivers);
	}

	@Override
	public ParticleEffect receivers(Player @Nullable ... receivers) {
		return (ParticleEffect) super.receivers(receivers);
	}

	@Override
	public ParticleEffect receivers(int radius) {
		return (ParticleEffect) super.receivers(radius);
	}

	@Override
	public ParticleEffect receivers(int radius, boolean byDistance) {
		return (ParticleEffect) super.receivers(radius, byDistance);
	}

	@Override
	public ParticleEffect receivers(int xzRadius, int yRadius) {
		return (ParticleEffect) super.receivers(xzRadius, yRadius);
	}

	@Override
	public ParticleEffect receivers(int xzRadius, int yRadius, boolean byDistance) {
		return (ParticleEffect) super.receivers(xzRadius, yRadius, byDistance);
	}

	@Override
	public ParticleEffect receivers(int xRadius, int yRadius, int zRadius) {
		return (ParticleEffect) super.receivers(xRadius, yRadius, zRadius);
	}

	@Override
	public ParticleEffect source(@Nullable Player source) {
		return (ParticleEffect) super.source(source);
	}

	@Override
	public ParticleEffect location(Location location) {
		return (ParticleEffect) super.location(location);
	}

	@Override
	public ParticleEffect location(World world, double x, double y, double z) {
		return (ParticleEffect) super.location(world, x, y, z);
	}

	@Override
	public ParticleEffect count(int count) {
		return (ParticleEffect) super.count(count);
	}

	@Override
	public ParticleEffect offset(double offsetX, double offsetY, double offsetZ) {
		return (ParticleEffect) super.offset(offsetX, offsetY, offsetZ);
	}

	@Override
	public ParticleEffect extra(double extra) {
		return (ParticleEffect) super.extra(extra);
	}

	@Override
	public ParticleEffect force(boolean force) {
		return (ParticleEffect) super.force(force);
	}
	//</editor-fold>
}
