package org.skriptlang.skript.bukkit.damagesource;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.registry.RegistryClassInfo;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.registrations.EventValues;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.HierarchicalAddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.damagesource.elements.conditions.CondScalesWithDifficulty;
import org.skriptlang.skript.bukkit.damagesource.elements.conditions.CondWasIndirect;
import org.skriptlang.skript.bukkit.damagesource.elements.expressions.*;

public class DamageSourceModule extends HierarchicalAddonModule {

	public DamageSourceModule(AddonModule parentModule) {
		super(parentModule);
	}

	@Override
	protected boolean canLoadSelf(SkriptAddon addon) {
		return Skript.classExists("org.bukkit.damage.DamageSource");
	}

	@Override
	protected void initSelf(SkriptAddon addon) {
		Classes.registerClass(new ClassInfo<>(DamageSource.class, "damagesource")
			.user("damage ?sources?")
			.name("Damage Source")
			.description(
				"Represents the source from which an entity was damaged.",
				"Cannot change any attributes of the damage source from an 'on damage' or 'on death' event.")
			.since("2.12")
			.defaultExpression(new EventValueExpression<>(DamageSource.class))
		);

		Classes.registerClass(
			new RegistryClassInfo<>(DamageType.class, RegistryAccess.registryAccess().getRegistry(RegistryKey.DAMAGE_TYPE),
									"damagetype", "damage types")
			.user("damage ?types?")
			.name("Damage Type")
			.description("References a damage type of a damage source.")
			.since("2.12")
		);

		EventValues.registerEventValue(EntityDamageEvent.class, DamageSource.class, EntityDamageEvent::getDamageSource);
		EventValues.registerEventValue(EntityDeathEvent.class, DamageSource.class, EntityDeathEvent::getDamageSource);
	}

	@Override
	protected void loadSelf(SkriptAddon addon) {
		register(addon,
			CondScalesWithDifficulty::register,
			CondWasIndirect::register,

			ExprCausingEntity::register,
			ExprCreatedDamageSource::register,
			ExprDamageLocation::register,
			ExprDamageType::register,
			ExprDirectEntity::register,
			ExprFoodExhaustion::register,
			ExprSourceLocation::register,

			ExprSecDamageSource::register
		);
	}

	@Override
	public String name() {
		return "damage source";
	}

}
