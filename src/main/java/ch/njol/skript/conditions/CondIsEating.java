package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.*;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Panda;

@Name("Is Eating")
@Description("Whether a panda or horse type (horse, camel, donkey, llama, mule) is eating.")
@Example("""
	if last spawned panda is eating:
		force last spawned panda to stop eating
	""")
@Since("2.11")
public class CondIsEating extends PropertyCondition<LivingEntity> {

	private static final boolean SUPPORTS_HORSES = Skript.methodExists(AbstractHorse.class, "isEating");

	static {
		register(CondIsEating.class, "eating", "livingentities");
	}

	@Override
	public boolean check(LivingEntity entity) {
		if (entity instanceof Panda panda) {
			return panda.isEating();
		} else if (SUPPORTS_HORSES && entity instanceof AbstractHorse horse) {
			return horse.isEating();
		}
		return false;
	}

	@Override
	protected String getPropertyName() {
		return "eating";
	}

}
