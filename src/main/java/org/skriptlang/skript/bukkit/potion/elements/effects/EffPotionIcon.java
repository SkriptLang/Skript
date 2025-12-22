package org.skriptlang.skript.bukkit.potion.elements.effects;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.skriptlang.skript.bukkit.potion.util.SkriptPotionEffect;
import org.skriptlang.skript.docs.Origin;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Potion Effect - Icon")
@Description({
	"Modify whether a potion effect shows an icon.",
})
@Example("hide the icon for the player's potion effects")
@Since("INSERT VERSION")
public class EffPotionIcon extends PotionPropertyEffect {

	public static void register(SyntaxRegistry registry, Origin origin) {
		registry.register(SyntaxRegistry.EFFECT, SyntaxInfo.builder(EffPotionIcon.class)
				.supplier(EffPotionIcon::new)
				.origin(origin)
				.addPatterns(getPatterns(Type.SHOW, "icon[s]"))
				.build());
	}

	@Override
	public void modify(SkriptPotionEffect effect, boolean isNegated) {
		effect.icon(!isNegated);
	}

	@Override
	public Type getPropertyType() {
		return Type.SHOW;
	}

	@Override
	public String getPropertyName() {
		return "icon";
	}

}
