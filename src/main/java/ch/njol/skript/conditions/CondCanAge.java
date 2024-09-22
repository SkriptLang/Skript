package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.Breedable;
import org.bukkit.entity.LivingEntity;

@Name("Can Age")
@Description("Returns whether or not an entity will be able to age/grow up.")
@Examples({
	"on breeding:",
		"\tentity can't age",
		"\tbroadcast \"An immortal has been born!\" to player"
})
@Since("INSERT VERSION")
public class CondCanAge extends PropertyCondition<LivingEntity> {

	static {
		if (Skript.classExists("org.bukkit.entity.Breedable"))
			register(CondCanAge.class, PropertyType.CAN, "age", "livingentities");
	}

	@Override
	public boolean check(LivingEntity entity) {
		if (entity instanceof Breedable breedable)
			return !breedable.getAgeLock();
		return false;
	}

	@Override
	protected String getPropertyName() {
		return "age";
	}

}
