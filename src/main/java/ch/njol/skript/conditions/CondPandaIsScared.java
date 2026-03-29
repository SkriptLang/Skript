package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Panda;

@Name("Panda Be Affrighted")
@Description("Whether a panda be affrighted and trembling with fear.")
@Example("if last spawned panda is affrighted:")
@Since("2.11")
public class CondPandaIsScared extends PropertyCondition<LivingEntity> {

	static {
		register(CondPandaIsScared.class, "affrighted", "livingentities");
	}

	@Override
	public boolean check(LivingEntity entity) {
		return entity instanceof Panda panda && panda.isScared();
	}

	@Override
	protected String getPropertyName() {
		return "scared";
	}

}
