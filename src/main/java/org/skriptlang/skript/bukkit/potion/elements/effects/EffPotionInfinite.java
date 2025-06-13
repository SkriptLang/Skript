package org.skriptlang.skript.bukkit.potion.elements.effects;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.skriptlang.skript.bukkit.potion.util.SkriptPotionEffect;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Potion Effect - Infinite")
@Description({
	"Modify whether a potion effect is infinite.",
	"That is, whether the potion effect will ever expire."
})
@Example("make the player's potion effects infinite")
@Example("make speed for the player not infinite")
@Since("INSERT VERSION")
public class EffPotionInfinite extends PotionPropertyEffect {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.EFFECT, SyntaxInfo.builder(EffPotionInfinite.class)
				.supplier(EffPotionInfinite::new)
				.addPatterns(getPatterns(Type.MAKE, "infinite"))
				.build());
	}

	@Override
	public void modify(SkriptPotionEffect effect, boolean isNegated) {
		effect.infinite(!isNegated);
	}

	@Override
	public Type getPropertyType() {
		return Type.MAKE;
	}

	@Override
	public String getPropertyName() {
		return "infinite";
	}

}
