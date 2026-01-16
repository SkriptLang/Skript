package org.skriptlang.skript.bukkit.entity.general.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Tameable;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Is Tameable")
@Description("Check if an entity is tameable.")
@Example("""
	on damage:
		if victim is tameable:
			cancel event
	""")
@Since("2.5")
public class CondIsTameable extends PropertyCondition<LivingEntity> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.CONDITION,
			infoBuilder(CondIsTameable.class, PropertyType.BE, "tameable", "livingentities")
				.supplier(CondIsTameable::new)
				.build()
		);
	}
	
	@Override
	public boolean check(LivingEntity entity) {
		return entity instanceof Tameable;
	}
	
	@Override
	protected String getPropertyName() {
		return "tameable";
	}
	
}