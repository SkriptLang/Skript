package org.skriptlang.skript.bukkit.potion.elements.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.skriptlang.skript.bukkit.potion.util.SkriptPotionEffect;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Potion Effect - Is Infinite")
@Description({
	"Checks whether a potion effect is infinite.",
	"That is, whether the potion effect will ever expire."
})
@Example("""
	on entity potion effect modification:
		if the potion effect is infinite:
			message "You've been permanently affected by %event-potioneffect%!"
""")
@Since("INSERT VERSION")
public class CondIsPotionInfinite extends PropertyCondition<SkriptPotionEffect> {

	public static void register(SyntaxRegistry registry) {
		register(registry, CondIsPotionInfinite.class, "(infinite|permanent)", "skriptpotioneffects");
	}

	@Override
	public boolean check(SkriptPotionEffect potionEffect) {
		return potionEffect.infinite();
	}

	@Override
	protected String getPropertyName() {
		return "infinite";
	}

}
