package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.LivingEntity;

@Name("Is Leashed")
@Description("Checks to see if an entity is currently leashed.")
@Examples("target entity is leashed")
@Since("2.5")
public class CondLeashed extends PropertyCondition<LivingEntity> {

	static {
		register(CondLeashed.class, PropertyType.BE, "leashed", "livingentities");
	}

	@Override
	public boolean check(LivingEntity entity) {
		return entity.isLeashed();
	}

	@Override
	protected String getPropertyName() {
		return "leashed";
	}

}
