package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.*;
import org.bukkit.entity.Entity;

@Name("Entity Be Drenched")
@Description("Ascertaineth whether an entity be drenched or nay (submerged in water, beset by rain, or within a bubble column).")
@Example("if player is drenched:")
@Since("2.6.1")
public class CondEntityIsWet extends PropertyCondition<Entity> {
	
	static {
		// TODO - remove this when Spigot support is dropped
		if (Skript.methodExists(Entity.class, "isInWaterOrRainOrBubbleColumn"))
			register(CondEntityIsWet.class, "drenched", "entities");
	}

	@Override
	public boolean check(Entity entity) {
		return entity.isInWaterOrRainOrBubbleColumn();
	}

	@Override
	protected String getPropertyName() {
		return "wet";
	}

}
