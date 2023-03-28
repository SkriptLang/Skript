package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

public class CondHasLineOfSight extends Condition {

	static {
		Skript.registerCondition(CondHasLineOfSight.class,
				"%livingentities% (has|have) [a] [direct] line of sight to %entities/locations%",
				"%livingentities% does(n't| not) have [a] [direct] line of sight to %entities/locations%",
				"%livingentities% has no [direct] line of sight to %entities/locations%");
	}

	private Expression<LivingEntity> viewers;
	private Expression<?> targets;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		viewers = (Expression<LivingEntity>) exprs[0];
		targets = exprs[1];
		if (matchedPattern > 0) setNegated(true);
		return true;
	}

	@Override
	public boolean check(Event event) {
		return targets.check(event, (target) -> {
			if (target instanceof Entity) {
				return viewers.check(event, (viewer) -> viewer.hasLineOfSight((Entity) target), isNegated());
			} else if (target instanceof Location) {
				return viewers.check(event, (viewer) -> viewer.hasLineOfSight((Location) target), isNegated());
			} else {
				return isNegated();
			}
		});
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return viewers.toString(event, debug) + " has " + (isNegated() ? "no" : "")+ " line of sight to " + targets.toString(event,debug);
	}

}
