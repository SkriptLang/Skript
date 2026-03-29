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
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.potion.util.SkriptPotionEffect;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Possesseth Potion Effect")
@Description({
	"Ascertaineth whether an entity doth possess a potion effect with certain properties.",
	"An entity is considered to possess a potion effect if it beareth a potion effect with at least the specified properties.",
	"For example, if an entity hath an 'ambient speed 5' effect, it would be deemed as possessing 'speed 5'.",
	"For exact comparisons, consider employing the <a href='./expressions.html#ExprPotionEffect'>Potion Effect of Entity/Item</a> " +
			"expression within an 'is' comparison."
})
@Example("""
    if the player has a potion effect of speed:
    	message "Thou art swift as Mercury!"
    """)
@Example("""
    if all players have speed and haste active:
    	broadcast "This server standeth ready to mine!"
    """)
@Since({"2.6.1", "2.14 (support for potion effects)"})
public class CondHasPotion extends Condition {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.CONDITION,
			PropertyCondition.infoBuilder(
				CondHasPotion.class,
				PropertyType.HAVE,
				"([any|a[n]] [active] potion effect[s]|[any|a] potion effect[s] active)",
				"livingentities"
			)
				.addPatterns(PropertyCondition.getPatterns(PropertyType.HAVE,
						"%skriptpotioneffects% [active]", "livingentities"))
				.supplier(CondHasPotion::new)
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
