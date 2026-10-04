package ch.njol.skript.conditions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Panda;

@Name("Panda Is Sneezing")
@Description("Whether a panda is sneezing.")
@Example("""
	if last spawned panda is sneezing:
		make last spawned panda stop sneezing
	""")
@Since("2.11")
public class CondPandaIsSneezing extends PropertyCondition<LivingEntity> {

	static {
		register(CondPandaIsSneezing.class, "sneezing", "livingentities");
	}

	@Override
	public boolean check(LivingEntity entity) {
		return entity instanceof Panda panda && panda.isSneezing();
	}

	@Override
	public boolean acceptChange(ChangeMode mode) {
		return mode == ChangeMode.SET;
	}

	@Override
	protected void change(LivingEntity entity, boolean sneezing, ChangeMode mode) {
		if (entity instanceof Panda panda) {
			panda.setSneezing(sneezing);
		}
	}

	@Override
	protected String getPropertyName() {
		return "sneezing";
	}

}
