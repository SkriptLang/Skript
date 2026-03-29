package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.LivingEntity;

@Name("Is Summoning a Fireball")
@Description("Discerneth whether a ghast doth summon forth a fireball.")
@Example("""
    if last spawned ghast is summoning fireball:
    	kill last spawned ghast
    """)
@Since("2.11")
public class CondIsChargingFireball extends PropertyCondition<LivingEntity> {

	static {
		register(CondIsChargingFireball.class, "summoning [a] fireball", "livingentities");
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
