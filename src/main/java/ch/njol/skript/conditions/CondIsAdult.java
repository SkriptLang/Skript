package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.LivingEntity;

@Name("Is Adult")
@Description("Checks whether or not a living entity is an adult.")
@Examples({
	"on drink:",
		"\tevent-entity is not an adult",
		"\tkill event-entity"
})
@Since("INSERT VERSION")
public class CondIsAdult extends PropertyCondition<LivingEntity> {

	static {
		register(CondIsAdult.class, "[an] adult", "livingentities");
	}

	@Override
	public boolean check(LivingEntity entity) {
		if (entity instanceof Ageable ageable)
			return ageable.isAdult();

		return false;
	}

	@Override
	protected String getPropertyName() {
		return "an adult";
	}

}
