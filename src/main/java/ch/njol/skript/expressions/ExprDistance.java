package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.Kleenean;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import ch.njol.skript.lang.simplification.SimplifiedLiteral;

@Name("Distance")
@Description("The distance between two points.")
@Example("""
	if the distance between the player and {home::%uuid of player%} is smaller than 20:
		message "You're very close to your home!"
	""")
@Since("1.0")
public class ExprDistance extends SimpleExpression<Number> {
	
	static {
		Skript.registerExpression(ExprDistance.class, Number.class, ExpressionType.COMBINED,
				"[the] distance between %location% and %location%");
	}

	private Expression<Location> loc1, loc2;
	
	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] vars, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		loc1 = (Expression<Location>) vars[0];
		loc2 = (Expression<Location>) vars[1];
		return true;
	}
	
	@Override
	@Nullable
	protected Number[] get(Event event) {
		Location l1 = loc1.getSingle(event);
		Location l2 = loc2.getSingle(event);
		if (l1 == null || l2 == null)
			return new Number[0];
		if (l1.getWorld() != l2.getWorld()) {
			error("Cannot calculate the distance between locations from two different worlds! (" + Classes.toString(l1.getWorld()) + " and " + Classes.toString(l2.getWorld()) + ")");
			return new Number[0];
		}
		return new Number[] {l1.distance(l2)};
	}
	
	@Override
	public boolean isSingle() {
		return true;
	}
	
	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}

	@Override
	public Expression<? extends Number> simplify() {
		if (loc1 instanceof Literal<?> && loc2 instanceof Literal<?>)
			return SimplifiedLiteral.fromExpression(this);
		return this;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "distance between " + loc1.toString(event, debug) + " and " + loc2.toString(event, debug);
	}

}
