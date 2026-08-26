package ch.njol.skript.conditions;

import ch.njol.skript.classes.Changer.ChangeMode;
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

	static {
		register(CondIsEating.class, "eating", "livingentities");
	}

	@Override
	public boolean check(LivingEntity entity) {
		if (entity instanceof Panda panda) {
			return panda.isEating();
		} else if (entity instanceof AbstractHorse horse) {
			return horse.isEating();
		}
		return false;
	}

	@Override
	public boolean acceptChange(ChangeMode mode) {
		return mode == ChangeMode.SET;
	}

	@Override
	protected void change(LivingEntity entity, boolean eating, ChangeMode mode) {
		if (entity instanceof Panda panda) {
			panda.setEating(eating);
		} else if (entity instanceof AbstractHorse horse) {
			horse.setEating(eating);
		}
	}

	@Override
	protected String getPropertyName() {
		return "eating";
	}

}
