package org.skriptlang.skript.bukkit.entity.elements.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.*;
import org.bukkit.entity.Entity;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Is Glowing")
@Description("Checks whether or not an entity is glowing.")
@Example("""
	command /glow:
		trigger:
			if player is glowing:
				make player stop glowing
			else:
				make player glow
	""")
@Since("INSERT VERSION")
public class CondIsGlowing extends PropertyCondition<Entity> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.CONDITION,
			infoBuilder(
				CondIsGlowing.class,
				PropertyType.BE,
				"glowing",
				"entities"
			)
				.supplier(CondIsGlowing::new)
				.build()
		);
	}

	@Override
	public boolean check(Entity entity) {
		return entity.isGlowing();
	}

	@Override
	protected String getPropertyName() {
		return "glowing";
	}

}
