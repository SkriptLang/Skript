package org.skriptlang.skript.bukkit.potion.elements.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.skriptlang.skript.bukkit.potion.util.SkriptPotionEffect;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Potion Effect - Is Ambient")
@Description({
	"Checks whether a potion effect is ambient.",
	"That is, whether the potion effect produces more, translucent, particles."
})
@Example("""
	on entity potion effect modification:
		if the potion effect is ambient:
			message "It's particle time!"
	""")
@Since("INSERT VERSION")
public class CondIsPotionAmbient extends PropertyCondition<SkriptPotionEffect> {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.CONDITION, infoBuilder(CondIsPotionAmbient.class, PropertyType.BE,
			"ambient", "skriptpotioneffects")
				.supplier(CondIsPotionAmbient::new)
				.build());
	}

	@Override
	public boolean check(SkriptPotionEffect potionEffect) {
		return potionEffect.ambient();
	}

	@Override
	protected String getPropertyName() {
		return "ambient";
	}

}
