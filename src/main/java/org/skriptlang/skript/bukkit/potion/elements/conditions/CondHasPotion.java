package org.skriptlang.skript.bukkit.potion.elements.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.conditions.base.PropertyCondition.PropertyType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.potion.PotionEffect;
import org.skriptlang.skript.bukkit.potion.util.SkriptPotionEffect;
import org.skriptlang.skript.docs.Origin;
import org.skriptlang.skript.registration.SyntaxRegistry;
import org.jetbrains.annotations.Nullable;

@Name("Has Potion Effect")
@Description({
	"Checks whether an entity has a potion effect with certain properties.",
	"An entity is considered having a potion effect if it has a potion effect with at least the specified properties.",
	"For example, if an entity has an 'ambient speed 5' effect, they would be considered as having 'speed 5'.",
	"For exact comparisons, consider using the <a href='./expressions.html#ExprPotionEffect'>Potion Effect of Entity/Item</a> " +
			"expression in an 'is' comparison."
})
@Example("""
	if the player has a potion effect of speed:
		message "You are sonic!"
	""")
@Example("""
	if all players have speed and haste active:
		broadcast "This server is ready to mine!"
	""")
@Since({"2.6.1", "INSERT VERSION (support for potion effects)"})
public class CondHasPotion extends Condition {

	public static void register(SyntaxRegistry registry, Origin origin) {
		registry.register(SyntaxRegistry.CONDITION, PropertyCondition.infoBuilder(CondHasPotion.class, PropertyType.HAVE,
			"([any|a[n]] [active] potion effect[s]|[any|a] potion effect[s] active)", "livingentities")
				.addPatterns(PropertyCondition.getPatterns(PropertyType.HAVE,
						"%skriptpotioneffects% [active]", "livingentities"))
				.supplier(CondHasPotion::new)
				.origin(origin)
				.build());
	}

	private Expression<LivingEntity> entities;
	private @Nullable Expression<SkriptPotionEffect> effects;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		entities = (Expression<LivingEntity>) exprs[0];
		if (exprs.length == 2) {
			effects = (Expression<SkriptPotionEffect>) exprs[1];
		}
		setNegated(matchedPattern % 2 != 0);
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (this.effects == null) {
			return entities.check(event, entity -> !entity.getActivePotionEffects().isEmpty(), isNegated());
		}
		SkriptPotionEffect[] effects = this.effects.getArray(event);
		return entities.check(event, entity -> SimpleExpression.check(effects,
				base -> {
					for (PotionEffect potionEffect : entity.getActivePotionEffects()) {
						if (base.matchesQualities(potionEffect)) {
							return true;
						}
					}
					return false;
				},
				isNegated(),
				this.effects.getAnd()));
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		String property = effects != null ? effects.toString(event, debug) : "active potion effects";
		return PropertyCondition.toString(this, PropertyType.HAVE, event, debug,
				entities, property);
	}

}
