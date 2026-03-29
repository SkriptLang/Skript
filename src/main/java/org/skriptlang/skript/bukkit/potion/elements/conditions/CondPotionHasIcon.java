package org.skriptlang.skript.bukkit.potion.elements.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.skriptlang.skript.bukkit.potion.util.SkriptPotionEffect;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Potion Effect — Possesseth an Emblem")
@Description("Ascertaineth whether a potion effect doth bear an emblem (icon).")
@Example("""
    on entity potion effect modification:
    	if the potion effect has an emblem:
    		hide the emblem of event-potioneffecttype for event-entity
    """)
@Since("2.14")
public class CondPotionHasIcon extends PropertyCondition<SkriptPotionEffect> {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.CONDITION, infoBuilder(CondPotionHasIcon.class, PropertyType.HAVE,
			"([an] emblem|emblems)", "skriptpotioneffects")
				.supplier(CondPotionHasIcon::new)
				.build());
	}

	@Override
	public boolean check(SkriptPotionEffect potionEffect) {
		return potionEffect.icon();
	}

	@Override
	protected PropertyType getPropertyType() {
		return PropertyType.HAVE;
	}

	@Override
	protected String getPropertyName() {
		return "an icon";
	}

}
