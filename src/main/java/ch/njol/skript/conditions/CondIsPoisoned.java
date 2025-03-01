package ch.njol.skript.conditions;

import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffectType;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;

@Name("Is Poisoned")
@Description("Checks whether an entity is poisoned.")
@Examples({
	"player is poisoned:",
		"\tcure the player from poison",
		"\tmessage \"You have been cured!\""
})
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
