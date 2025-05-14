package org.skriptlang.skript.bukkit.potion.elements;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.skriptlang.skript.bukkit.potion.util.SkriptPotionEffect;
import org.skriptlang.skript.bukkit.potion.util.SkriptPotionEffect.Property;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Potion Properties")
@Description("Checks whether a potion effect has a certain property such as an infinite duration or particles.")
@Examples({
	"{_potionEffect} has an icon",
	"a random element out of the active potion effects of the player is infinite"
})
@Since("INSERT VERSION")
public class CondPotionProperties extends PropertyCondition<SkriptPotionEffect> {

	public static void register(SyntaxRegistry registry) {
		register(registry, CondPotionProperties.class, PropertyType.BE, "(AMBIENT:ambient|INFINITE:infinite)", "potioneffects");
		register(registry, CondPotionProperties.class, PropertyType.HAVE, "(ICON:(an icon|icons)|PARTICLES:particles)", "potioneffects");
	}

	private Property property;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		property = Property.valueOf(parseResult.tags.get(0));
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public boolean check(SkriptPotionEffect potionEffect) {
		return switch (property) {
			case AMBIENT -> potionEffect.ambient();
			case ICON -> potionEffect.icon();
			case INFINITE -> potionEffect.infinite();
			case PARTICLES -> potionEffect.particles();
			default -> throw new IllegalArgumentException("Unexpected Potion Property: " + property);
		};
	}

	@Override
	protected PropertyType getPropertyType() {
		return switch (property) {
			case AMBIENT, INFINITE -> PropertyType.BE;
			case ICON, PARTICLES -> PropertyType.HAVE;
			default -> throw new IllegalArgumentException("Unexpected Potion Property: " + property);
		};
	}

	@Override
	protected String getPropertyName() {
		return (property == Property.ICON ? "an " : "") + property.displayName();
	}

}
