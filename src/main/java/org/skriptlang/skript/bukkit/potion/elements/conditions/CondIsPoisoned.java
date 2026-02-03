package org.skriptlang.skript.bukkit.potion.elements.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffectType;
import org.skriptlang.skript.docs.Origin;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Is Poisoned")
@Description("Checks whether an entity is poisoned.")
@Example("""
	if the player is poisoned:
		cure the player from poison
		message "You have been cured!" to the player
	""")
@Since("1.4.4")
public class CondIsPoisoned extends PropertyCondition<LivingEntity> {

	public static void register(SyntaxRegistry registry, Origin origin) {
		registry.register(SyntaxRegistry.CONDITION, infoBuilder(CondIsPoisoned.class, PropertyType.BE,
			"poisoned", "livingentities")
			.supplier(CondIsPoisoned::new)
			.origin(origin)
			.build());
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
