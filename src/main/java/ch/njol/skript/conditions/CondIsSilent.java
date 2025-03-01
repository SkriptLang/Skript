package ch.njol.skript.conditions;

import org.bukkit.entity.Entity;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;

@Name("Is Silent")
@Description("Checks whether an entity is silent i.e. its sounds are disabled.")
@Examples("target entity is silent")
@Since("2.5")
public class CondIsSilent extends PropertyCondition<Entity> {
	
	static {
		register(CondIsSilent.class, "silent", "entities");
	}
	
	@Override
	public boolean check(Entity entity) {
		return entity.isSilent();
	}
	
	@Override
	protected String getPropertyName() {
		return "silent";
	}

}
