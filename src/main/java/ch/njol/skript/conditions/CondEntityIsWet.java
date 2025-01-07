package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.*;
import org.bukkit.entity.Entity;

@Name("Entity is Wet")
@Description("Checks whether an entity is wet or not (in water, rain or a bubble column).")
@Examples("if player is wet:")
@RequiredPlugins("Paper 1.16+")
@Since("2.6.1")
public class CondEntityIsWet extends PropertyCondition<Entity> {
	
	static {
		if (Skript.methodExists(Entity.class, "isInWaterOrRainOrBubbleColumn"))
			register(CondEntityIsWet.class, PropertyType.BE, "wet", "entities");
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
