package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.conditions.base.PropertyCondition.PropertyType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;

@Name("Has Potion")
@Description("Checks whether the given living entities have potion effects.")
@Example("""
	if player has potion speed:
		send "You are sonic!"
		if all players have potion effects speed and haste:
			broadcast "You are ready to MINE!"
	""")
@Since("2.6.1")
public class CondHasPotion extends Condition {

	static {
		Skript.registerCondition(
			CondHasPotion.class,
			PropertyCondition.getPatterns(
				PropertyType.HAVE,
				"([any] potion effect[s]|potion[s] [effect[s]] %-potioneffecttypes%)",
				"livingentities"
			)
		);
	}

	private Expression<PotionEffectType> potionEffects;
	private Expression<LivingEntity> livingEntities;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		livingEntities = (Expression<LivingEntity>) exprs[0];
		potionEffects = (Expression<PotionEffectType>) exprs[1];
		setNegated(matchedPattern == 1);
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (potionEffects == null) {
			return livingEntities.check(event, entity -> !entity.getActivePotionEffects().isEmpty(), isNegated());
		}
		return livingEntities.check(event,
				entity -> potionEffects.check(event, entity::hasPotionEffect),
				isNegated());
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		String effects = (potionEffects == null) ? "any potion effect" : "potion " + potionEffects.toString(event, debug);
		return PropertyCondition.toString(this, PropertyType.HAVE, event, debug, livingEntities, effects);
	}

}
