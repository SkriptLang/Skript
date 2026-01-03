package org.skriptlang.skript.bukkit.potion;

import ch.njol.skript.Skript;
import ch.njol.skript.bukkitutil.BukkitUtils;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.EnumClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.classes.Serializer;
import ch.njol.skript.classes.YggdrasilSerializer;
import ch.njol.skript.classes.registry.RegistryClassInfo;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import ch.njol.yggdrasil.Fields;
import org.bukkit.Registry;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionEffectTypeCategory;
import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.potion.elements.conditions.*;
import org.skriptlang.skript.bukkit.potion.elements.effects.*;
import org.skriptlang.skript.bukkit.potion.elements.events.*;
import org.skriptlang.skript.bukkit.potion.elements.expressions.*;
import org.skriptlang.skript.bukkit.potion.util.SkriptPotionEffect;
import org.skriptlang.skript.docs.Origin;
import org.skriptlang.skript.lang.comparator.Comparators;
import org.skriptlang.skript.lang.comparator.Relation;
import org.skriptlang.skript.lang.converter.Converter;
import org.skriptlang.skript.lang.converter.Converters;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.io.StreamCorruptedException;

public class PotionModule implements AddonModule {

	@Override
	public void init(SkriptAddon addon) {
		// Register ClassInfos
		Classes.registerClass(new ClassInfo<>(SkriptPotionEffect.class, "skriptpotioneffect")
			.name(ClassInfo.NO_DOC)
			.defaultExpression(new EventValueExpression<>(SkriptPotionEffect.class))
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
					return "potion_effect:" + potionEffect.potionEffectType().getKey().getKey();
				}
			})
			.serializer(new YggdrasilSerializer<>()));

		Classes.registerClass(new ClassInfo<>(PotionEffect.class, "potioneffect")
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
				public String toString(PotionEffect potionEffect, int flags) {
					return SkriptPotionEffect.fromBukkitEffect(potionEffect).toString(flags);
				}

				@Override
				public String toVariableNameString(PotionEffect potionEffect) {
					return "potion_effect:" + potionEffect.getType().getKey().getKey();
				}
			})
			.serializer(new Serializer<>() {
				@Override
				public Fields serialize(PotionEffect potionEffect) {
					Fields fields = new Fields();
					fields.putObject("potion", SkriptPotionEffect.fromBukkitEffect(potionEffect));
					return fields;
				}

				@Override
				public void deserialize(PotionEffect potionEffect, Fields fields) {
					assert false;
				}

				@Override
				protected PotionEffect deserialize(Fields fields) throws StreamCorruptedException {
					//<editor-fold desc="Legacy deserialization handling" defaultstate="collapsed">
					if (!fields.hasField("potion")) {
						String typeName = fields.getObject("type", String.class);
						assert typeName != null;
						//noinspection deprecation - legacy compatibility method
						PotionEffectType type = PotionEffectType.getByName(typeName);
						if (type == null)
							throw new StreamCorruptedException("Invalid PotionEffectType " + typeName);
						int amplifier = fields.getPrimitive("amplifier", int.class);
						int duration = fields.getPrimitive("duration", int.class);
						boolean particles = fields.getPrimitive("particles", boolean.class);
						boolean ambient = fields.getPrimitive("ambient", boolean.class);
						return new PotionEffect(type, duration, amplifier, ambient, particles);
					}
					//</editor-fold>
					SkriptPotionEffect potionEffect = fields.getObject("potion", SkriptPotionEffect.class);
					if (potionEffect == null) {
						throw new StreamCorruptedException();
					}
					return potionEffect.asBukkitPotionEffect();
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

		Registry<@NotNull PotionEffectType> petRegistry;
		if (BukkitUtils.registryExists("MOB_EFFECT")) { // Paper (1.21.4)
			petRegistry = Registry.MOB_EFFECT;
		} else if (BukkitUtils.registryExists("EFFECT")) { // Bukkit (1.20.3)
			petRegistry = Registry.EFFECT;
		} else {
			throw new IllegalStateException("Potion effect registry does not exist");
		}
		Classes.registerClass(new RegistryClassInfo<>(PotionEffectType.class, petRegistry, "potioneffecttype", "potion effect types", false)
			.user("potion ?effect ?types?")
			.name("Potion Effect Type")
			.description("A potion effect type, e.g. 'strength' or 'swiftness'.")
			.examples("apply swiftness 5 to the player",
				"apply potion of speed 2 to the player for 60 seconds",
				"remove invisibility from the victim")
			.since("2.0 beta 3"));

		Classes.registerClass(new EnumClassInfo<>(EntityPotionEffectEvent.Cause.class, "potioncause", "potion causes")
			.user("(entity ?)?potion ?effect ?causes?")
			.name("Potion Effect Event Cause")
			.description("Represents the cause of an 'entity potion effect' event. For example, an arrow hitting an entity or a command being executed.")
			.examples("""
				on entity potion effect:
					if the event-potion effect cause is arrow affliction:
						message "You were hit by a tipped arrow!"
				""")
			.since("2.10"));
		Classes.registerClass(new EnumClassInfo<>(EntityPotionEffectEvent.Action.class, "potionaction", "potion actions")
			.user("(entity ?)?potion ?effect ?actions?")
			.name("Potion Effect Event Action")
			.description("Represents the action being performed in an 'entity potion effect' event.",
				"'added' indicates the entity does not already have a potion effect of the event potion effect type.",
				"'changed' indicates the entity already has a potion effect of the event potion effect type, but some property about the potion effect is changing.",
				"'cleared' indicates that the effect is being removed because all of the entity's effects are being removed.",
				"'removed' indicates that the event potion effect type has been specifically removed from the entity.")
			.examples("""
				on entity potion effect:
					if the event-potion effect action is removal:
						message "One of your existing potion effects was removed!"
				""")
			.since("2.14"));

		// Added in 1.21
		if (Skript.classExists("org.bukkit.potion.PotionEffectTypeCategory")) {
			Classes.registerClass(new EnumClassInfo<>(PotionEffectTypeCategory.class, "potioneffecttypecategory", "potion effect type categories")
				.user("potion ?effect ?type? categor(y|ies)")
				.name("Potion Effect Type Category")
				.description("Represents the type of effect a potion effect type has on an entity.")
				.since("2.14"));
			Comparators.registerComparator(PotionEffectType.class, PotionEffectTypeCategory.class,
				(type, category) -> Relation.get(type.getCategory() == category));
		}

		// SkriptPotionEffect -> PotionEffect
		Converters.registerConverter(SkriptPotionEffect.class, PotionEffect.class, SkriptPotionEffect::asBukkitPotionEffect, Converter.NO_CHAINING);
		// PotionEffect -> SkriptPotionEffect
		Converters.registerConverter(PotionEffect.class, SkriptPotionEffect.class, SkriptPotionEffect::fromBukkitEffect, Converter.NO_CHAINING);
		// PotionEffectType -> SkriptPotionEffect
		Converters.registerConverter(PotionEffectType.class, SkriptPotionEffect.class, SkriptPotionEffect::fromType, Converter.NO_CHAINING);
		// SkriptPotionEffect -> PotionEffectType
		Converters.registerConverter(SkriptPotionEffect.class, PotionEffectType.class, SkriptPotionEffect::potionEffectType, Converter.NO_CHAINING);
	}

	@Override
	public void load(SkriptAddon addon) {
		// Load Syntax
		SyntaxRegistry registry = addon.syntaxRegistry();
		Origin origin = AddonModule.origin(addon, "potions");
		// conditions
		CondHasPotion.register(registry, origin);
		CondIsPoisoned.register(registry, origin);
		CondIsPotionAmbient.register(registry, origin);
		CondIsPotionInstant.register(registry, origin);
		CondPotionHasIcon.register(registry, origin);
		CondPotionHasParticles.register(registry, origin);
		// effects
		EffApplyPotionEffect.register(registry, origin);
		EffPoison.register(registry, origin);
		EffPotionAmbient.register(registry, origin);
		EffPotionIcon.register(registry, origin);
		EffPotionInfinite.register(registry, origin);
		EffPotionParticles.register(registry, origin);
		// events
		EvtEntityPotion.register(registry, origin);
		// expressions
		ExprPotionAmplifier.register(registry, origin);
		ExprPotionDuration.register(registry, origin);
		ExprPotionEffect.register(registry, origin);
		ExprPotionEffects.register(registry, origin);
		ExprPotionEffectTypeCategory.register(registry, origin);
		ExprSecPotionEffect.register(registry, origin);
		ExprSkriptPotionEffect.register(registry, origin);
	}

}
