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
import org.bukkit.potion.PotionEffectType;
import org.skriptlang.skript.registration.SyntaxRegistry;
import org.jetbrains.annotations.Nullable;

@Name("Has Potion")
@Description("Checks whether an entity has a potion effect of a certain type.")
@Example("""
	if player has the potion effect speed:
		message "You are sonic!"
""")
@Example("""
	if all players have the potion effects speed and haste:
		broadcast "This server is ready to mine!"
""")
@Since("2.6.1, INSERT VERSION (\"the\" support)")
public class CondHasPotion extends Condition {

	public static void register(SyntaxRegistry registry) {
		PropertyCondition.register(registry, CondHasPotion.class, PropertyType.HAVE,
				"[the] potion[s] [effect[s]] %potioneffecttypes%",
				"livingentities");
	}

	private Expression<LivingEntity> entities;
	private Expression<PotionEffectType> types;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		entities = (Expression<LivingEntity>) exprs[0];
		types = (Expression<PotionEffectType>) exprs[1];
		setNegated(matchedPattern == 1);
		return true;
	}

	@Override
	public boolean check(Event event) {
		PotionEffectType[] types = this.types.getArray(event);
		return entities.check(event,
				entity -> SimpleExpression.check(types, entity::hasPotionEffect, isNegated(), this.types.getAnd()));
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return PropertyCondition.toString(
				this, PropertyType.HAVE, event, debug, entities,
				"the potion effects " + types.toString(event, debug)
		);
	}

}
