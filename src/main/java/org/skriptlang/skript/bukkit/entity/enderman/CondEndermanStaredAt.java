package org.skriptlang.skript.bukkit.entity.enderman;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.LivingEntity;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Enderman Has Been Stared At")
@Description({
	"Checks to see if an enderman has been stared at.",
	"This will return true as long as the entity that stared at the enderman is still alive."
})
@Example("if last spawned enderman has been stared at:")
@Since("2.11")
public class CondEndermanStaredAt extends PropertyCondition<LivingEntity> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.CONDITION,
			infoBuilder(CondEndermanStaredAt.class, PropertyType.HAVE, "been stared at", "livingentities")
				.supplier(CondEndermanStaredAt::new)
				.build()
		);
	}


	@Override
	public boolean check(LivingEntity entity) {
		if (entity instanceof Enderman enderman)
			return enderman.hasBeenStaredAt();
		return false;
	}

	@Override
	protected String getPropertyName() {
		return "stared at";
	}

}
