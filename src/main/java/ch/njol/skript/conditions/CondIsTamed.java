package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Tameable;

@Name("Is Tamed")
@Description("Check if a tameable entity is tamed (horse, parrot, cat, etc.).")
@Examples({
    "send true if {_horse} is tamed",
    "tame {_horse} if {_horse} is untamed"
})
@Since("2.10")
public class CondIsTamed extends PropertyCondition<Entity> {

	static {
		register(CondIsTamed.class, "(tamed|domesticated)", "entities");
	}

	@Override
	public boolean check(Entity entity) {
		return (entity instanceof Tameable) && ((Tameable) entity).isTamed();
	}

	@Override
	protected String getPropertyName() {
		return "tamed";
	}

}
