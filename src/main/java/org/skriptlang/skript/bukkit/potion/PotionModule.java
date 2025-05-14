package org.skriptlang.skript.bukkit.potion;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.classes.Serializer;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.StringUtils;
import ch.njol.yggdrasil.Fields;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.potion.elements.*;
import org.skriptlang.skript.bukkit.potion.util.PotionUtils;
import org.skriptlang.skript.bukkit.potion.util.SkriptPotionEffect;
import org.skriptlang.skript.lang.comparator.Comparators;
import org.skriptlang.skript.lang.comparator.Relation;
import org.skriptlang.skript.lang.converter.Converters;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.io.StreamCorruptedException;

public class PotionModule implements AddonModule {

	@Override
	public void load(SkriptAddon addon) {
		// PotionEffectType -> SkriptPotionEffect
		Converters.registerConverter(PotionEffectType.class, SkriptPotionEffect.class, SkriptPotionEffect::new);

		Comparators.registerComparator(PotionEffectType.class, PotionEffectType.class, (p1, p2) -> Relation.get(p1.equals(p2)));
		Comparators.registerComparator(SkriptPotionEffect.class, SkriptPotionEffect.class, (p1, p2) -> Relation.get(p1.equals(p2)));

		// Register ClassInfos
		Classes.registerClass(new ClassInfo<>(SkriptPotionEffect.class, "potioneffect")
				.user("potion ?effects?")
				.name("Potion Effect")
				.description("A potion effect, including the potion effect type, tier and duration.")
				.usage("speed of tier 1 for 10 seconds")
				.since("2.5.2")
				.parser(new Parser<>() {
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
				.serializer(new Serializer<>() {
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
				})
		);

		Classes.registerClass(new ClassInfo<>(PotionEffectType.class, "potioneffecttype")
				.user("potion ?effect ?types?")
				.name("Potion Effect Type")
				.description("A potion effect type, e.g. 'strength' or 'swiftness'.")
				.usage(StringUtils.join(PotionUtils.getNames(), ", "))
				.examples(
					"apply swiftness 5 to the player",
					"apply potion of speed 2 to the player for 60 seconds",
					"remove invisibility from the victim"
				)
				.since("")
				.supplier(PotionEffectType.values())
				.parser(new Parser<>() {
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
				.serializer(new Serializer<>() {
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
		SyntaxRegistry registry = addon.syntaxRegistry();
		CondHasPotion.register(registry);
		CondIsPoisoned.register(registry);
		CondPotionProperties.register(registry);
		EffApplyPotionEffect.register(registry);
		EffPoison.register(registry);
		EffPotionProperties.register(registry);
		EvtEntityPotion.register(registry);
		ExprPotionEffect.register(registry);
		ExprPotionEffects.register(registry);
		ExprPotionProperties.register(registry);
	}

}
