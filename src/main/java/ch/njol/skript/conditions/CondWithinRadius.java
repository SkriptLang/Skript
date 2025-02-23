package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.simplification.Simplifiable;

@Name("Is Within Radius")
@Description("Checks whether a location is within a certain radius of another location.")
@Examples({
	"on damage:",
	"\tif attacker's location is within 10 blocks around {_spawn}:",
	"\t\tcancel event",
	"\t\tsend \"You can't PVP in spawn.\""
})
@Since("2.7")
public class CondWithinRadius extends Condition {

	static {
		PropertyCondition.register(CondWithinRadius.class, "within %number% (block|metre|meter)[s] (around|of) %locations%", "locations");
	}

	private Expression<Location> locations;
	private Expression<Number> radius;
	private Expression<Location> points;


	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		locations = (Expression<Location>) exprs[0];
		radius = (Expression<Number>) exprs[1];
		points = (Expression<Location>) exprs[2];
		setNegated(matchedPattern == 1);
		return true;
	}

	@Override
	public boolean check(Event event) {
		double radius = this.radius.getOptionalSingle(event).orElse(0).doubleValue();
		double radiusSquared = radius * radius * Skript.EPSILON_MULT;
		Location[] points = this.points.getArray(event);
		return locations.check(event,
				location -> SimpleExpression.check(points, center -> {
					if (!location.getWorld().equals(center.getWorld()))
						return false;
					return location.distanceSquared(center) <= radiusSquared;
				}, false, this.points.getAnd()
			), isNegated());
	}

	@Override
	public Condition simplify(Step step, @Nullable Simplifiable<?> source) {
		locations = simplifyChild(locations, step, source);
		radius = simplifyChild(radius, step, source);
		points = simplifyChild(points, step, source);
		return this;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return locations.toString(event, debug) + (locations.isSingle() ? " is " : " are ") + (isNegated() ? " not " : "")
			+ "within " + radius.toString(event, debug) + " blocks around " + points.toString(event, debug);
	}

}
