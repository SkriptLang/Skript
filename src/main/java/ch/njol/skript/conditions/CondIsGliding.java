package ch.njol.skript.conditions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.LivingEntity;

import ch.njol.skript.conditions.base.PropertyCondition;

@Name("Is Gliding")
@Description("Checks whether a living entity is gliding.")
@Examples("if player is gliding")
@Since("2.7")
public class CondIsGliding extends PropertyCondition<LivingEntity> {

	static {
		register(CondIsGliding.class, "gliding", "livingentities");
	}

	@Override
	public boolean check(LivingEntity entity) {
		return entity.isGliding();
	}

	@Override
	protected String getPropertyName() {
		return "gliding";
	}

}
