package ch.njol.skript.conditions;

import org.bukkit.entity.Entity;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;

@Name("Is on Ground")
@Description("Checks whether an entity is on ground.")
@Examples("player is not on ground")
@Since("2.2-dev26")
public class CondIsOnGround extends PropertyCondition<Entity> {
	
	static {
		PropertyCondition.register(CondIsOnGround.class, "on [the] ground", "entities");
	}
	
	@Override
	public boolean check(Entity entity) {
		return entity.isOnGround();
	}
	
	@Override
	protected String getPropertyName() {
		return "on ground";
	}
	
}
