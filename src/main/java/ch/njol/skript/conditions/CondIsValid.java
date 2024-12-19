package ch.njol.skript.conditions;

import org.bukkit.entity.Entity;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;

@Name("Is Valid")
@Description("Checks whether an entity has died or been despawned for some other reason.")
@Examples("if event-entity is valid")
@Since("2.7")
public class CondIsValid extends PropertyCondition<Entity> {

	static {
		register(CondIsValid.class, "valid", "entities");
	}

	@Override
	public boolean check(Entity entity) {
		return entity.isValid();
	}

	@Override
	protected String getPropertyName() {
		return "valid";
	}

}
