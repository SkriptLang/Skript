package org.skriptlang.skript.bukkit.entity.elements.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.LivingEntity;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Is Riptiding")
@Description("Checks to see if an entity is currently using the Riptide enchantment.")
@Example("target entity is riptiding")
@Since("2.5")
public class CondIsRiptiding extends PropertyCondition<LivingEntity> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.CONDITION,
			infoBuilder(CondIsRiptiding.class, PropertyType.BE, "riptiding", "livingentities")
				.supplier(CondIsRiptiding::new)
				.build()
		);
	}
	
	@Override
	public boolean check(LivingEntity entity) {
		return entity.isRiptiding();
	}
	
	@Override
	protected String getPropertyName() {
		return "riptiding";
	}
	
}
