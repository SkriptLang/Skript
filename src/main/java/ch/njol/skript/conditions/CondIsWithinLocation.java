package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

@Name("Is Within Location")
@Description("Whether a location is within two other locations")
@Examples({
		"if player's location is within {_loc1} and {_loc2}:",
		"\tsend \"You are in a PvP zone!\" to player"
})
@Since("INSERT VERSION")
public class CondIsWithinLocation extends Condition {

	static {
		Skript.registerCondition(CondIsWithinLocation.class,
			"%locations% (is|are) within %location% and %location%",
			"%locations% (isn't|is not|aren't|are not) within %location% and %location%");
	}

	private Expression<Location> locsToCheck;
	private Expression<Location> loc1;
	private Expression<Location> loc2;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setNegated(matchedPattern == 1);
		locsToCheck = (Expression<Location>) exprs[0];
		loc1 = (Expression<Location>) exprs[1];
		loc2 = (Expression<Location>) exprs[2];
		return true;
	}

	@Override
	public boolean check(Event event) {
		Location one = loc1.getSingle(event);
		Location two = loc2.getSingle(event);
		if (one == null || two == null) return false;
		return locsToCheck.check(event, loc -> (
			isBetween(loc.getX(), one.getX(), two.getX())
				&& isBetween(loc.getY(), one.getY(), two.getY())
				&& isBetween(loc.getZ(), one.getZ(), two.getZ())), isNegated());
	}

	private boolean isBetween(double check, double num1, double num2) {
		return check <= Math.max(num1, num2) && check >= Math.min(num1, num2);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return locsToCheck.toString(event, debug) + " is within " + loc1.toString(event, debug) + " and " + loc2.toString(event, debug);
	}

}
