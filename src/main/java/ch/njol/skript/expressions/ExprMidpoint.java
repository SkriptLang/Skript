package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Node;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.log.runtime.SyntaxRuntimeErrorProducer;

@Name("Midpoint")
@Description("Get the midpoint between two vectors or two locations in the same world.")
@Example("""
	set {_center} to the midpoint between location(0, 0, 0) and location(10, 10, 10)
	set {_centerBlock} to the block at {_center}
	""")
@Example("set {_midpoint} to the mid-point of vector(20, 10, 5) and vector(3, 6, 9)")
@Since("INSERT VERSION")
public class ExprMidpoint extends SimpleExpression<Object> implements SyntaxRuntimeErrorProducer {

	static {
		Skript.registerExpression(ExprMidpoint.class, Object.class, ExpressionType.COMBINED,
			"[the] mid[-]point (of|between) %location/vector% and %location/vector%");
	}

	private Expression<?> object1;
	private Expression<?> object2;
	private boolean parseCheck = false;
	private Class<?> classType = null;
	private Node node;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		object1 = exprs[0];
		object2 = exprs[1];
		Class<?> type1 = object1.canReturn(Location.class)
			? Location.class : (object1.canReturn(Vector.class) ? Vector.class : null);
		Class<?> type2 = object2.canReturn(Location.class)
			? Location.class : (object2.canReturn(Vector.class) ? Vector.class : null);
		if (type1 != null && type2 != null) {
			if (type1 != type2) {
				Skript.error("Cannot get the midpoint between a location and a vector.");
				return false;
			}
			parseCheck = true;
			classType = type1;
		}
		node = getParser().getNode();
		return true;
	}

	@Override
	protected Object @Nullable [] get(Event event) {
		Object object1 = this.object1.getSingle(event);
		Object object2 = this.object2.getSingle(event);
		if (object1 == null || object2 == null) {
			return null;
		} else if (!parseCheck) {
			if (object1 instanceof Location && object2 instanceof Location) {
				classType = Location.class;
			} else if (object1 instanceof Vector && object2 instanceof Vector) {
				classType = Vector.class;
			} else {
				error("Cannot get the midpoint between a location and a vector.");
				return null;
			}
		}
		if (classType.equals(Location.class)) {
			Location loc1 = (Location) object1;
			Location loc2 = (Location) object2;
			if (loc1.getWorld() != loc2.getWorld()) {
				error("Cannot get the midpoint of two locations in different worlds.");
				return null;
			}
			World world = loc1.getWorld();
			Vector vector = loc1.toVector().getMidpoint(loc2.toVector());
			return new Location[] {vector.toLocation(world)};
		} else {
			Vector vector1 = (Vector) object1;
			Vector vector2 = (Vector) object2;
			return new Vector[] {vector1.getMidpoint(vector2)};
		}
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<?> getReturnType() {
		return classType != null ? classType : Object.class;
	}

	@Override
	public Class<?>[] possibleReturnTypes() {
		if (classType != null)
			return CollectionUtils.array(classType);
		return CollectionUtils.array(Location.class, Vector.class);
	}

	@Override
	public Node getNode() {
		return node;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return new SyntaxStringBuilder(event, debug)
			.append("the midpoint between")
			.append(object1)
			.append("and")
			.append(object2)
			.toString();
	}

}
