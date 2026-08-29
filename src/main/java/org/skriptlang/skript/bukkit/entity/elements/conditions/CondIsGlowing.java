package org.skriptlang.skript.bukkit.entity.elements.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.*;
import org.bukkit.entity.Entity;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Is Glowing")
@Description(
	"Check whether an entity is glowing. Glowing entites have outlines that can be " +
	"seen through blocks. This will change the entity's glowing property (NBT). This " +
	"is not related to the glowing potion effect."
)
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
