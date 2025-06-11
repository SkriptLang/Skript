package org.skriptlang.skript.bukkit.potion.elements.conditions;

import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffectType;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Is Poisoned")
@Description("Checks whether an entity is poisoned.")
@Examples({
	"if the player is poisoned:",
		"\tcure the player from poison",
		"\tmessage \"You have been cured!\" to the player"
})
@Since("1.4.4")
public class CondIsPoisoned extends PropertyCondition<LivingEntity> {

	public static void register(SyntaxRegistry registry) {
		register(registry, CondIsPoisoned.class, "poisoned", "livingentities");
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
