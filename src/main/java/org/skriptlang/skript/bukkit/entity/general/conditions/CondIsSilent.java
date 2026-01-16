package org.skriptlang.skript.bukkit.entity.general.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.Entity;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Is Silent")
@Description("Checks whether an entity is silent i.e. its sounds are disabled.")
@Example("target entity is silent")
@Since("2.5")
public class CondIsSilent extends PropertyCondition<Entity> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.CONDITION,
			infoBuilder(CondIsSilent.class, PropertyType.BE, "silent", "entities")
				.supplier(CondIsSilent::new)
				.build()
		);
	}
	
	@Override
	public boolean check(Entity entity) {
		return entity.isSilent();
	}
	
	@Override
	protected String getPropertyName() {
		return "silent";
	}

}
