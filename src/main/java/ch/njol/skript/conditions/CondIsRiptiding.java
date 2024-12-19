package ch.njol.skript.conditions;

import org.bukkit.entity.LivingEntity;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;

@Name("Is Riptiding")
@Description("Checks to see if an entity is currently using the Riptide enchantment.")
@Examples("target entity is riptiding")
@Since("2.5")
public class CondIsRiptiding extends PropertyCondition<LivingEntity> {
	
	static {
		if (Skript.methodExists(LivingEntity.class, "isRiptiding")) {
			register(CondIsRiptiding.class, PropertyType.BE, "riptiding", "livingentities");
		}
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
