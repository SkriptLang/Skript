package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Panda;

@Name("Panda Is Rolling")
@Description("Whether a panda is rolling.")
@Example("""
	if last spawned panda is rolling:
		make last spawned panda stop rolling
	""")
@Since("2.11")
public class CondPandaIsRolling extends PropertyCondition<LivingEntity> {

	static {
		register(CondPandaIsRolling.class, "rolling", "livingentities");
	}

	@Override
	public boolean check(LivingEntity entity) {
		return entity instanceof Panda panda && panda.isRolling();
	}

	@Override
	protected String getPropertyName() {
		return "rolling";
	}

}
