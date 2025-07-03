package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Strider;

@Name("Strider Is Shivering")
@Description("Whether a strider is shivering.")
@Example("""
	if last spawned strider is shivering:
		make last spawned strider stop shivering
	""")
@Since("2.12")
public class CondStriderIsShivering extends PropertyCondition<LivingEntity> {

	static {
		register(CondStriderIsShivering.class, "shivering", "livingentities");
	}

	@Override
	public boolean check(LivingEntity entity) {
		return entity instanceof Strider strider && strider.isShivering();
	}

	@Override
	protected String getPropertyName() {
		return "shivering";
	}

}
