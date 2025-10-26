package org.skriptlang.skript.bukkit.damagesource;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.registry.RegistryClassInfo;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.registrations.EventValues;
import org.bukkit.Registry;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.registration.TypeInfo;
import org.skriptlang.skript.registration.TypeRegistry;

import java.io.IOException;

public class DamageSourceModule implements AddonModule {

	@Override
	public boolean canLoad(SkriptAddon addon) {
		return Skript.classExists("org.bukkit.damage.DamageSource");
	}

	@Override
	public void init(SkriptAddon addon) {
		Skript.registerType(TypeInfo.builder(addon, DamageSource.class, "Damage Source", "damage ?sources?")
				.description(
						"Represents the source from which an entity was damaged.",
						"Cannot change any attributes of the damage source from an 'on damage' or 'on death' event."
				)
				.since("2.12")
				.requires("Minecraft 1.20.4+")
				.defaultExpression(new EventValueExpression<>(DamageSource.class))
				.build());

		Skript.registerType(TypeInfo.builder(addon, DamageType.class, "Damage Type", Registry.DAMAGE_TYPE, "damage types", "damage ?types?")
				.description("References a damage type of a damage source.")
				.since("2.12")
				.requires("Minecraft 1.20.4+")
				.build());

		if (Skript.methodExists(EntityDamageEvent.class, "getDamageSource")) {
			EventValues.registerEventValue(EntityDamageEvent.class, DamageSource.class, EntityDamageEvent::getDamageSource);
		}
		if (Skript.methodExists(EntityDeathEvent.class, "getDamageSource")) {
			EventValues.registerEventValue(EntityDeathEvent.class, DamageSource.class, EntityDeathEvent::getDamageSource);
		}
	}

	@Override
	public void load(SkriptAddon addon) {
		try {
			Skript.getAddonInstance().loadClasses("org.skriptlang.skript.bukkit.damagesource", "elements");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
