package org.skriptlang.skript.bukkit.particles.registration;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.util.ColorRGB;
import ch.njol.skript.util.Timespan;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Unmodifiable;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class DataParticles {
	private static final List<EffectInfo<Particle, ?>> PARTICLE_INFOS = new ArrayList<>();

	private static <F, D> void registerParticle(Particle particle, String pattern, D defaultData, Function<F, D> dataFunction, ToString toStringFunction) {
		registerParticle(particle, pattern, (event, expressions, parseResult) -> {
			if (expressions[0] == null)
				return defaultData; // default data if none is provided
			//noinspection unchecked
			D data = dataFunction.apply((F) expressions[0].getSingle(event));
			if (data == null)
				return defaultData; // default data if none is provided
			return data;
		}, toStringFunction);
	}

	private static <D> void registerParticle(Particle particle, String pattern, DataSupplier<D> dataSupplier, ToString toStringFunction) {
		PARTICLE_INFOS.add(new EffectInfo<>(particle, pattern, dataSupplier, toStringFunction));
	}

	public static @Unmodifiable List<EffectInfo<Particle, ?>> getParticleInfos() {
		if (PARTICLE_INFOS.isEmpty()) {
			registerAll();
		}
		return Collections.unmodifiableList(PARTICLE_INFOS);
	}

	private static void registerAll() {

		// colors

		if (Skript.isRunningMinecraft(1, 21, 9)) {
			DataSupplier<Particle.Spell> spellData = //<editor-fold desc="spell lambda">
				(event, expressions, parseResult) -> {
					ch.njol.skript.util.Color color = (ch.njol.skript.util.Color) expressions[0].getSingle(event);
					if (color == null)
						color = ColorRGB.fromBukkitColor(org.bukkit.Color.WHITE); // default color if none is provided
					Number power = (Number) expressions[1].getSingle(event);
					if (power == null)
						power = 1.0; // default power if none is provided
					return new Particle.Spell(color.asBukkitColor(), power.floatValue());
				}; //</editor-fold>

			registerParticle(Particle.EFFECT, "[a[n]] %color% effect particle[s] (of|with) power %number%",
				spellData,
				(exprs, parseResult, builder) -> builder.append(exprs[0], "effect particle of power", exprs[1]));

			registerParticle(Particle.INSTANT_EFFECT, "[a[n]] %color% instant effect particle[s] (of|with) power %number%",
				spellData,
				(exprs, parseResult, builder) -> builder.append(exprs[0], "instant effect particle of power", exprs[1]));

			registerParticle(Particle.FLASH, "[a[n]] %color% flash particle[s]", org.bukkit.Color.WHITE,
				color -> ((ch.njol.skript.util.Color) color).asBukkitColor(),
				(exprs, parseResult, builder) -> builder.append(exprs[0], "flash particle"));

		}

		registerParticle(Particle.ENTITY_EFFECT, "[a[n]] %color% (potion|entity) effect particle[s]", org.bukkit.Color.WHITE,
			color -> ((ch.njol.skript.util.Color) color).asBukkitColor(),
			(exprs, parseResult, builder) -> builder.append(exprs[0], "potion effect particle"));

		if (Skript.isRunningMinecraft(1, 21, 5)) {
			registerParticle(Particle.TINTED_LEAVES, "[a[n]] %color% tinted leaves particle[s]", org.bukkit.Color.WHITE,
				color -> ((ch.njol.skript.util.Color) color).asBukkitColor(),
				(exprs, parseResult, builder) -> builder.append(exprs[0], "tinted leaves particle"));
		}

		registerParticle(Particle.DUST, "[a[n]] %color% dust particle[s] [of size %number%]",
			//<editor-fold desc="dust options lambda" defaultstate="collapsed">
			(event, expressions, parseResult) -> {
				org.bukkit.Color bukkitColor;
				ch.njol.skript.util.Color color = (ch.njol.skript.util.Color) expressions[0].getSingle(event);
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
			}, //</editor-fold>
			(exprs, parseResult, builder) -> builder.append(exprs[0], "dust particle of size", exprs[1]));

		// dust color transition particle
		registerParticle(Particle.DUST_COLOR_TRANSITION, "[a[n]] %color% dust particle[s] [of size %number%] that transitions to %color%",
			//<editor-fold desc="dust color transition options lambda" defaultstate="collapsed">
			(event, expressions, parseResult) -> {
				org.bukkit.Color bukkitColor;
				ch.njol.skript.util.Color color = (ch.njol.skript.util.Color) expressions[0].getSingle(event);
				if (color == null) {
					bukkitColor = org.bukkit.Color.WHITE; // default color if none is provided
				} else {
					bukkitColor = color.asBukkitColor();
				}

				Number size = (Number) expressions[1].getSingle(event);
				if (size == null || size.doubleValue() <= 0) {
					size = 1.0; // default size if none is provided or invalid
				}

				ch.njol.skript.util.Color toColor = (ch.njol.skript.util.Color) expressions[2].getSingle(event);
				org.bukkit.Color bukkitToColor;
				if (toColor == null) {
					bukkitToColor = org.bukkit.Color.WHITE; // default transition color if none is provided
				} else {
					bukkitToColor = toColor.asBukkitColor();
				}

				return new Particle.DustTransition(bukkitColor, bukkitToColor, size.floatValue());
			}, //</editor-fold>
			(exprs, parseResult, builder) -> builder.append(exprs[0], "dust particle of size", exprs[1], "that transitions to", exprs[2]));

		// blockdata
		registerParticle(Particle.BLOCK, "[a[n]] %itemtype/blockdata% block particle[s]",
			DataSupplier::getBlockData,
			(exprs, parseResult, builder) -> builder.append(exprs[0], "block particle"));

		if (Skript.isRunningMinecraft(1, 21, 2)) {
			registerParticle(Particle.BLOCK_CRUMBLE, "[a[n]] %itemtype/blockdata% [block] crumble particle[s]",
				DataSupplier::getBlockData,
				(exprs, parseResult, builder) -> builder.append(exprs[0], "block crumble particle"));
		}

		registerParticle(Particle.BLOCK_MARKER, "[a[n]] %itemtype/blockdata% [block] marker particle[s]",
			DataSupplier::getBlockData,
			(exprs, parseResult, builder) -> builder.append(exprs[0], "block marker particle"));

		registerParticle(Particle.DUST_PILLAR, "[a[n]] %itemtype/blockdata% dust pillar particle[s]",
			DataSupplier::getBlockData,
			(exprs, parseResult, builder) -> builder.append(exprs[0], "dust pillar particle"));

		registerParticle(Particle.FALLING_DUST, "[a] falling %itemtype/blockdata% dust particle[s]",
			DataSupplier::getBlockData,
			(exprs, parseResult, builder) -> builder.append("falling", exprs[0], "dust particle"));

		// misc

		registerParticle(Particle.ITEM, "[an] %itemtype% item particle[s]",
			//<editor-fold desc="item stack data lamba" defaultstate="collapsed">
			(event, expressions, parseResult) -> {
				ItemType itemType = (ItemType) expressions[0].getSingle(event);
				if (itemType == null)
					return new ItemStack(Material.AIR); // default item if none is provided
				return itemType.getRandom();
			}, //</editor-fold>
			(exprs, parseResult, builder) -> builder.append(exprs[0], "item particle"));

		registerParticle(Particle.SCULK_CHARGE, "[a] sculk charge particle[s] [with [a] roll angle [of] %-number%]",
			//<editor-fold desc="charge lambda" defaultstate="collapsed">
			(event, expressions, parseResult) -> {
				if (expressions[0] == null)
					return 0.0f; // default angle if none is provided
				Number angle = (Number) expressions[0].getSingle(event);
				if (angle == null)
					return 0.0f; // default angle if none is provided
				return (float) Math.toRadians(angle.floatValue());
			}, //</editor-fold>
			(exprs, parseResult, builder) -> builder.append("sculk charge particle)")
													.appendIf(exprs[0] != null, "with a roll angle of", exprs[0]));

		registerParticle(Particle.SHRIEK, "[a] shriek particle[s] [delayed by %-timespan%]", 0,
			timespan -> ((Timespan) timespan).getAs(Timespan.TimePeriod.TICK),
			(exprs, parseResult, builder) -> builder.append("shriek particle")
													.appendIf(exprs[0] != null, "delayed by", exprs[0]));

		registerParticle(Particle.VIBRATION, "[a] vibration particle moving to[wards] %entity/location% [over [a duration of] %-timespan%]",
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
			}, //</editor-fold>
			(exprs, parseResult, builder) -> builder.append("vibration particle moving towards", exprs[0])
													.appendIf(exprs[1] != null, "over", exprs[1]));

		if (Skript.isRunningMinecraft(1, 21, 4)) {
			registerParticle(Particle.TRAIL, "[a[n]] %color% trail particle moving to[wards] %location% [over [a duration of] %-timespan%]",
				//<editor-fold desc="trail lambda" defaultstate="collapsed">
				(event, expressions, parseResult) -> {
					org.bukkit.Color bukkitColor;
					ch.njol.skript.util.Color color = (ch.njol.skript.util.Color) expressions[0].getSingle(event);
					if (color == null) {
						bukkitColor = org.bukkit.Color.WHITE; // default color if none is provided
					} else {
						bukkitColor = color.asBukkitColor();
					}

					Location targetLocation = (Location) expressions[1].getSingle(event);
					if (targetLocation == null)
						return null;

					Number durationTicks = 20;
					if (expressions[2] != null) {
						Timespan duration = (Timespan) expressions[2].getSingle(event);
						if (duration != null)
							durationTicks = duration.getAs(Timespan.TimePeriod.TICK);
					}

					return new Particle.Trail(targetLocation, bukkitColor, durationTicks.intValue());
				}, //</editor-fold>
				(exprs, parseResult, builder) -> builder.append(exprs[0], "trail particle leading to", exprs[1])
													.appendIf(exprs[2] != null, "over", exprs[2]));
		} else if (Skript.isRunningMinecraft(1, 21, 2)) {
			// need to get Particle.TargetColor via reflection (1.21.2 - 1.21.3)
			//<editor-fold desc="reflection for Particle.TargetColor" defaultstate="collapsed">
			Class<?>[] classes = Particle.class.getClasses();
			Class<?> targetColorClass = null;
			for (Class<?> cls : classes) {
				if (cls.getSimpleName().equals("TargetColor")) {
					targetColorClass = cls;
					break;
				}
			}
			if (targetColorClass != null) {
				try {
					var constructor = targetColorClass.getDeclaredConstructor(Location.class, Color.class);
					registerParticle(Particle.TRAIL, "[a[n]] %color% trail particle moving to[wards] %location%",
						//<editor-fold desc="trail lambda" defaultstate="collapsed">
						(event, expressions, parseResult) -> {
							org.bukkit.Color bukkitColor;
							ch.njol.skript.util.Color color = (ch.njol.skript.util.Color) expressions[0].getSingle(event);
							if (color == null) {
								bukkitColor = org.bukkit.Color.WHITE; // default color if none is provided
							} else {
								bukkitColor = color.asBukkitColor();
							}

							Location targetLocation = (Location) expressions[1].getSingle(event);
							if (targetLocation == null)
								return null;

							try {
								return constructor.newInstance(targetLocation, bukkitColor);
							} catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
								throw new RuntimeException(e);
							}
						}, //</editor-fold>
						(exprs, parseResult, builder) -> builder.append(exprs[0], "trail particle moving to", exprs[1]));
				} catch (NoSuchMethodException e) {
					throw new RuntimeException(e);
				}
			}
			//</editor-fold>
		}

		if (Skript.isRunningMinecraft(1, 21, 9)) {
			registerParticle(Particle.DRAGON_BREATH, "[a] dragon breath particle[s] [of power %-number%]",
				0.5f, input -> input,
				(exprs, parseResult, builder) -> builder.append("dragon breath particle")
														.appendIf(exprs[0] != null, "of power", exprs[0]));
		}
	}
}
