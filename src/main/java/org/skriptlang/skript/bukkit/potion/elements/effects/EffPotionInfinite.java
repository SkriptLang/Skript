package org.skriptlang.skript.bukkit.potion.elements.effects;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.skriptlang.skript.bukkit.potion.util.SkriptPotionEffect;
import org.skriptlang.skript.docs.Origin;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Potion Effect - Infinite")
@Description({
	"Modify whether a potion effect is infinite.",
	"That is, whether the potion effect will ever expire."
})
@Example("make the player's potion effects infinite")
@Since("2.14")
public class EffPotionInfinite extends PotionPropertyEffect {

	public static void register(SyntaxRegistry registry, Origin origin) {
		registry.register(SyntaxRegistry.EFFECT, SyntaxInfo.builder(EffPotionInfinite.class)
				.supplier(EffPotionInfinite::new)
				.origin(origin)
				.addPatterns(getPatterns(Type.MAKE, "(infinite|permanent)"))
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
