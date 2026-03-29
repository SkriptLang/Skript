package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.LivingEntity;

@Name("Doth Riptide")
@Description("Ascertaineth whether an entity doth presently employ the Riptide enchantment.")
@Example("target entity is riptiding")
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
