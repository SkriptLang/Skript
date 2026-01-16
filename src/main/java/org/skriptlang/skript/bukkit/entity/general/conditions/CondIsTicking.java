package org.skriptlang.skript.bukkit.entity.general.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.Entity;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Is Ticking")
@Description("Checks if an entity is ticking.")
@Example("send true if target is ticking")
@Since("2.10")
public class CondIsTicking extends PropertyCondition<Entity> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.CONDITION,
			infoBuilder(CondIsTicking.class, PropertyType.BE, "ticking", "entities")
				.supplier(CondIsTicking::new)
				.build()
		);
	}

	@Override
	public boolean check(Entity entity) {
		return entity.isTicking();
	}

	@Override
	protected String getPropertyName() {
		return "ticking";
	}

}

