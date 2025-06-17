package org.skriptlang.skript.bukkit.potion;

import ch.njol.skript.Skript;
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
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionEffectTypeCategory;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.potion.elements.conditions.*;
import org.skriptlang.skript.bukkit.potion.elements.effects.*;
import org.skriptlang.skript.bukkit.potion.elements.events.*;
import org.skriptlang.skript.bukkit.potion.elements.expressions.*;
import org.skriptlang.skript.bukkit.potion.util.PotionUtils;
import org.skriptlang.skript.bukkit.potion.util.SkriptPotionEffect;
import org.skriptlang.skript.lang.comparator.Comparators;
import org.skriptlang.skript.lang.comparator.Relation;
import org.skriptlang.skript.lang.converter.Converters;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.io.StreamCorruptedException;

public class PotionModule implements AddonModule {

	@Override
	public void init(SkriptAddon addon) {
		// Register ClassInfos
		Classes.registerClass(new ClassInfo<>(SkriptPotionEffect.class, "skriptpotioneffect")
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
					return potionEffect.toPotionEffect();
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

		var petRegistry = PotionUtils.getPotionEffectTypeRegistry();
		if (petRegistry != null) {
			Classes.registerClass(new RegistryClassInfo<>(PotionEffectType.class, petRegistry, "potioneffecttype", "potion effect types", false)
				.user("potion ?effect ?types?")
				.name("Potion Effect Type")
				.description("A potion effect type, e.g. 'strength' or 'swiftness'.")
				.examples("apply swiftness 5 to the player",
					"apply potion of speed 2 to the player for 60 seconds",
					"remove invisibility from the victim")
				.since(""));
		} else {
			Classes.registerClass(LegacyPotionEffectTypeInfo.getInfo());
		}

		Classes.registerClass(new EnumClassInfo<>(EntityPotionEffectEvent.Cause.class, "entitypotioncause", "entity potion causes")
			.user("(entity )?potion ?effect ?cause")
			.name("Entity Potion Effect Event Cause")
			.description("Represents the cause of an 'entity potion effect' event. For example, an arrow hitting an entity or a command being executed.")
			.since("2.10"));
		Classes.registerClass(new EnumClassInfo<>(EntityPotionEffectEvent.Action.class, "entitypotionaction", "entity potion actions")
			.user("(entity )?potion ?effect ?action")
			.name("Entity Potion Effect Event Action")
			.description("Represents the action being performed in an 'entity potion effect' event.",
				"'added' indicates the entity does not already have a potion effect of the event potion effect type.",
				"'changed' indicates the entity already has a potion effect of the event potion effect type, but some property about the potion effect is changing.",
				"'cleared' indicates that the effect is being removed because all of the entity's effects are being removed.",
				"'removed' indicates that the event potion effect type has been specifically removed from the entity.")
			.since("INSERT VERSION"));

		// Added in 1.21
		if (Skript.classExists("org.bukkit.potion.PotionEffectTypeCategory")) {
			Classes.registerClass(new EnumClassInfo<>(PotionEffectTypeCategory.class, "potioneffecttypecategory", "potion effect type categories")
				.user("potion ?effect ?type? category?(ies)?")
				.name("Potion Effect Type Category")
				.description("Represents the type of effect a potion effect type has on an entity.")
				.since("INSERT VERSION"));
			Comparators.registerComparator(PotionEffectType.class, PotionEffectTypeCategory.class,
				(type, category) -> Relation.get(type.getCategory() == category));
		}

		// SkriptPotionEffect -> PotionEffect
		Converters.registerConverter(SkriptPotionEffect.class, PotionEffect.class, SkriptPotionEffect::toPotionEffect);
		// PotionEffect -> SkriptPotionEffect
		Converters.registerConverter(PotionEffect.class, SkriptPotionEffect.class, SkriptPotionEffect::fromBukkitEffect);
		// PotionEffectType -> SkriptPotionEffect
		Converters.registerConverter(PotionEffectType.class, SkriptPotionEffect.class, SkriptPotionEffect::fromType);
	}

	@Override
	public void load(SkriptAddon addon) {
		// Load Syntax
		SyntaxRegistry registry = addon.syntaxRegistry();
		// conditions
		CondHasPotion.register(registry);
		CondIsPoisoned.register(registry);
		CondIsPotionAmbient.register(registry);
		CondIsPotionInfinite.register(registry);
		CondIsPotionInstant.register(registry);
		CondPotionHasIcon.register(registry);
		CondPotionHasParticles.register(registry);
		// effects
		EffApplyPotionEffect.register(registry);
		EffPoison.register(registry);
		EffPotionAmbient.register(registry);
		EffPotionIcon.register(registry);
		EffPotionInfinite.register(registry);
		EffPotionParticles.register(registry);
		// events
		EvtEntityPotion.register(registry);
		// expressions
		ExprPotionAmplifier.register(registry);
		ExprPotionDuration.register(registry);
		ExprPotionEffect.register(registry);
		ExprPotionEffects.register(registry);
		ExprPotionEffectTypeCategory.register(registry);
		ExprSecPotionEffect.register(registry);
		ExprSkriptPotionEffect.register(registry);
	}

}
