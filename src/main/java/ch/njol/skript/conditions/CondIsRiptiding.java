package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.LivingEntity;

@Name("Is Riptiding")
@Description("Checks to see if an entity is currently using the Riptide enchantment.")
@Examples("target entity is riptiding")
@Since("2.5")
public class CondIsRiptiding extends PropertyCondition<LivingEntity> {
	
	static {
		register(CondIsRiptiding.class, "riptiding", "livingentities");
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
