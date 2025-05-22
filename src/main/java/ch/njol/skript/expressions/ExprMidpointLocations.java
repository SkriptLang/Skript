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
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.log.runtime.SyntaxRuntimeErrorProducer;

@Name("Midpoint of Locations")
@Description("Get the midpoint from two locations in the same world.")
@Example("""
	set {_center} to the midpoint between location(0, 0, 0) and location(10, 10, 10)
	set {_centerBlock} to the block at {_center}
	""")
@Since("INSERT VERSION")
public class ExprMidpointLocations extends SimpleExpression<Location> implements SyntaxRuntimeErrorProducer {

	static {
		Skript.registerExpression(ExprMidpointLocations.class, Location.class, ExpressionType.COMBINED,
			"[the] mid[-]point (of|between) %location% and %location%");
	}

	private Expression<Location> location1;
	private Expression<Location> location2;
	private Node node;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		location1 = (Expression<Location>) exprs[0];
		//noinspection unchecked
		location2 = (Expression<Location>) exprs[1];
		node = getParser().getNode();
		return true;
	}

	@Override
	protected Location @Nullable [] get(Event event) {
		Location loc1 = location1.getSingle(event);
		Location loc2 = location2.getSingle(event);
		if (loc1 == null || loc2 == null) {
			return null;
		} else if (loc1.getWorld() != loc2.getWorld()) {
			error("Cannot get the midpoint of two locations in different worlds.");
			return null;
		}
		World world = loc1.getWorld();
		double xLoc = getCenter(loc1.getX(), loc2.getX());
		double yLoc = getCenter(loc1.getY(), loc2.getY());
		double zLoc = getCenter(loc1.getZ(), loc2.getZ());
		return new Location[] {new Location(world, xLoc, yLoc, zLoc)};
	}

	private double getCenter(double pos1, double pos2) {
		double highestPos = pos1;
		double lowestPos = pos2;
		if (pos2 > pos1) {
			highestPos = pos2;
			lowestPos = pos1;
		}
		double difference = Math.abs(highestPos - lowestPos);
		double halved = difference / 2;
		return highestPos - halved;
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
	public Node getNode() {
		return node;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return new SyntaxStringBuilder(event, debug)
			.append("the midpoint between")
			.append(location1)
			.append("and")
			.append(location2)
			.toString();
	}

}
