package org.skriptlang.skript.bukkit.particles.registration;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.ColorRGB;
import ch.njol.skript.util.Timespan;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Vibration;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class DataParticles {
	private static final List<EffectInfo<Particle, ?>> PARTICLE_INFOS = new ArrayList<>();

	private static <F, D> void registerParticle(Particle particle, String pattern, D defaultData, Function<F, D> dataFunction, ToString<D> toStringFunction) {
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

	private static <D> void registerParticle(Particle particle, String pattern, DataSupplier<D> dataSupplier, ToString<D> toStringFunction) {
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

		registerParticle(Particle.EFFECT, "[a[n]] %color% effect particle[s] (of|with) power %number%",
			//<editor-fold desc="spell lambda">
			(event, expressions, parseResult) -> {
				ch.njol.skript.util.Color color = (ch.njol.skript.util.Color) expressions[0].getSingle(event);
				if (color == null)
					color = ColorRGB.fromBukkitColor(org.bukkit.Color.WHITE); // default color if none is provided
				Number power = (Number) expressions[1].getSingle(event);
				if (power == null)
					power = 1.0; // default power if none is provided
				return new Particle.Spell(color.asBukkitColor(), power.floatValue());
			},
			//</editor-fold>
			spell -> Classes.toString(ColorRGB.fromBukkitColor(spell.getColor())) + " effect particle of power " + spell.getPower());

		registerParticle(Particle.ENTITY_EFFECT, "[a[n]] %color% (potion|entity) effect particle[s]", org.bukkit.Color.WHITE,
			color -> ((ch.njol.skript.util.Color) color).asBukkitColor(),
			color -> Classes.toString(ColorRGB.fromBukkitColor(color)) + " potion effect particle");

		registerParticle(Particle.FLASH, "[a[n]] %color% flash particle[s]", org.bukkit.Color.WHITE,
			color -> ((ch.njol.skript.util.Color) color).asBukkitColor(),
			color -> Classes.toString(ColorRGB.fromBukkitColor(color)) + " flash particle");

		registerParticle(Particle.TINTED_LEAVES, "[a[n]] %color% tinted leaves particle[s]", org.bukkit.Color.WHITE,
			color -> ((ch.njol.skript.util.Color) color).asBukkitColor(),
			color -> Classes.toString(ColorRGB.fromBukkitColor(color)) + " tinted leaves particle");

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
			dustOptions -> Classes.toString(ColorRGB.fromBukkitColor(dustOptions.getColor())) + " dust particle of size " + dustOptions.getSize());

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
			dustTransition -> Classes.toString(ColorRGB.fromBukkitColor(dustTransition.getColor())) +
				" dust particle of size " + dustTransition.getSize() +
				" that transitions to " + Classes.toString(ColorRGB.fromBukkitColor(dustTransition.getToColor())));

		// blockdata
		registerParticle(Particle.BLOCK, "[a[n]] %itemtype/blockdata% block particle[s]",
			DataSupplier::getBlockData,
			blockData -> Classes.toString(blockData) + " block particle");

		registerParticle(Particle.BLOCK_CRUMBLE, "[a[n]] %itemtype/blockdata% [block] crumble particle[s]",
			DataSupplier::getBlockData,
			blockData -> Classes.toString(blockData) + " block crumble particle");

		registerParticle(Particle.BLOCK_MARKER, "[a[n]] %itemtype/blockdata% [block] marker particle[s]",
			DataSupplier::getBlockData,
			blockData -> Classes.toString(blockData) + " block marker particle");

		registerParticle(Particle.DUST_PILLAR, "[a[n]] %itemtype/blockdata% dust pillar particle[s]",
			DataSupplier::getBlockData,
			blockData -> Classes.toString(blockData) + " dust pillar particle");

		registerParticle(Particle.FALLING_DUST, "[a] falling %itemtype/blockdata% dust particle[s]",
			DataSupplier::getBlockData,
			blockData -> "falling " + Classes.toString(blockData) + " dust particle");

		registerParticle(Particle.DRAGON_BREATH, "[a] dragon breath particle[s] [of power %-number%]",
			0.5f, input -> input,
			power -> "dragon breath particle of power " + power);

		// misc

		registerParticle(Particle.ITEM, "[an] %itemtype% item particle[s]",
			//<editor-fold desc="item stack data lamba" defaultstate="collapsed">
			(event, expressions, parseResult) -> {
				ItemType itemType = (ItemType) expressions[0].getSingle(event);
				if (itemType == null)
					return new ItemStack(Material.AIR); // default item if none is provided
				return itemType.getRandom();
			}, //</editor-fold>
			itemStack -> Classes.toString(itemStack) + " item particle");

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
			angle -> "sculk charge particle with roll angle " + Math.toDegrees(angle) + " degrees");

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

				Number durationTicks= 20;
				if (expressions[2] != null) {
					Timespan duration = (Timespan) expressions[2].getSingle(event);
					if (duration != null)
						durationTicks = duration.getAs(Timespan.TimePeriod.TICK);
				}

				return new Particle.Trail(targetLocation, bukkitColor, durationTicks.intValue());
			}, //</editor-fold>
			trail -> Classes.toString(ColorRGB.fromBukkitColor(trail.getColor())) +
				" trail particle leading to " + Classes.toString(trail.getTarget()) +
				" over " + trail.getDuration() + " ticks");

		registerParticle(Particle.VIBRATION, "[a] vibration particle moving to[wards] %entity/location% over [a duration of] %timespan%",
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
			vibration -> "vibration particle moving to " +
				(vibration.getDestination() instanceof Vibration.Destination.BlockDestination blockDestination ?
					Classes.toString(blockDestination.getLocation()) :
					Classes.toString(((Vibration.Destination.EntityDestination) vibration.getDestination()).getEntity())
				) +
				" over " + vibration.getArrivalTime() + " ticks");
	}
}
