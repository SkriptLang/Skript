package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.*;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.LivingEntity;

@Name("Enderman Has Been Stared At")
@Description({
	"Checks to see if an enderman has been stared at.",
	"This will return true as long as the entity that stared at the enderman is still alive."
})
@Examples("if last spawned enderman has been stared at:")
@RequiredPlugins("Paper")
@Since("INSERT VERSION")
public class CondEndermanStaredAt extends PropertyCondition<LivingEntity> {

	static {
		if (Skript.methodExists(Enderman.class, "hasBeenStaredAt"))
			register(CondEndermanStaredAt.class, PropertyType.HAVE, "been stared at", "livingentities");
	}

	@Override
	public boolean check(LivingEntity entity) {
		if (entity instanceof Enderman enderman)
			return enderman.hasBeenStaredAt();
		return false;
	}

	@Override
	protected String getPropertyName() {
		return "stared at";
	}

}
