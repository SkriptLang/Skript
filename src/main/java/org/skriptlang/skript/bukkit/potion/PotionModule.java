package org.skriptlang.skript.bukkit.potion;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.classes.YggdrasilSerializer;
import ch.njol.skript.classes.registry.RegistryClassInfo;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import org.bukkit.potion.PotionEffectType;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.potion.elements.*;
import org.skriptlang.skript.bukkit.potion.util.PotionUtils;
import org.skriptlang.skript.bukkit.potion.util.SkriptPotionEffect;
import org.skriptlang.skript.lang.converter.Converters;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class PotionModule implements AddonModule {

	@Override
	public void init(SkriptAddon addon) {
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
					return "potioneffect:" + potionEffect.potionEffectType().getKey().getKey();
				}
			})
			.serializer(new YggdrasilSerializer<>()));

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

		// PotionEffectType -> SkriptPotionEffect
		Converters.registerConverter(PotionEffectType.class, SkriptPotionEffect.class, SkriptPotionEffect::fromType);
	}

	@Override
	public void load(SkriptAddon addon) {
		// Load Syntax
		SyntaxRegistry registry = addon.syntaxRegistry();
		CondHasIcon.register(registry);
		CondHasParticles.register(registry);
		CondHasPotion.register(registry);
		CondIsAmbient.register(registry);
		CondIsInfinite.register(registry);
		CondIsPotionInstant.register(registry);
		CondIsPoisoned.register(registry);
		EffApplyPotionEffect.register(registry);
		EffPoison.register(registry);
		EffPotionProperties.register(registry);
		EvtEntityPotion.register(registry);
		ExprPotionEffect.register(registry);
		ExprPotionEffects.register(registry);
		ExprPotionProperties.register(registry);
	}

}
