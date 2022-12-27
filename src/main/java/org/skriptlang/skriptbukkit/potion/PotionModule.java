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
package org.skriptlang.skriptbukkit.potion;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAddon;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Comparator;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.classes.Serializer;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.registrations.Comparators;
import ch.njol.skript.registrations.Converters;
import ch.njol.util.StringUtils;
import ch.njol.yggdrasil.Fields;
import org.skriptlang.skriptbukkit.potion.util.PotionUtils;
import org.skriptlang.skriptbukkit.potion.util.SkriptPotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.eclipse.jdt.annotation.Nullable;

import java.io.IOException;
import java.io.StreamCorruptedException;

public class PotionModule {

	public void register(SkriptAddon addon) {
		// PotionEffectType -> SkriptPotionEffect
		Converters.registerConverter(PotionEffectType.class, SkriptPotionEffect.class, SkriptPotionEffect::new);

		Comparators.registerComparator(PotionEffectType.class, PotionEffectType.class, new Comparator<PotionEffectType, PotionEffectType>() {
			@Override
			public Relation compare(PotionEffectType p1, PotionEffectType p2) {
				return Relation.get(p1.equals(p2));
			}

			@Override
			public boolean supportsOrdering() {
				return false;
			}
		});
		Comparators.registerComparator(SkriptPotionEffect.class, SkriptPotionEffect.class, new Comparator<SkriptPotionEffect, SkriptPotionEffect>() {
			@Override
			public Relation compare(SkriptPotionEffect p1, SkriptPotionEffect p2) {
				return Relation.get(p1.equals(p2));
			}

			@Override
			public boolean supportsOrdering() {
				return false;
			}
		});

		// Register ClassInfos
		Classes.registerClass(new ClassInfo<>(SkriptPotionEffect.class, "potioneffect")
			.user("potion ?effects?")
			.name("Potion Effect")
			.description("A potion effect, including the potion effect type, tier and duration.")
			.usage("speed of tier 1 for 10 seconds")
			.since("2.5.2")
			.parser(new Parser<SkriptPotionEffect>() {
				@Override
				public boolean canParse(ParseContext context) {
					return false;
				}

				@Override
				public String toString(SkriptPotionEffect potionEffect, int flags) {
					return potionEffect.toString(flags);
				}

				@Override
				public String toVariableNameString(SkriptPotionEffect potionEffect) {
					return "potioneffect:" + potionEffect.toPotionEffect().getType().getName();
				}
			})
			.serializer(new Serializer<SkriptPotionEffect>() {
				@Override
				public Fields serialize(SkriptPotionEffect potionEffect) {
					Fields fields = new Fields();
					fields.putObject("type", potionEffect.potionEffectType().getName());
					fields.putPrimitive("duration", potionEffect.duration());
					fields.putPrimitive("amplifier", potionEffect.amplifier());
					fields.putPrimitive("ambient", potionEffect.ambient());
					fields.putPrimitive("particles", potionEffect.particles());
					fields.putPrimitive("icon", potionEffect.icon());
					return fields;
				}

				@Override
				public void deserialize(SkriptPotionEffect o, Fields f) {
					assert false;
				}

				@Override
				protected SkriptPotionEffect deserialize(Fields fields) throws StreamCorruptedException {
					String typeName = fields.getObject("type", String.class);
					assert typeName != null;
					PotionEffectType type = PotionEffectType.getByName(typeName);
					if (type == null)
						throw new StreamCorruptedException("Invalid PotionEffectType " + typeName);

					int duration = fields.getPrimitive("duration", int.class);
					int amplifier = fields.getPrimitive("amplifier", int.class);
					boolean ambient = fields.getPrimitive("ambient", boolean.class);
					boolean particles = fields.getPrimitive("particles", boolean.class);
					boolean icon = fields.getPrimitive("icon", boolean.class);

					return new SkriptPotionEffect(type).duration(duration).amplifier(amplifier).ambient(ambient).particles(particles).icon(icon);
				}

				@Override
				public boolean mustSyncDeserialization() {
					return false;
				}

				@Override
				protected boolean canBeInstantiated() {
					return false;
				}
			}));

		Classes.registerClass(new ClassInfo<>(PotionEffectType.class, "potioneffecttype")
			.user("potion( ?effect)? ?types?") // "type" is non-optional to prevent clashing with potion effects
			.name("Potion Effect Type")
			.description("A potion effect type, e.g. 'strength' or 'swiftness'.")
			.usage(StringUtils.join(PotionUtils.getNames(), ", "))
			.examples(
				"apply swiftness 5 to the player",
				"apply potion of speed 2 to the player for 60 seconds",
				"remove invisibility from the victim"
			)
			.since("")
			.parser(new Parser<PotionEffectType>() {
				@Override
				@Nullable
				public PotionEffectType parse(String s, ParseContext context) {
					return PotionUtils.fromString(s);
				}

				@Override
				public String toString(PotionEffectType p, int flags) {
					return PotionUtils.toString(p, flags);
				}

				@Override
				public String toVariableNameString(PotionEffectType p) {
					return "potioneffecttype:" + p.getName();
				}
			})
			.serializer(new Serializer<PotionEffectType>() {
				@Override
				public Fields serialize(final PotionEffectType o) {
					final Fields f = new Fields();
					f.putObject("name", o.getName());
					return f;
				}

				@Override
				public void deserialize(final PotionEffectType o, final Fields f) {
					assert false;
				}

				@Override
				protected PotionEffectType deserialize(final Fields fields) throws StreamCorruptedException {
					final String name = fields.getObject("name", String.class);
					assert name != null;
					final PotionEffectType t = PotionEffectType.getByName(name);
					if (t == null)
						throw new StreamCorruptedException("Invalid PotionEffectType " + name);
					return t;
				}

				@Override
				public boolean mustSyncDeserialization() {
					return false;
				}

				@Override
				public boolean canBeInstantiated() {
					return false;
				}
			})
		);

		// Load Syntax
		try {
			addon.loadClasses("org.skriptlang.skriptbukkit.potion.elements");
		} catch (IOException e) {
			Skript.exception(e, "An error occurred while trying to load potion elements.");
		}

	}

}
