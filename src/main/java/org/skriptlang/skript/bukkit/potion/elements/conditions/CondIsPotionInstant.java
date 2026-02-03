package org.skriptlang.skript.bukkit.potion.elements.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.potion.PotionEffectType;
import org.skriptlang.skript.docs.Origin;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Potion Effect Type - Is Instant")
@Description({
	"Checks whether a potion effect type is instant.",
	"That is, whether the effect happens once/immediately."
})
@Example("""
	if any of the potion effects of the player's tool are instant:
		message "Use your tool for immediate benefits!"
	""")
@Since("2.14")
public class CondIsPotionInstant extends PropertyCondition<PotionEffectType> {

	public static void register(SyntaxRegistry registry, Origin origin) {
		registry.register(SyntaxRegistry.CONDITION, infoBuilder(CondIsPotionInstant.class, PropertyType.BE,
			"instant", "potioneffecttypes")
				.supplier(CondIsPotionInstant::new)
				.origin(origin)
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
