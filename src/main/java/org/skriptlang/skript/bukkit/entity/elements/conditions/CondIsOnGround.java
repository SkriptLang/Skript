package org.skriptlang.skript.bukkit.entity.elements.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.Entity;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Is on Ground")
@Description("Checks whether an entity is on ground.")
@Example("player is not on ground")
@Since("2.2-dev26")
public class CondIsOnGround extends PropertyCondition<Entity> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.CONDITION,
			infoBuilder(CondIsOnGround.class, PropertyType.BE, "on [the] ground", "entities")
				.supplier(CondIsOnGround::new)
				.build()
		);
	}
	
	@Override
	public boolean check(Entity entity) {
		return entity.isOnGround();
	}
	
	@Override
	protected String getPropertyName() {
		return "on ground";
	}
	
}
