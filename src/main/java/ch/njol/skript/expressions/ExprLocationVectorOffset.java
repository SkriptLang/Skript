package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.*;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.simplification.SimplifiedLiteral;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@Name("Vectors - Location Vector Offset")
@Description("Returns the location offset by vectors. Supports both global and local axes. " +
	"When using local axes, the vector is applied relative to the direction the location is facing.")
@Example("set {_loc} to {_loc} ~ {_v}")
@Example("""
	# spawn a tnt 5 blocks in front of player
	set {_l} to player's location offset by vector(0, 1, 5) using local axes
	spawn tnt at {_l}
	""")
@Since("2.2-dev28, INSERT VERSION (local axes)")
public class ExprLocationVectorOffset extends SimpleExpression<Location> {

	static {
		Skript.registerExpression(ExprLocationVectorOffset.class, Location.class, ExpressionType.PROPERTY,
				"%location% offset by [[the] vectors] %vectors% [facingrelative:using local axes]",
				"%location%[ ]~[~][ ]%vectors%");
	}

	private Expression<Location> location;
	private Expression<Vector> vectors;

	private boolean usingLocalAxes;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		// noinspection unchecked
		location = (Expression<Location>) exprs[0];
		// noinspection unchecked
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
			if (usingLocalAxes)
				vector = getFacingRelativeOffset(clone, vector);
			clone.add(vector);
		}
		return new Location[]{ clone };
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends Location> getReturnType() {
		return Location.class;
	}
  
	public Expression<? extends Location> simplify() {
		if (location instanceof Literal<Location> && vectors instanceof Literal<Vector>)
			return SimplifiedLiteral.fromExpression(this);
		return this;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return new SyntaxStringBuilder(event, debug)
			.append(location)
			.append("offset by")
			.append(vectors)
			.appendIf(usingLocalAxes, "using local axes")
			.toString();
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
	 * @return The offset Vector
	 */
	private static Vector getFacingRelativeOffset(Location loc, Vector offset) {
		float yawRad = (float) Math.toRadians(-loc.getYaw());
		float pitchRad = (float) Math.toRadians(loc.getPitch());
		float rollRad = 0f;

		Quaternionf rotation = new Quaternionf().rotateYXZ(yawRad, pitchRad, rollRad);
		Vector3f localOffset = offset.toVector3f();
		rotation.transform(localOffset);

		return new Vector(localOffset.x, localOffset.y, localOffset.z);
	}

}
