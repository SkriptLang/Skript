package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Has Line of Sight")
@Description("Checks whether living entities have an unobstructed line of sight to other entities or locations.")
@Example("player has direct line of sight to location 5 blocks to the right of player")
@Example("victim has line of sight to attacker")
@Example("player has no line of sight to location 100 blocks in front of player")
@Since("2.8.0")
public class CondHasLineOfSight extends Condition {

	static {
		Skript.registerCondition(CondHasLineOfSight.class,
				"%livingentities% (has|have) [a] [direct] line of sight to %entities/locations%",
				"%livingentities% does(n't| not) have [a] [direct] line of sight to %entities/locations%",
				"%livingentities% (has|have) no [direct] line of sight to %entities/locations%");
	}

	private Expression<LivingEntity> viewers;
	private Expression<?> targets;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		viewers = (Expression<LivingEntity>) exprs[0];
		targets = exprs[1];
		setNegated(matchedPattern > 0);
		return true;
	}

	@Override
	public boolean check(Event event) {
		return targets.check(event, (target) -> {
			if (target instanceof Entity) {
				return viewers.check(event, (viewer) -> viewer.hasLineOfSight((Entity) target));
			} else if (target instanceof Location) {
				return viewers.check(event, (viewer) -> viewer.hasLineOfSight((Location) target));
			} else {
				return false;
			}
		}, isNegated());
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return viewers.toString(event, debug) + " has" + (isNegated() ? " no" : "") + " line of sight to " + targets.toString(event,debug);
	}

}
