package org.skriptlang.skript.bukkit.entity.elements.conditions;

import org.bukkit.entity.LivingEntity;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Has AI")
@Description(
	"Check whether an entity has AI. Entities without AI will not try to attack " +
	"other entities and will not be able to move at all. This includes knockback " +
	"from getting hit, falling due to gravity, getting pushed by entities/water, " +
	"changing velocity with commands and any other form of movement. Accumulated " +
	"knockback and velocity changes will be applied when ai is enabled again."
)
@Example("target entity has ai")
@Since("2.5")
public class CondAI extends PropertyCondition<LivingEntity> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.CONDITION,
			infoBuilder(
				CondAI.class,
				PropertyType.HAVE,
				"(ai|artificial intelligence)",
				"livingentities"
			)
				.supplier(CondAI::new)
				.build()
		);
	}
	
	@Override
	public boolean check(LivingEntity entity) {
		return entity.hasAI();
	}

	@Override
	protected PropertyType getPropertyType() {
		return PropertyType.HAVE;
	}

	@Override
	protected String getPropertyName() {
		return "artificial intelligence";
	}

}
