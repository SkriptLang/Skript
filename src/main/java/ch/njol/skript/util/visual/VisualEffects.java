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
import ch.njol.skript.aliases.Aliases;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.expressions.ExprVisualEffect;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.Noun;
import ch.njol.skript.util.Color;
import ch.njol.skript.util.ColorRGB;
import ch.njol.skript.util.Direction;
import ch.njol.skript.util.SkriptColor;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.variables.Variables;
import ch.njol.util.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Vibration;
import org.bukkit.Vibration.Destination;
import org.bukkit.Vibration.Destination.BlockDestination;
import org.bukkit.Vibration.Destination.EntityDestination;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.eclipse.jdt.annotation.Nullable;

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

	private static final boolean DUST_OPTIONS_EXISTS = true;

	private static final Map<String, Consumer<VisualEffectType>> effectTypeModifiers = new HashMap<>();
	private static VisualEffectType[] visualEffectTypes;

	static {
		Variables.yggdrasil.registerSingleClass(Effect.class, "Bukkit_Effect");
		Variables.yggdrasil.registerSingleClass(EntityEffect.class, "Bukkit_EntityEffect");
	}

	public static VisualEffectType get(int i) {
		return visualEffectTypes[i];
	}

	@Nullable
	public static VisualEffectType get(String id) {
		for (VisualEffectType type : visualEffectTypes) {
			if (id.equals(type.getId()))
				return type;
		}
		return null;
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
		Skript.registerExpression(ExprVisualEffect.class, VisualEffect.class, ExpressionType.COMBINED, patterns);
	}

	private static void registerColorable(String id) {
		effectTypeModifiers.put(id, VisualEffectType::setColorable);
	}

	private static void registerDataSupplier(String id, BiFunction<Object[], Location, Object> dataSupplier) {
		Consumer<VisualEffectType> consumer = type -> type.withData(dataSupplier);
		if (effectTypeModifiers.containsKey(id)) {
			consumer = effectTypeModifiers.get(id).andThen(consumer);
		}
		effectTypeModifiers.put(id, consumer);
	}

	static {
		Language.addListener(() -> {
			if (visualEffectTypes != null) // Already registered
				return;
			// Colorables
			registerColorable("Particle.SPELL_MOB");
			registerColorable("Particle.SPELL_MOB_AMBIENT");
			registerColorable("Particle.REDSTONE");
			registerColorable("Particle.NOTE");
			registerColorable("Particle.DUST_COLOR_TRANSITION");

			// Data suppliers
			registerDataSupplier("Effect.POTION_BREAK", (raw, location) ->
				new PotionEffect(raw[0] == null ? PotionEffectType.SPEED : (PotionEffectType) raw[0], 1, 0));
			registerDataSupplier("Effect.SMOKE", (raw, location) -> {
				if (raw[0] == null)
					return BlockFace.SELF;
				return Direction.getFacing(((Direction) raw[0]).getDirection(location), false);
			});

			Color defaultColor = SkriptColor.LIGHT_RED;
			float defaultSize = 1;
			registerDataSupplier("Particle.SPELL_MOB", (raw, location) -> {
				Color color = raw[0] == null ? defaultColor : (Color) raw[0];
				return new ParticleOption(color, 1);
			});
			registerDataSupplier("Particle.SPELL_MOB_AMBIENT", (raw, location) -> {
				Color color = raw[0] == null ? defaultColor : (Color) raw[0];
				return new ParticleOption(color, 1);
			});
			registerDataSupplier("Particle.REDSTONE", (raw, location) -> {
				Color color = raw[0] == null ? defaultColor : (Color) raw[0];
				float size = raw[1] == null ? defaultSize : ((Number) raw[1]).floatValue();

				return new Particle.DustOptions(color.asBukkitColor(), size);
			});
			registerDataSupplier("Particle.NOTE", (raw, location) -> {
				int colorValue = (int) (((Number) raw[0]).floatValue() * 255);
				ColorRGB color = new ColorRGB(colorValue, 0, 0);
				return new ParticleOption(color, 1);
			});
			registerDataSupplier("Particle.ITEM_CRACK", (raw, location) -> {
				ItemStack itemStack = Aliases.javaItemType("iron sword").getRandom();
				if (raw[0] instanceof ItemType) {
					ItemStack rand = ((ItemType) raw[0]).getRandom();
					if (rand != null)
						itemStack = rand;
				} else if (raw[0] != null) {
					return raw[0];
				}

				assert itemStack != null;
				if (Particle.ITEM_CRACK.getDataType() == Material.class)
					return itemStack.getType();
				return itemStack;
			});

			BiFunction<Object[], Location, Object> crackDustBiFunction = (raw, location) -> {
				if (raw[0] == null) {
					return Material.STONE.getData();
				} else if (raw[0] instanceof ItemType) {
					ItemStack rand = ((ItemType) raw[0]).getRandom();
					return Bukkit.createBlockData(rand != null ? rand.getType() : Material.STONE);
				} else {
					return raw[0];
				}
			};
			registerDataSupplier("Particle.BLOCK_CRACK", crackDustBiFunction);
			registerDataSupplier("Particle.BLOCK_DUST", crackDustBiFunction);
			registerDataSupplier("Particle.FALLING_DUST", crackDustBiFunction);

			registerDataSupplier("Particle.DUST_COLOR_TRANSITION", (raw, location) -> {
				Color colorFrom = raw[0] == null ? defaultColor : (Color) raw[0];
				Color colorTo = raw[2] == null ? defaultColor : (Color) raw[2];
				float size = raw[1] == null ? defaultSize : ((Number) raw[1]).floatValue();

				return new Particle.DustTransition(colorFrom.asBukkitColor(), colorTo.asBukkitColor(), size);
			});

			int defaultArrivalTime = 20; // in ticks, 1 second
			if (Skript.classExists("org.bukkit.Vibration")) {
				// Don't convert to lambda, it breaks compat with < 1.17
				//noinspection Convert2Lambda
				registerDataSupplier("Particle.VIBRATION", new BiFunction<Object[], Location, Object>() {
					@Override
					public Object apply(Object[] raw, Location location) {
						if (raw[0] == null || raw[1] == null)
							return null;

						Location origin = (Location) raw[0];

						Destination destination;
						if (raw[1] instanceof Location) {
							destination = new BlockDestination((Location) raw[1]);
						} else if (raw[1] instanceof Entity) {
							destination = new EntityDestination((Entity) raw[1]);
						} else {
							return null;
						}

						int arrivalTime = raw[2] == null ? defaultArrivalTime : (int) ((Timespan) raw[2]).getTicks_i();

						return new Vibration(origin, destination, arrivalTime);
					}
				});
			}

			generateTypes();
		});
	}

}
