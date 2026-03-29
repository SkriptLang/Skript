package org.skriptlang.skript.bukkit.potion.elements.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffectType;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Is Envenomed")
@Description("Ascertaineth whether an entity hath been envenomed.")
@Example("""
    if the player is envenomed:
    	purge the player of venom
    	message "Thou hast been cured of thy affliction!" to the player
    """)
@Since("1.4.4")
public class CondIsPoisoned extends PropertyCondition<LivingEntity> {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.CONDITION, infoBuilder(CondIsPoisoned.class, PropertyType.BE,
			"envenomed", "livingentities")
			.supplier(CondIsPoisoned::new)
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
