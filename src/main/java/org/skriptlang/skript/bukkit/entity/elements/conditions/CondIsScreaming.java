package org.skriptlang.skript.bukkit.entity.elements.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Goat;
import org.bukkit.entity.LivingEntity;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Is Screaming")
@Description("Check whether a goat or enderman is screaming.")
@Example("""
	if last spawned goat is not screaming:
		make last spawned goat scream
	""")
@Example("""
	if {_enderman} is screaming:
		force {_enderman} to stop screaming
	""")
@Since("2.11")
public class CondIsScreaming extends PropertyCondition<LivingEntity> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.CONDITION,
			infoBuilder(CondIsScreaming.class, PropertyType.BE, "screaming", "livingentities")
				.supplier(CondIsScreaming::new)
				.build()
		);
	}

	@Override
	public boolean check(LivingEntity entity) {
		if (entity instanceof Goat goat) {
			return goat.isScreaming();
		} else if (entity instanceof Enderman enderman) {
			return enderman.isScreaming();
		}
		return false;
	}

	@Override
	protected String getPropertyName() {
		return "screaming";
	}

}
