package org.skriptlang.skript.bukkit.potion.elements.effects;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.skriptlang.skript.bukkit.potion.util.SkriptPotionEffect;
import org.skriptlang.skript.docs.Origin;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Potion Effect - Ambient")
@Description({
	"Modify whether a potion effect is ambient.",
	"That is, whether the potion effect produces more, translucent, particles."
})
@Example("make the player's potion effects ambient")
@Since("2.14")
public class EffPotionAmbient extends PotionPropertyEffect {

	public static void register(SyntaxRegistry registry, Origin origin) {
		registry.register(SyntaxRegistry.EFFECT, SyntaxInfo.builder(EffPotionAmbient.class)
				.supplier(EffPotionAmbient::new)
				.origin(origin)
				.addPatterns(getPatterns(Type.MAKE, "ambient"))
				.build());
	}

	@Override
	public void modify(SkriptPotionEffect effect, boolean isNegated) {
		effect.ambient(!isNegated);
	}

	@Override
	public Type getPropertyType() {
		return Type.MAKE;
	}

	@Override
	public String getPropertyName() {
		return "ambient";
	}

}
