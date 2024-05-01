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
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SyntaxElementInfo;
import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.Noun;
import ch.njol.skript.util.Color;
import ch.njol.skript.util.ColorRGB;
import ch.njol.skript.util.Direction;
import ch.njol.skript.util.SkriptColor;
import ch.njol.skript.variables.Variables;
import ch.njol.util.StringUtils;
import ch.njol.util.coll.iterator.SingleItemIterator;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
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

	private static final boolean NEW_EFFECT_DATA = Skript.classExists("org.bukkit.block.data.BlockData");
	private static final boolean HAS_REDSTONE_DATA = Skript.classExists("org.bukkit.Particle$DustOptions");

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

	@Nullable
	static final Particle OLD_REDSTONE_PARTICLE;
	@Nullable
	static final Particle OLD_ITEM_CRACK_PARTICLE;

	static {
		Particle oldRedstoneParticle = null;
		Particle oldItemCrackParticle = null;
		if (Skript.fieldExists(Particle.class, "REDSTONE")) { // initialize legacy particle support
			try {
				oldRedstoneParticle = Particle.valueOf("REDSTONE");
				oldItemCrackParticle = Particle.valueOf("ITEM_CRACK");
			} catch (IllegalArgumentException e) {
				oldRedstoneParticle = null;
				oldItemCrackParticle = null;
				Skript.exception(e, "Failed to initialize legacy particle support. Some particles may not work as expected.");
			}
		}
		OLD_REDSTONE_PARTICLE = oldRedstoneParticle;
		OLD_ITEM_CRACK_PARTICLE = oldItemCrackParticle;
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

			// Data suppliers
			registerDataSupplier("Effect.POTION_BREAK", (raw, location) ->
				new PotionEffect(raw == null ? PotionEffectType.SPEED : (PotionEffectType) raw, 1, 0));
			registerDataSupplier("Effect.SMOKE", (raw, location) -> {
				if (raw == null)
					return BlockFace.SELF;
				return Direction.getFacing(((Direction) raw).getDirection(location), false);
			});

			Color defaultColor = SkriptColor.LIGHT_RED;
			registerDataSupplier("Particle.SPELL_MOB", (raw, location) -> {
				Color color = raw == null ? defaultColor : (Color) raw;
				return new ParticleOption(color, 1);
			});
			registerDataSupplier("Particle.SPELL_MOB_AMBIENT", (raw, location) -> {
				Color color = raw == null ? defaultColor : (Color) raw;
				return new ParticleOption(color, 1);
			});
			registerDataSupplier("Particle.REDSTONE", (raw, location) -> {
				Color color = raw == null ? defaultColor : (Color) raw;
				ParticleOption particleOption = new ParticleOption(color, 1);

				if (HAS_REDSTONE_DATA && (OLD_REDSTONE_PARTICLE == null || OLD_REDSTONE_PARTICLE.getDataType() == Particle.DustOptions.class)) {
					return new Particle.DustOptions(particleOption.getBukkitColor(), particleOption.size);
				} else {
					return particleOption;
				}
			});
			registerDataSupplier("Particle.NOTE", (raw, location) -> {
				int colorValue = (int) (((Number) raw).floatValue() * 255);
				ColorRGB color = new ColorRGB(colorValue, 0, 0);
				return new ParticleOption(color, 1);
			});
			registerDataSupplier("Particle.ITEM_CRACK", (raw, location) -> {
				ItemStack itemStack = Aliases.javaItemType("iron sword").getRandom();
				if (raw instanceof ItemType) {
					ItemStack rand = ((ItemType) raw).getRandom();
					if (rand != null)
						itemStack = rand;
				} else if (raw != null) {
					return raw;
				}

				assert itemStack != null;
				if (OLD_ITEM_CRACK_PARTICLE == null || OLD_ITEM_CRACK_PARTICLE.getDataType() == Material.class)
					return itemStack.getType();
				return itemStack;
			});

			BiFunction<Object, Location, Object> crackDustBiFunction = (raw, location) -> {
				if (raw == null) {
					return Material.STONE.getData();
				} else if (raw instanceof ItemType) {
					ItemStack rand = ((ItemType) raw).getRandom();
					if (NEW_EFFECT_DATA) {
						return Bukkit.createBlockData(rand != null ? rand.getType() : Material.STONE);
					} else {
						if (rand == null)
							return Material.STONE.getData();

						@SuppressWarnings("deprecation")
						MaterialData type = rand.getData();
						assert type != null;
						return type;
					}
				} else {
					return raw;
				}
			};
			registerDataSupplier("Particle.BLOCK_CRACK", crackDustBiFunction);
			registerDataSupplier("Particle.BLOCK_DUST", crackDustBiFunction);
			registerDataSupplier("Particle.FALLING_DUST", crackDustBiFunction);

			generateTypes();
		});
	}

}
