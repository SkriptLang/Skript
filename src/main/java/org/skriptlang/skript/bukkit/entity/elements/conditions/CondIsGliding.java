package org.skriptlang.skript.bukkit.entity.elements.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.LivingEntity;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Is Gliding")
@Description("Checks whether a living entity is gliding.")
@Example("if player is gliding")
@Since("2.7")
public class CondIsGliding extends PropertyCondition<LivingEntity> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.CONDITION,
			infoBuilder(CondIsGliding.class, PropertyType.BE, "gliding", "livingentities")
				.supplier(CondIsGliding::new)
				.build()
		);
	}

	@Override
	public boolean check(LivingEntity entity) {
		return entity.isGliding();
	}

	@Override
	protected String getPropertyName() {
		return "gliding";
	}

}
