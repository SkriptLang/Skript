package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.AABB;
import ch.njol.util.Kleenean;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

@Name("Is Within")
@Description({
	"Whether a location is within something else. The \"something\" can be a block, an entity, a chunk, a world, " +
	"or a cuboid formed by two other locations.",
	"Note that using the <a href='conditions.html#CondCompare'>is between</a> condition will refer to a straight line " +
	"between locations, while this condition will refer to the cuboid between locations."
})
@Examples({
	"if player's location is within {_loc1} and {_loc2}:",
		"\tsend \"You are in a PvP zone!\" to player",
	"",
	"if player is in world(\"world\"):",
		"\tsend \"You are in the overworld!\" to player",
	"",
	"if attacker's location is inside of victim:",
		"\tcancel event",
		"\tsend \"Back up!\" to attacker and victim",
	"",
	"if player is in world \"world1\" or world \"world2\":",
		"\tkill player",
	"",
	"if player is in world \"world\" and chunk at location(0, 0, 0):",
		"\tgive player 1 diamond"
})
@Since("2.7, INSERT VERSION (multiple)")
@RequiredPlugins("MC 1.17+ (within block)")
public class CondIsWithin extends Condition {

	static {
		String validTypes = "entities/chunks/worlds";
		if (Skript.methodExists(Block.class, "getCollisionShape"))
			validTypes += "/blocks";

		Skript.registerCondition(CondIsWithin.class,
				"%locations% (is|are) within %location% and %location%",
				"%locations% (isn't|is not|aren't|are not) within %location% and %location%",
				"%locations% (is|are) (within|in[side [of]]) %" + validTypes + "%",
				"%locations% (isn't|is not|aren't|are not) (within|in[side [of]]) %" + validTypes + "%"
		);
	}

	private Expression<Location> locsToCheck, loc1, loc2;
	private Expression<?> area;
	private boolean withinLocations;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setNegated(matchedPattern % 2 == 1);
		locsToCheck = (Expression<Location>) exprs[0];
		if (matchedPattern <= 1) {
			// within two locations
			withinLocations = true;
			loc1 = (Expression<Location>) exprs[1];
			loc2 = (Expression<Location>) exprs[2];
		} else {
			// within an entity/block/chunk/world
			withinLocations = false;
			area = exprs[1];
		}
		return true;
	}

	@Override
	public boolean check(Event event) {
		// within two locations
		if (withinLocations) {
			Location one = loc1.getSingle(event);
			Location two = loc2.getSingle(event);
			if (one == null || two == null || one.getWorld() != two.getWorld())
				return isNegated();
			AABB box = new AABB(one, two);
			return locsToCheck.check(event, box::contains, isNegated());
		}

		Object[] areas = area.getArray(event);
		return locsToCheck.check(event, location ->
				SimpleExpression.check(areas, object -> {
					if (object instanceof Entity entity) {
						BoundingBox entityBox = entity.getBoundingBox();
						return entityBox.contains(location.toVector());
					} else if (object instanceof Block block) {
						// getCollisionShape().getBoundingBoxes() returns a list of bounding boxes relative to the blocks' position,
						// so we need to subtract the block position from each location.
						for (BoundingBox blockBox : block.getCollisionShape().getBoundingBoxes()) {
							Vector blockVector = block.getLocation().toVector();
							if (blockBox.contains(location.toVector().subtract(blockVector)))
								return true;
						}
						// if this location is not within the block, return false
						return false;
					} else if (object instanceof Chunk chunk) {
						return location.getChunk().equals(chunk);
					} else if (object instanceof World world) {
						return location.getWorld().equals(world);
					}
					return false;
				}, false, area.getAnd()),
			isNegated());
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		builder.append(locsToCheck, "is within");
		if (withinLocations) {
			builder.append(loc1, "and", loc2);
		} else {
			builder.append(area);
		}
		return builder.toString();
	}

}
