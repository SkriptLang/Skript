package org.skriptlang.skript.bukkit.entity.elements.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.Entity;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Has Gravity")
@Description("Checks whether or not an entity experiences gravity.")
@Example("send whether player has gravity")
@Since("INSERT VERSION")
public class CondHasGravity extends PropertyCondition<Entity> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.CONDITION,
			infoBuilder(
				CondHasGravity.class,
				PropertyType.HAVE,
				"gravity",
				"entities"
			)
				.supplier(CondHasGravity::new)
				.build()
		);
	}

	@Override
	public boolean check(Entity entity) {
		return entity.hasGravity();
	}

	@Override
	protected String getPropertyName() {
		return "gravity";
	}

}
