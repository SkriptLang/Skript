package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.Entity;

@Name("Is ticking")
@Description("Check if an entity is ticking.")
@Examples({
	"send true if target is ticking"
})
@Since("INSERT VERSION")
public class CondIsTicking extends PropertyCondition<Entity> {

	static {
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
