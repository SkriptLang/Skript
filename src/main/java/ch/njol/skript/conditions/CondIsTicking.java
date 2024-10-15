package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.*;
import org.bukkit.entity.Entity;

@Name("Is Ticking")
@Description("Checks if an entity is ticking.")
@Examples("send true if target is ticking")
@RequiredPlugins("PaperMC")
@Since("INSERT VERSION")
public class CondIsTicking extends PropertyCondition<Entity> {

	static {
		if (Skript.methodExists(Entity.class, "isTicking"))
			register(CondIsTicking.class, "ticking", "entities");
	}

	@Override
	public boolean check(Entity entity) {
		return entity.isTicking();
	}

	@Override
	protected String getPropertyName() {
		return "ticking";
	}

}

