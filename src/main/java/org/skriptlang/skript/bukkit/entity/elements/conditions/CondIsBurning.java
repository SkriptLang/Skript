package org.skriptlang.skript.bukkit.entity.elements.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.Entity;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Is Burning")
@Description("Checks whether an entity is on fire, e.g. a zombie due to being in sunlight, or any entity after falling into lava.")
@Example("""
	# increased attack against burning targets
	victim is burning:
		increase damage by 2
	""")
@Since("1.4.4")
public class CondIsBurning extends PropertyCondition<Entity> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.CONDITION,
			infoBuilder(CondIsBurning.class, PropertyType.BE, "(burning|ignited|on fire)", "entities")
				.supplier(CondIsBurning::new)
				.build()
		);
	}
	
	@Override
	public boolean check(Entity entity) {
		return entity.getFireTicks() > 0;
	}
	
	@Override
	protected String getPropertyName() {
		return "burning";
	}
	
}
