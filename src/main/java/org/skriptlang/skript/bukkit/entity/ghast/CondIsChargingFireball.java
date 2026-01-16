package org.skriptlang.skript.bukkit.entity.ghast;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.LivingEntity;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Is Charging Fireball")
@Description("Check whether a ghast is charging a fireball.")
@Example("""
	if last spawned ghast is charging fireball:
		kill last spawned ghast
	""")
@Since("2.11")
public class CondIsChargingFireball extends PropertyCondition<LivingEntity> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.CONDITION,
			infoBuilder(CondIsChargingFireball.class, PropertyType.BE, "charging [a] fireball", "livingentities")
				.supplier(CondIsChargingFireball::new)
				.build()
		);
	}

	@Override
	public boolean check(LivingEntity entity) {
		if (entity instanceof Ghast ghast)
			return ghast.isCharging();
		return false;
	}

	@Override
	protected String getPropertyName() {
		return "charging fireball";
	}

}
