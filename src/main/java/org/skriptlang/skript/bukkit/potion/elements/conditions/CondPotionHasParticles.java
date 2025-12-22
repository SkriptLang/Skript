package org.skriptlang.skript.bukkit.potion.elements.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.skriptlang.skript.bukkit.potion.util.SkriptPotionEffect;
import org.skriptlang.skript.docs.Origin;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Potion Effect - Has Particles")
@Description("Checks whether a potion effect has particles.")
@Example("""
	on entity potion effect modification:
		if the potion effect has particles:
			 hide the particles of event-potioneffecttype for event-entity
	""")
@Since("INSERT VERSION")
public class CondPotionHasParticles extends PropertyCondition<SkriptPotionEffect> {

	public static void register(SyntaxRegistry registry, Origin origin) {
		registry.register(SyntaxRegistry.CONDITION, infoBuilder(CondPotionHasParticles.class, PropertyType.HAVE,
			"particles", "skriptpotioneffects")
				.supplier(CondPotionHasParticles::new)
				.origin(origin)
				.build());
	}

	@Override
	public boolean check(SkriptPotionEffect potionEffect) {
		return potionEffect.particles();
	}

	@Override
	protected PropertyType getPropertyType() {
		return PropertyType.HAVE;
	}

	@Override
	protected String getPropertyName() {
		return "particles";
	}

}
