package org.skriptlang.skript.bukkit.entity.elements.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Tameable;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Is Tamed")
@Description("Check if a tameable entity is tamed (horse, parrot, cat, etc.).")
@Example("send true if {_horse} is tamed")
@Example("tame {_horse} if {_horse} is untamed")
@Since("2.10")
public class CondIsTamed extends PropertyCondition<Entity> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.CONDITION,
			infoBuilder(CondIsTamed.class, PropertyType.BE, "(tamed|domesticated)", "entities")
				.supplier(CondIsTamed::new)
				.build()
		);
	}

	@Override
	public boolean check(Entity entity) {
		return (entity instanceof Tameable tameable) && tameable.isTamed();
	}

	@Override
	protected String getPropertyName() {
		return "tamed";
	}

}
