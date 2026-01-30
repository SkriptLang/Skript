package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.Entity;
import io.papermc.paper.entity.Leashable;

@Name("Is Leashed")
@Description("Checks to see if an entity is currently leashed.")
@Example("target entity is leashed")
@Since("2.5, INSERT VERSION (any entity type)")
public class CondLeashed extends PropertyCondition<Entity> {

	static {
		register(CondLeashed.class, PropertyType.BE, "leashed", "entities");
	}

	@Override
	public boolean check(Entity entity) {
		if (entity instanceof Leashable leashable) {
			return leashable.isLeashed();
		}
		return false;
	}

	@Override
	protected String getPropertyName() {
		return "leashed";
	}

}
