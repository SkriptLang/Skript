package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.*;
import org.bukkit.entity.LivingEntity;

@Name("Is Swimming")
@Description("Checks whether a living entity is swimming.")
@Examples("player is swimming")
@Since("2.3")
public class CondIsSwimming extends PropertyCondition<LivingEntity> {
	
	static {
		register(CondIsSwimming.class, "swimming", "livingentities");
	}
	
	@Override
	public boolean check(LivingEntity entity) {
		return entity.isSwimming();
	}
	
	@Override
	protected String getPropertyName() {
		return "swimming";
	}
	
}
