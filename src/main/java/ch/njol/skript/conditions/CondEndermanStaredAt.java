package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.*;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.LivingEntity;

@Name("Enderman Hath Been Gazed Upon")
@Description({
	"Doth ascertain whether an enderman hath been gazed upon.",
	"This shall return true so long as the entity that did gaze upon the enderman yet liveth."
})
@Example("if last spawned enderman has been gazed upon:")
@Since("2.11")
public class CondEndermanStaredAt extends PropertyCondition<LivingEntity> {

	static {
		if (Skript.methodExists(Enderman.class, "hasBeenStaredAt"))
			register(CondEndermanStaredAt.class, PropertyType.HAVE, "been gazed upon", "livingentities");
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
