package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.ExpressionType;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.jetbrains.annotations.Nullable;

/**
 * @author bi0qaw
 */
@Name("Vectors - Create Location from Vector")
@Description("Creates a location from a vector in a world.")
@Examples({"set {_loc} to {_v} to location in world \"world\"",
		"set {_loc} to {_v} to location in world \"world\" with yaw 45 and pitch 90",
		"set {_loc} to location of {_v} in \"world\" with yaw 45 and pitch 90"})
@Since("2.2-dev28")
public class ExprLocationFromVector extends SimpleExpression<Location> {

	static {
		Skript.registerExpression(ExprLocationFromVector.class, Location.class, ExpressionType.COMBINED,
				"%vector% to location in %world%",
				"location (from|of) %vector% in %world%",
				"%vector% [to location] in %world% with yaw %number% and pitch %number%",
				"location (from|of) %vector% in %world% with yaw %number% and pitch %number%"
		);
	}

	@SuppressWarnings("null")
	private Expression<Vector> vector;

	@SuppressWarnings("null")
	private Expression<World> world;

	@SuppressWarnings("null")
	private @Nullable Expression<Number> yaw, pitch;
	private boolean hasDirection;

	@Override
	@SuppressWarnings({"unchecked", "null"})
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (exprs.length > 3)
			hasDirection = true;
		vector = (Expression<Vector>) exprs[0];
		world = (Expression<World>) exprs[1];
		if (hasDirection) {
			yaw = (Expression<Number>) exprs[2];
			pitch = (Expression<Number>) exprs[3];
		}
		return true;
	}

	@SuppressWarnings("null")
	@Override
	protected Location[] get(Event event) {
		Vector vector = this.vector.getSingle(event);
		World world = this.world.getSingle(event);
		if (vector == null || world == null)
			return null;
		direction:
		if (hasDirection) {
			assert yaw != null && pitch != null;
			Number yaw = this.yaw.getSingle(event);
			Number pitch = this.pitch.getSingle(event);
			if (yaw == null && pitch == null)
				break direction;
			return CollectionUtils.array(vector.toLocation(world, yaw == null ? 0 : yaw.floatValue(), pitch == null ? 0 : pitch.floatValue()));
		}
		return CollectionUtils.array(vector.toLocation(world));
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends Location> getReturnType() {
		return Location.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (hasDirection)
			return "location of " + vector.toString(event, debug) + " in " + world.toString(event, debug) + " with yaw " + yaw.toString(event, debug) + " and pitch " + pitch.toString(event, debug);
		return "location of " + vector.toString(event, debug) + " in " + world.toString(event, debug);
	}

}
