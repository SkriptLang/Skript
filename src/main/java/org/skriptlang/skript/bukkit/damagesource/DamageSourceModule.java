package org.skriptlang.skript.bukkit.damagesource;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.registry.RegistryClassInfo;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.registrations.EventValues;
import org.bukkit.Registry;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.skriptlang.skript.lang.comparator.Comparators;
import org.skriptlang.skript.lang.comparator.Relation;

public class DamageSourceModule {

	public static void load() throws Exception {
		if (!Skript.classExists("org.bukkit.damage.DamageSource"))
			return;

		Skript.getAddonInstance().loadClasses("org.skriptlang.skript.bukkit.damagesource", "elements");

		Class<?> damageSourceClass = Class.forName("org.bukkit.damage.DamageSource");

		//noinspection unchecked,rawtypes
		Classes.registerClass(new ClassInfo<>(damageSourceClass, "damagesource")
			.user("damage ?sources?")
			.name("Damage Source")
			.description(
				"Represents the source from which an entity was damaged.",
				"Cannot change any attributes of the damage source from an 'on damage' or 'on death' event.")
			.since("INSERT VERSION")
			.requiredPlugins("Minecraft 1.20.4+")
			.defaultExpression(new EventValueExpression<>((Class) damageSourceClass))
			.cloner(org.skriptlang.skript.bukkit.damagesource.DamageSourceWrapper::clone)
		);

		Comparators.registerComparator(damageSourceClass, org.bukkit.damage.DamageSource.class,
			((o1, o2) -> Relation.get(o1.equals(o2))));

		Classes.registerClass(new RegistryClassInfo<>(org.bukkit.damage.DamageType.class, Registry.DAMAGE_TYPE, "damagetype", "damage types")
			.user("damage ?types?")
			.name("Damage Type")
			.description("References a damage type of a damage source.")
			.since("INSERT VERSION")
			.requiredPlugins("Minecraft 1.20.4+")
		);

		if (Skript.methodExists(EntityDamageEvent.class, "getDamageSource"))
			EventValues.registerEventValue(EntityDamageEvent.class, org.bukkit.damage.DamageSource.class, EntityDamageEvent::getDamageSource);
		if (Skript.methodExists(EntityDeathEvent.class, "getDamageSource"))
			EventValues.registerEventValue(EntityDeathEvent.class, org.bukkit.damage.DamageSource.class, EntityDeathEvent::getDamageSource);
	}

}
