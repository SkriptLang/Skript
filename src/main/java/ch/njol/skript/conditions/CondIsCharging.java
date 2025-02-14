package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.LivingEntity;

@Name("Is Charging Fireball")
@Description("Check whether a ghast is charging a fireball.")
@Examples({
	"if last spawned ghast is charging:",
		"\tkill last spawned ghast"
})
@Since("INSERT VERSION")
public class CondIsCharging extends PropertyCondition<LivingEntity> {

	static {
		register(CondIsCharging.class, "charging [[a] fireball]", "livingentities");
	}

	@Override
	public boolean check(LivingEntity entity) {
		if (entity instanceof Ghast ghast)
			return ghast.isCharging();
		return false;
	}

	@Override
	protected String getPropertyName() {
		return "charging fireball";
	}

}
