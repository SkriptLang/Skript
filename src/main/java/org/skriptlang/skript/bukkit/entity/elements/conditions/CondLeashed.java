package org.skriptlang.skript.bukkit.entity.elements.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.LivingEntity;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Is Leashed")
@Description("Checks to see if an entity is currently leashed.")
@Example("target entity is leashed")
@Since("2.5")
public class CondLeashed extends PropertyCondition<LivingEntity> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.CONDITION,
			infoBuilder(CondLeashed.class, PropertyType.BE, "leashed", "livingentities")
				.supplier(CondLeashed::new)
				.build()
		);
	}

	@Override
	public boolean check(LivingEntity entity) {
		return entity.isLeashed();
	}

	@Override
	protected String getPropertyName() {
		return "leashed";
	}

}
