package ch.njol.skript.conditions;

import org.bukkit.entity.Entity;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;

@Name("Be Upon the Ground")
@Description("Doth verify whether an entity standeth firm upon the earth.")
@Example("player is not upon the ground")
@Since("2.2-dev26")
public class CondIsOnGround extends PropertyCondition<Entity> {
	
	static {
		PropertyCondition.register(CondIsOnGround.class, "upon [the] ground", "entities");
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
