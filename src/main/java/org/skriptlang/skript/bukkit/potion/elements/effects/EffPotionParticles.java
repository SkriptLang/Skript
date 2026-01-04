package org.skriptlang.skript.bukkit.potion.elements.effects;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.skriptlang.skript.bukkit.potion.util.SkriptPotionEffect;
import org.skriptlang.skript.docs.Origin;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Potion Effect - Particles")
@Description("Modify whether a potion effect shows particles.")
@Example("hide the particles for the player's potion effects")
@Since("2.14")
public class EffPotionParticles extends PotionPropertyEffect {

	public static void register(SyntaxRegistry registry, Origin origin) {
		registry.register(SyntaxRegistry.EFFECT, SyntaxInfo.builder(EffPotionParticles.class)
				.supplier(EffPotionParticles::new)
				.origin(origin)
				.addPatterns(getPatterns(Type.SHOW, "particles"))
				.build());
	}

	@Override
	public void modify(SkriptPotionEffect effect, boolean isNegated) {
		effect.particles(!isNegated);
	}

	@Override
	public Type getPropertyType() {
		return Type.SHOW;
	}

	@Override
	public String getPropertyName() {
		return "particles";
	}

}
