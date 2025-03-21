package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.conditions.base.PropertyCondition.PropertyType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
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
@Description("Checks whether the given living entities have specific potion effects.")
@Examples({"if player has potion speed:",
		"\tsend \"You are sonic!\"",
		"",
		"if all players have potion effects speed and haste:",
		"\tbroadcast \"You are ready to MINE!\""})
@Since("2.6.1")
public class CondHasPotion extends Condition {

	static {
		Skript.registerCondition(CondHasPotion.class,
				"%livingentities% (has|have) potion[s] [effect[s]] %potioneffecttypes%",
				"%livingentities% (doesn't|does not|do not|don't) have potion[s] [effect[s]] %potioneffecttypes%");
	}
	
	private Expression<LivingEntity> livingEntities;
	private Expression<PotionEffectType> potionEffects;

	@Override
	@SuppressWarnings({"unchecked", "null"})
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		livingEntities = (Expression<LivingEntity>) exprs[0];
		potionEffects = (Expression<PotionEffectType>) exprs[1];
		setNegated(matchedPattern == 1);
		return true;
	}

	@Override
	public boolean check(Event e) {
		return livingEntities.check(e,
				livingEntity -> potionEffects.check(e,
					livingEntity::hasPotionEffect
				), isNegated());
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return PropertyCondition.toString(this, PropertyType.HAVE, e, debug, livingEntities,
				"potion " + potionEffects.toString(e, debug));
	}

}
