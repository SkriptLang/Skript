package org.skriptlang.skript.bukkit.entity.elements.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.Entity;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Entity is Wet")
@Description("Checks whether an entity is wet or not (in water, rain or a bubble column).")
@Example("if player is wet:")
@Since("2.6.1")
public class CondEntityIsWet extends PropertyCondition<Entity> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.CONDITION,
			infoBuilder(CondEntityIsWet.class, PropertyType.BE, "wet", "entities")
				.supplier(CondEntityIsWet::new)
				.build()
		);
	}

	@Override
	public boolean check(Entity entity) {
		return entity.isInWater() || entity.isInRain() || entity.isInBubbleColumn();
	}

	@Override
	protected String getPropertyName() {
		return "wet";
	}

}
