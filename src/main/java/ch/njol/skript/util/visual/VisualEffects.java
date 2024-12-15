/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.util.visual;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.bukkitutil.ItemUtils;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SyntaxElementInfo;
import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.Noun;
import ch.njol.skript.util.Color;
import ch.njol.skript.util.ColorRGB;
import ch.njol.skript.util.Direction;
import ch.njol.skript.util.SkriptColor;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.variables.Variables;
import ch.njol.util.StringUtils;
import ch.njol.util.coll.iterator.SingleItemIterator;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class VisualEffects {

	private static final boolean NEW_EFFECT_DATA = Skript.classExists("org.bukkit.block.data.BlockData");

	private static final Map<String, Consumer<VisualEffectType>> effectTypeModifiers = new HashMap<>();
	private static SyntaxElementInfo<VisualEffect> elementInfo;
	private static VisualEffectType[] visualEffectTypes;

	static {
		Variables.yggdrasil.registerSingleClass(VisualEffectType.class, "VisualEffect.NewType");
		Variables.yggdrasil.registerSingleClass(Effect.class, "Bukkit_Effect");
		Variables.yggdrasil.registerSingleClass(EntityEffect.class, "Bukkit_EntityEffect");
	}

	@Nullable
	public static VisualEffect parse(String s) {
		if (elementInfo == null)
			return null;
		return SkriptParser.parseStatic(
			Noun.stripIndefiniteArticle(s), new SingleItemIterator<>(elementInfo), null);
	}

	public static VisualEffectType get(int i) {
		return visualEffectTypes[i];
	}

	public static String getAllNames() {
		List<Noun> names = new ArrayList<>();
		for (VisualEffectType visualEffectType : visualEffectTypes) {
			names.add(visualEffectType.getName());
		}
		return StringUtils.join(names, ", ");
	}

	private static void generateTypes() {
		List<VisualEffectType> types = new ArrayList<>();
		Stream.of(Effect.class, EntityEffect.class, Particle.class)
				.map(Class::getEnumConstants)
				.flatMap(Arrays::stream)
				.map(VisualEffectType::of)
				.filter(Objects::nonNull)
				.forEach(types::add);

		for (VisualEffectType type : types) {
			String id = type.getId();
			if (effectTypeModifiers.containsKey(id))
				effectTypeModifiers.get(id).accept(type);
		}

		visualEffectTypes = types.toArray(new VisualEffectType[0]);
		String[] patterns = new String[visualEffectTypes.length];
		for (int i = 0; i < visualEffectTypes.length; i++) {
			patterns[i] = visualEffectTypes[i].getPattern();
		}
		elementInfo = new SyntaxElementInfo<>(patterns, VisualEffect.class, VisualEffect.class.getName());
	}

	private static void registerColorable(String id) {
		effectTypeModifiers.put(id, VisualEffectType::setColorable);
	}

	private static void registerDataSupplier(String id, BiFunction<Object, Location, Object> dataSupplier) {
		Consumer<VisualEffectType> consumer = type -> type.withData(dataSupplier);
		if (effectTypeModifiers.containsKey(id)) {
			consumer = effectTypeModifiers.get(id).andThen(consumer);
		}
		effectTypeModifiers.put(id, consumer);
	}

	// only applies to some older versions where ITEM_CRACK exists
	private static final boolean IS_ITEM_CRACK_MATERIAL =
			Skript.fieldExists(Particle.class, "ITEM_CRACK")
			&& Particle.valueOf("ITEM_CRACK").getDataType() == Material.class;

	static {
		Language.addListener(() -> {
			if (visualEffectTypes != null) // Already registered
				return;

			// Data suppliers
			registerDataSupplier("Effect.POTION_BREAK", (raw, location) ->
				new PotionEffect(raw == null ? PotionEffectType.SPEED : (PotionEffectType) raw, 1, 0));
			registerDataSupplier("Effect.SMOKE", (raw, location) -> {
				if (raw == null)
					return BlockFace.SELF;
				return Direction.getFacing(((Direction) raw).getDirection(location), false);
			});

			// Useful: https://minecraft.wiki/w/Particle_format

			/*
			 * Particles with BlockData DataType
			 */
			final BiFunction<Object, Location, Object> blockDataSupplier = (raw, location) -> {
				if (raw instanceof Object[]) { // workaround for modern pattern since it contains a choice
					Object[] data = (Object[]) raw;
					raw = data[0] != null ? data[0] : data[1];
				}
				if (raw == null)
					return Bukkit.createBlockData(Material.AIR);
				if (raw instanceof ItemType) {
					ItemType type = (ItemType) raw;
					ItemStack random = type.getRandom();
					return Bukkit.createBlockData(random != null ? random.getType() : type.getMaterial());
				}
				return raw;
			};
			registerDataSupplier("Particle.BLOCK", blockDataSupplier);
			registerDataSupplier("Particle.BLOCK_CRACK", blockDataSupplier);
			registerDataSupplier("Particle.BLOCK_DUST", blockDataSupplier);

			registerDataSupplier("Particle.BLOCK_MARKER", blockDataSupplier);

			registerDataSupplier("Particle.DUST_PILLAR", blockDataSupplier);

			registerDataSupplier("Particle.FALLING_DUST", blockDataSupplier);

			/*
			 * Particles with DustOptions DataType
			 */
			final Color defaultColor = SkriptColor.LIGHT_RED;
			final BiFunction<Object, Location, Object> dustOptionsSupplier = (raw, location) -> {
				Object[] data = (Object[]) raw;
				Color color = data[0] != null ? (Color) data[0] : defaultColor;
				float size = data[1] != null ? (Float) data[1] : 1;
				return new Particle.DustOptions(color.asBukkitColor(), size);
			};
			registerDataSupplier("Particle.DUST", dustOptionsSupplier);
			registerDataSupplier("Particle.REDSTONE", dustOptionsSupplier);

			/*
			 * Particles with Color DataType
			 */
			registerDataSupplier("Particle.ENTITY_EFFECT", (raw, location) -> {
				if (raw == null)
					return defaultColor.asBukkitColor();
				return ((Color) raw).asBukkitColor();
			});
			final BiFunction<Object, Location, Object> oldColorSupplier = (raw, location) -> {
				Color color = raw != null ? (Color) raw : defaultColor;
				return new ParticleOption(color, 1);
			};
			registerColorable("Particle.SPELL_MOB");
			registerDataSupplier("Particle.SPELL_MOB", oldColorSupplier);
			registerColorable("Particle.SPELL_MOB_AMBIENT");
			registerDataSupplier("Particle.SPELL_MOB_AMBIENT", oldColorSupplier);

			final BiFunction<Object, Location, Object> itemStackSupplier = (raw, location) -> {
				ItemStack itemStack = null;
				if (raw instanceof ItemType)
					itemStack = ((ItemType) raw).getRandom();
				if (itemStack == null || ItemUtils.isAir(itemStack.getType())) // item crack air is not allowed
					itemStack = new ItemStack(Material.IRON_SWORD);
				if (IS_ITEM_CRACK_MATERIAL)
					return itemStack.getType();
				return itemStack;
			};
			registerDataSupplier("Particle.ITEM", itemStackSupplier);
			registerDataSupplier("Particle.ITEM_CRACK", itemStackSupplier);

			/*
			 * Particles with other DataTypes
			 */
			registerDataSupplier("Particle.DUST_COLOR_TRANSITION", (raw, location) -> {
				Object[] data = (Object[]) raw;
				Color fromColor = data[0] != null ? (Color) data[0] : defaultColor;
				Color toColor = data[1] != null ? (Color) data[1] : defaultColor;
				float size = data[2] != null ? (Float) data[2] : 1;
				return new Particle.DustTransition(fromColor.asBukkitColor(), toColor.asBukkitColor(), size);
			});

			// uses color differently
			registerColorable("Particle.NOTE");
			// TODO test how this works
			registerDataSupplier("Particle.NOTE", (raw, location) -> {
				int colorValue = (int) (((Number) raw).floatValue() * 255);
				ColorRGB color = ColorRGB.fromRGB(colorValue, 0, 0);
				return new ParticleOption(color, 1);
			});

			// Float DataType, represents "the angle the particle displays at in radians"
			registerDataSupplier("Particle.SCULK_CHARGE", (raw, location) -> raw != null ? raw : 0);

			// Integer DataType, represents "the delay in ticks"
			registerDataSupplier("Particle.SHRIEK", (raw, location) -> {
				int delay = 0;
				if (raw instanceof Timespan)
					delay = (int) Math.min(Math.max(((Timespan) raw).getAs(Timespan.TimePeriod.TICK), 0), Integer.MAX_VALUE);
				return delay;
			});

			registerDataSupplier("Particle.VIBRATION", (raw, location) -> VibrationUtils.buildVibration((Object[]) raw, location));

			generateTypes();
		});
	}

	// exists to avoid NoClassDefFoundError from Vibration
	private static final class VibrationUtils {
		private static Vibration buildVibration(Object[] data, Location location) {
			int arrivalTime = -1;
			if (data[1] != null)
				arrivalTime = (int) Math.min(Math.max(((Timespan) data[1]).getAs(Timespan.TimePeriod.TICK), 0), Integer.MAX_VALUE);
			if (data[0] instanceof Entity) {
				Entity entity = (Entity) data[0];
				if (arrivalTime == -1)
					arrivalTime = (int) (location.distance(entity.getLocation()) / 20);
				//noinspection removal - new constructor only exists on newer versions
				return new Vibration(location, new Vibration.Destination.EntityDestination(entity), arrivalTime);
			}
			// assume it's a location
			Location destination = data[0] != null ? (Location) data[0] : location;
			if (arrivalTime == -1)
				arrivalTime = (int) (location.distance(destination) / 20);
			//noinspection removal - new constructor only exists on newer versions
			return new Vibration(location, new Vibration.Destination.BlockDestination(destination), arrivalTime);
		}
	}

}
