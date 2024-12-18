package ch.njol.skript.conditions;

import org.bukkit.entity.LivingEntity;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;

@Name("Is Swimming")
@Description("Checks whether a living entity is swimming.")
@Examples("player is swimming")
@RequiredPlugins("1.13 or newer")
@Since("2.3")
public class CondIsSwimming extends PropertyCondition<LivingEntity> {
	
	static {
		if (Skript.methodExists(LivingEntity.class, "isSwimming"))
			register(CondIsSwimming.class, "swimming", "livingentities");
	}
	
	@Override
	public boolean check(final LivingEntity e) {
		return e.isSwimming();
	}
	
	@Override
	protected String getPropertyName() {
		return "swimming";
	}
	
}
