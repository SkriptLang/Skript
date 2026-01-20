package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Panda;

@Name("Panda Is On Its Back")
@Description("Whether a panda is on its back.")
@Example("""
	if last spawned panda is on its back:
		make last spawned panda get off its back
	""")
@Since("2.11")
public class CondPandaIsOnBack extends PropertyCondition<LivingEntity> {

	static {
		register(CondPandaIsOnBack.class, "on (its|their) back[s]", "livingentities");
	}

	@Override
	public boolean check(LivingEntity entity) {
		return entity instanceof Panda panda && panda.isOnBack();
	}

	@Override
	protected String getPropertyName() {
		return "on their back";
	}

}
