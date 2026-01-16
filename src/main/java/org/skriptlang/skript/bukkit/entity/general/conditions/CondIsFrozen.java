package org.skriptlang.skript.bukkit.entity.general.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.Entity;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Is Frozen")
@Description("Checks whether an entity is frozen.")
@Example("""
	if player is frozen:
		kill player
	""")
@Since("2.7")
public class CondIsFrozen extends PropertyCondition<Entity> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.CONDITION,
			infoBuilder(CondIsFrozen.class, PropertyType.BE, "frozen", "entities")
				.supplier(CondIsFrozen::new)
				.build()
		);
	}

	@Override
	public boolean check(Entity entity) {
		return entity.isFrozen();
	}

	@Override
	protected String getPropertyName() {
		return "frozen";
	}

}
