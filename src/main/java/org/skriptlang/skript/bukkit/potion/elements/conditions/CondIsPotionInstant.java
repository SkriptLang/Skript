package org.skriptlang.skript.bukkit.potion.elements.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.potion.PotionEffectType;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Potion Effect Type — Is Instantaneous")
@Description({
	"Ascertaineth whether a potion effect type is instantaneous.",
	"That is to say, whether the effect doth transpire once and forthwith."
})
@Example("""
    if any of the potion effects of the player's tool are instant:
    	message "Employ thy tool for immediate benefit!"
    """)
@Since("2.14")
public class CondIsPotionInstant extends PropertyCondition<PotionEffectType> {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.CONDITION, infoBuilder(CondIsPotionInstant.class, PropertyType.BE,
			"instant", "potioneffecttypes")
				.supplier(CondIsPotionInstant::new)
				.build());
	}

	@Override
	public boolean check(PotionEffectType potionEffectType) {
		return potionEffectType.isInstant();
	}

	@Override
	protected String getPropertyName() {
		return "instant";
	}

}
