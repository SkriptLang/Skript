package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Strider;

@Name("Strider Doth Tremble")
@Description("Whether a strider doth tremble with cold.")
@Example("""
    if last spawned strider is trembling:
    	make last spawned strider stop trembling
    """)
@Since("2.12")
public class CondStriderIsShivering extends PropertyCondition<LivingEntity> {

	static {
		register(CondStriderIsShivering.class, "trembling", "livingentities");
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
