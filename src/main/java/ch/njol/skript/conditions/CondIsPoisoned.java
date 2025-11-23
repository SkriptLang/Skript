package ch.njol.skript.conditions;

import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffectType;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;

@Name("Is Poisoned")
@Description("Checks whether an entity is poisoned.")
@Example("""
	player is poisoned:
		cure the player from poison
		message "You have been cured!"
	""")
@Since("1.4.4")
public class CondIsPoisoned extends PropertyCondition<LivingEntity> {
	
	static {
		register(CondIsPoisoned.class, "poisoned", "livingentities");
	}
	
	@Override
	public boolean check(LivingEntity entity) {
		return entity.hasPotionEffect(PotionEffectType.POISON);
	}
	
	@Override
	protected String getPropertyName() {
		return "poisoned";
	}
	
}
