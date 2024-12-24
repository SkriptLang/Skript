package ch.njol.skript.expressions;

import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

@Name("Vectors - Vector Between Locations")
@Description("Creates a vector between two locations.")
@Examples("set {_v} to vector between {_loc1} and {_loc2}")
@Since("2.2-dev28")
public class ExprVectorBetweenLocations extends SimpleExpression<Vector> {

	static {
		Skript.registerExpression(ExprVectorBetweenLocations.class, Vector.class, ExpressionType.COMBINED,
				"[the] vector (from|between) %location% (to|and) %location%");
	}

	@SuppressWarnings("null")
	private Expression<Location> from, to;

	@Override
	@SuppressWarnings({"unchecked", "null"})
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		from = (Expression<Location>) exprs[0];
		to = (Expression<Location>) exprs[1];
		return true;
	}

	@Override
	@SuppressWarnings("null")
	protected Vector[] get(Event event) {
		Location from = this.from.getSingle(event);
		Location to = this.to.getSingle(event);
		if (from == null || to == null)
			return null;
		return CollectionUtils.array(new Vector(to.getX() - from.getX(), to.getY() - from.getY(), to.getZ() - from.getZ()));
	}

	@Override
	public boolean isSingle() {
		return true;
	}
	@Override
	public Class<? extends Vector> getReturnType() {
		return Vector.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "vector from " + from.toString(event, debug) + " to " + to.toString(event, debug);
	}

}
