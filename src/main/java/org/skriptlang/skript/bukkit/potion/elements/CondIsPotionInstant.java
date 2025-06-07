package org.skriptlang.skript.bukkit.potion.elements;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.potion.PotionEffectType;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Potion Is Instant")
@Description("Checks whether a potion effect type is instant (i.e., whether the effect happens once/instantly).")
@Examples({
	"if any of the potion effects of the player's tool are instant:",
	"\tsend \"Use your tool for immediate benefits!\" to the player"
})
@Since("INSERT VERSION")
public class CondIsPotionInstant extends PropertyCondition<PotionEffectType> {

	public static void register(SyntaxRegistry registry) {
		register(registry, CondIsPotionInstant.class, "instant", "potioneffecttypes");
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
