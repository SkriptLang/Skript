package org.skriptlang.skript.bukkit.entity.general.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.LivingEntity;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Is Swimming")
@Description("Checks whether a living entity is swimming.")
@Example("player is swimming")
@Since("2.3")
public class CondIsSwimming extends PropertyCondition<LivingEntity> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.CONDITION,
			infoBuilder(CondIsSwimming.class, PropertyType.BE, "swimming", "livingentities")
				.supplier(CondIsSwimming::new)
				.build()
		);
	}
	
	@Override
	public boolean check(LivingEntity entity) {
		return entity.isSwimming();
	}
	
	@Override
	protected String getPropertyName() {
		return "swimming";
	}
	
}
