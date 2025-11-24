package org.skriptlang.skript.bukkit.particles.elements.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Color;
import ch.njol.skript.util.Patterns;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;
import org.bukkit.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.particles.ParticleEffect;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ExprParticleWithData extends SimpleExpression<ParticleEffect> {

	private static final Patterns<ParticlePattern> PATTERNS;

	private static final List<ParticlePattern> REGISTERED_PATTERNS = new ArrayList<>();

	private static <T> void registerParticle(Particle particle, String pattern, T defaultData) {
		registerParticle(particle, pattern, (event, expressions, parseResult) -> {
			if (expressions[0] == null)
				return defaultData; // default data if none is provided
			//noinspection unchecked
			T data = (T) expressions[0].getSingle(event);
			if (data == null)
				return defaultData; // default data if none is provided
			return data;
		});
	}

	private static void registerParticle(Particle particle, String pattern, GetData<?> getData) {
		REGISTERED_PATTERNS.add(new ParticlePattern(particle, pattern, getData));
	}

	static {
		registerParticle(Particle.EFFECT, "[a[n]] %color% effect particle[s] (of|with) power %number%",
			//<editor-fold desc="spell lambda">
			(event, expressions, parseResult) -> {
				Color color = (Color) expressions[0].getSingle(event);
				if (color == null)
					return org.bukkit.Color.WHITE; // default color if none is provided
				Number power = (Number) expressions[1].getSingle(event);
				if (power == null)
					power = 1.0; // default power if none is provided
				return new Particle.Spell(color.asBukkitColor(), power.floatValue());
			}
			//</editor-fold>
		);

		registerParticle(Particle.ENTITY_EFFECT, "[a[n]] %color% (potion|entity) effect particle[s]", org.bukkit.Color.WHITE);
		registerParticle(Particle.FLASH, "[a[n]] %color% flash particle[s]", org.bukkit.Color.WHITE);
		registerParticle(Particle.TINTED_LEAVES, "[a[n]] %color% tinted leaves particle[s]", org.bukkit.Color.WHITE);

		registerParticle(Particle.DUST, "[a[n]] %color% dust particle[s] [of size %number%]",
			//<editor-fold desc="dust options lambda">
			(event, expressions, parseResult) -> {
				org.bukkit.Color bukkitColor;
				Color color = (Color) expressions[0].getSingle(event);
				if (color == null) {
					bukkitColor = org.bukkit.Color.WHITE; // default color if none is provided
				} else {
					bukkitColor = color.asBukkitColor();
				}

				Number size = (Number) expressions[1].getSingle(event);
				if (size == null || size.doubleValue() <= 0) {
					size = 1.0; // default size if none is provided or invalid
				}

				return new Particle.DustOptions(bukkitColor, size.floatValue());
			} //</editor-fold>
		);

		// dust color transition particle
		registerParticle(Particle.DUST_COLOR_TRANSITION, "[a[n]] %color% dust particle[s] [of size %number%] that transitions to %color%",
			//<editor-fold desc="dust color transition options lambda">
			(event, expressions, parseResult) -> {
				org.bukkit.Color bukkitColor;
				Color color = (Color) expressions[0].getSingle(event);
				if (color == null) {
					bukkitColor = org.bukkit.Color.WHITE; // default color if none is provided
				} else {
					bukkitColor = color.asBukkitColor();
				}

				Number size = (Number) expressions[1].getSingle(event);
				if (size == null || size.doubleValue() <= 0) {
					size = 1.0; // default size if none is provided or invalid
				}

				Color toColor = (Color) expressions[2].getSingle(event);
				org.bukkit.Color bukkitToColor;
				if (toColor == null) {
					bukkitToColor = org.bukkit.Color.WHITE; // default transition color if none is provided
				} else {
					bukkitToColor = toColor.asBukkitColor();
				}

				return new Particle.DustTransition(bukkitColor, bukkitToColor, size.floatValue());
			} //</editor-fold>
		);

		registerParticle(Particle.ITEM, "[an] %itemtype% item particle[s]",
			//<editor-fold desc="item stack data lamba">
			(event, expressions, parseResult) -> {
				ItemType itemType = (ItemType) expressions[0].getSingle(event);
				if (itemType == null)
					return new ItemStack(Material.AIR); // default item if none is provided
				return itemType.getRandom();
			} //</editor-fold>
		);

		GetData<BlockData> blockdataData = (event, expressions, parseResult) -> {
			//<editor-fold desc="blockdataData lambda">
			Object object = expressions[0].getSingle(event);
			if (object instanceof ItemType itemType) {
				ItemStack random = itemType.getRandom();
				return Bukkit.createBlockData(random != null ? random.getType() : itemType.getMaterial());
			} else if (object instanceof BlockData blockData) {
				return blockData;
			}
			return Bukkit.createBlockData(Material.AIR); // default block if none is provided
			//</editor-fold>
		};
		registerParticle(Particle.BLOCK, "[a[n]] %itemtype/blockdata% block particle[s]", blockdataData);
		registerParticle(Particle.BLOCK_CRUMBLE, "[a[n]] %itemtype/blockdata% [block] crumble particle[s]", blockdataData);
		registerParticle(Particle.BLOCK_MARKER, "[a[n]] %itemtype/blockdata% [block] marker particle[s]", blockdataData);
		registerParticle(Particle.DUST_PILLAR, "[a[n]] %itemtype/blockdata% dust pillar particle[s]", blockdataData);
		registerParticle(Particle.FALLING_DUST, "[a] falling %itemtype/blockdata% dust particle[s]", blockdataData);

		registerParticle(Particle.DRAGON_BREATH, "[a] dragon breath particle[s] [of power %-number%]", 0.5f);

		registerParticle(Particle.SCULK_CHARGE, "[a] sculk charge particle[s] [with angle %-number%]",
			//<editor-fold desc="charge lambda">
			(event, expressions, parseResult) -> {
				if (expressions[0] == null)
					return 0; // default angle if none is provided
				Number angle = (Number) expressions[0].getSingle(event);
				if (angle == null)
					return 0; // default angle if none is provided
				return (float) Math.toRadians(angle.floatValue());
			} //</editor-fold>
		);

		registerParticle(Particle.TRAIL, "[a[n]] %color% trail particle leading to %location% [(for|with [a] duration of) %-timespan%]",
			//<editor-fold desc="trail lambda">
			(event, expressions, parseResult) -> {
				org.bukkit.Color bukkitColor;
				Color color = (Color) expressions[0].getSingle(event);
				if (color == null) {
					bukkitColor = org.bukkit.Color.WHITE; // default color if none is provided
				} else {
					bukkitColor = color.asBukkitColor();
				}

				Location targetLocation = (Location) expressions[1].getSingle(event);
				if (targetLocation == null)
					return null;

				Number durationTicks;
				if (expressions[2] == null) {
					durationTicks = 20; // default duration of 1 second if none is provided
				} else {
					Number duration = (Number) expressions[2].getSingle(event);
					// default duration of 1 second if none is provided
					durationTicks = Objects.requireNonNullElse(duration, 20);
				}

				return new Particle.Trail(targetLocation, bukkitColor, durationTicks.intValue());
			} //</editor-fold>
		);

		registerParticle(Particle.VIBRATION, "[a] vibration particle moving to %entity/location% over %timespan%",
			//<editor-fold desc="vibration lambda">
			(event, expressions, parseResult) -> {
				Object target = expressions[0].getSingle(event);
				Vibration.Destination destination;
				if (target instanceof Location location) {
					destination = new Vibration.Destination.BlockDestination(location);
				} else if (target instanceof Entity entity) {
					destination = new Vibration.Destination.EntityDestination(entity);
				} else {
					return null;
				}

				int duration;
				Timespan timespan = (Timespan) expressions[1].getSingle(event);
				if (timespan == null) {
					duration = 20; // default duration of 1 second if none is provided
				} else {
					duration = (int) timespan.getAs(Timespan.TimePeriod.TICK);
				}
				return new Vibration(destination, duration);
			} //</editor-fold>
		);

		// create Patterns object
		Object[][] patterns = new Object[REGISTERED_PATTERNS.size()][2];
		int i = 0;
		for (ParticlePattern particlePattern : REGISTERED_PATTERNS) {
			patterns[i][0] = particlePattern.pattern;
			patterns[i][1] = particlePattern;
			i++;
		}
		PATTERNS = new Patterns<>(patterns);

		Skript.registerExpression(ExprParticleWithData.class, ParticleEffect.class, ExpressionType.COMBINED, PATTERNS.getPatterns());
	}

	private ParseResult parseResult;
	private Expression<?>[] expressions;
	private Particle particle;
	private GetData<?> getData;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		this.parseResult = parseResult;
		this.expressions = expressions;
		ParticlePattern particlePattern = PATTERNS.getInfo(matchedPattern);
		if (particlePattern == null)
			return false;
		this.particle = particlePattern.particle;
		this.getData = particlePattern.getData;
		return true;
	}

	@Override
	protected ParticleEffect @Nullable [] get(Event event) {
		Object data = getData.getData(event, expressions, parseResult);
		if (data == null) {
			error("Could not obtain required data for particle " + particle.name());
			return null;
		}
		ParticleEffect effect = new ParticleEffect(particle);
		effect.setData(data);
		return new ParticleEffect[] {effect};
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends ParticleEffect> getReturnType() {
		return ParticleEffect.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "particle with data";
	}

	/**
	 * A helper class to store a particle and its pattern.
	 *
	 * @param particle The particle
	 * @param pattern The pattern
	 * @param getData The function to get the data from the event
	 */
	private record ParticlePattern(Particle particle, String pattern, GetData<?> getData) {}

	/**
	 * A functional interface to get the data from the event.
	 *
	 * @param <T> The type of the data
	 */
	@FunctionalInterface
	interface GetData<T> {
		/**
		 * Get the data from the event.
		 *
		 * @param event       The event to evaluate with
		 * @param expressions Any expressions that are used in the pattern
		 * @param parseResult The parse result from parsing
		 * @return The data to use for the effect, or null if the required data could not be obtained
		 */
		@Nullable T getData(Event event, Expression<?>[] expressions, ParseResult parseResult);
	}

}
