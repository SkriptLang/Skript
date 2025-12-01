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

	// Skript parsing dependencies

	private static final ParticleParser ENUM_PARSER = new ParticleParser();

	public static ParticleEffect parse(String input, ParseContext context) {
		Particle particle = ENUM_PARSER.parse(input.toLowerCase(Locale.ENGLISH), context);
		if (particle == null)
			return null;
		if (particle.getDataType() != Void.class) {
			Skript.error("The " + Classes.toString(particle) + " requires data and cannot be parsed directly. Use the Particle With Data expression instead.");
			return null;
		}
		return ParticleEffect.of(particle);
	}

	public static String toString(Particle particle, int flags) {
		return ENUM_PARSER.toString(particle, flags);
	}

	public static String[] getAllNamesWithoutData() {
		return ENUM_PARSER.getPatternsWithoutData();
	}

	// Instance code

	public ParticleEffect(ParticleBuilder builder) {
		super(builder.particle());
		this.count(builder.count());
		this.data(builder.data());
		Location loc;
		if ((loc = builder.location()) != null)
			this.location(loc);
		this.offset(builder.offsetX(), builder.offsetY(), builder.offsetZ());
		this.extra(builder.extra());
		this.force(builder.force());
		this.receivers(builder.receivers());
		this.source(builder.source());
	}

	protected ParticleEffect(Particle particle) {
		super(particle);
	}

	@Override
	public ParticleEffect spawn() {
		if (dataType() != Void.class && !dataType().isInstance(data()))
			return this; // data is not compatible with the particle type
		return (ParticleEffect) super.spawn();
	}

	public ParticleEffect spawn(Location location) {
		this.location(location)
			.spawn();
		return this;
	}

	public Vector3d offset() {
		return new Vector3d(offsetX(), offsetY(), offsetZ());
	}

	public ParticleEffect offset(Vector3d offset) {
		return (ParticleEffect) super.offset(offset.x(), offset.y(), offset.z());
	}

	public ParticleEffect receivers(Vector3i radii) {
		return (ParticleEffect) super.receivers(radii.x(), radii.y(), radii.z());
	}

	public ParticleEffect receivers(Vector3d radii) {
		return (ParticleEffect) super.receivers((int) radii.x(), (int) radii.y(), (int) radii.z());
	}

	public boolean isUsingNormalDistribution() {
		return count() != 0;
	}

	public Vector3d getDistribution() {
		return isUsingNormalDistribution() ? offset() : null;
	}

	public void setDistribution(Vector3d distribution) {
		if (!isUsingNormalDistribution()) {
			count(1);
		}
		offset(distribution);
	}

	@Override
	public <T> ParticleEffect data(@Nullable T data) {
		if (data != null && !dataType().isInstance(data)) {
			return this; // do not allow incompatible data types
		}
		return (ParticleEffect) super.data(data);
	}

	public boolean acceptsData(@Nullable Object data) {
		if (data == null) return true;
		return dataType().isInstance(data);
	}

	public Class<?> dataType() {
		return particle().getDataType();
	}

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
