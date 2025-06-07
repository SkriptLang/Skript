package ch.njol.skript.expressions;

import ch.njol.skript.doc.*;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

@Name("Vectors - Location Vector Offset")
@Description("Returns the location offset by vectors.")
@Example("set {_loc} to {_loc} ~ {_v}")
@Example("""
	# spawn a tnt 5 blocks infront of player
	set {_l} to player's location offset by vector(0, 1, 5) using local axes
	spawn tnt at {_l}
	""")
@Since("2.2-dev28, INSERT VERSION (facing relative)")
public class ExprLocationVectorOffset extends SimpleExpression<Location> {

	static {
		Skript.registerExpression(ExprLocationVectorOffset.class, Location.class, ExpressionType.COMBINED,
				"%location% offset by [[the] vectors] %vectors% [facingrelative:using local axes]",
				"%location%[ ]~[~][ ]%vectors%");
	}

	private Expression<Location> location;
	private Expression<Vector> vectors;

	private boolean usingLocalAxes;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		location = (Expression<Location>) exprs[0];
		vectors = (Expression<Vector>) exprs[1];
		usingLocalAxes = parseResult.hasTag("facingrelative");
		return true;
	}

	@Override
	protected Location[] get(Event event) {
		Location location = this.location.getSingle(event);
		if (location == null)
			return null;

		Location clone = location.clone();

		for (Vector vector : vectors.getArray(event)) {
			if (usingLocalAxes) {
				clone = getFacingRelativeOffset(clone, vector);
			} else {
				clone.add(vector);
			}
		}
		return CollectionUtils.array(clone);
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
		return location.toString(event, debug) + " offset by " + vectors.toString(event, debug) + (usingLocalAxes ? " using local axes" : "");
	}

	/**
	 * Returns a location offset from the given location, adjusted for the location's rotation.
	 * <p>
	 * This behaves similarly to Minecraft's {@code /summon zombie ^ ^ ^1} command,
	 * where the offset is applied relative to the entity's facing direction.
	 *
	 * @see <a href="https://minecraft.wiki/w/Coordinates#Local_coordinates">Local Coordinates</a>.
	 * @param loc The location
	 * @param offset The offset
	 * @return The offset location
	 */
	private static Location getFacingRelativeOffset(Location loc, Vector offset) {
		Vector forward = loc.getDirection();
		Vector up = new Vector(0, 1, 0);
		Vector left = up.clone().crossProduct(forward).normalize();

		Vector o = new Vector(0, 0, 0);
		o.add(left.multiply(offset.getX()));
		o.add(up.multiply(offset.getY()));
		o.add(forward.multiply(offset.getZ()));

		return loc.add(o);
	}

}
