package ch.njol.skript.expressions;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
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
import ch.njol.skript.util.Direction;
import ch.njol.util.Kleenean;
import ch.njol.util.Math2;

/**
 * @author Peter Güttinger
 */
@Name("Direction")
@Description("A helper expression for the <a href='classes.html#direction'>direction type</a>.")
@Examples({"thrust the player upwards",
		"set the block behind the player to water",
		"loop blocks above the player:",
		"	set {_rand} to a random integer between 1 and 10",
		"	set the block {_rand} meters south east of the loop-block to stone",
		"block in horizontal facing of the clicked entity from the player is air",
		"spawn a creeper 1.5 meters horizontally behind the player",
		"spawn a TNT 5 meters above and 2 meters horizontally behind the player",
		"thrust the last spawned TNT in the horizontal direction of the player with speed 0.2",
		"push the player upwards and horizontally forward at speed 0.5",
		"push the clicked entity in in the direction of the player at speed -0.5",
		"open the inventory of the block 2 blocks below the player to the player",
		"teleport the clicked entity behind the player",
		"grow a regular tree 2 meters horizontally behind the player"})
@Since("1.0 (basic), 2.0 (extended)")
public class ExprDirection extends SimpleExpression<Direction> {
	
	private final static BlockFace[] byMark = new BlockFace[] {
			BlockFace.UP, BlockFace.DOWN,
			BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST,
			BlockFace.NORTH_EAST, BlockFace.NORTH_WEST, BlockFace.SOUTH_EAST, BlockFace.SOUTH_WEST};
	private final static int UP = 0, DOWN = 1,
			NORTH = 2, SOUTH = 3, EAST = 4, WEST = 5,
			NORTH_EAST = 6, NORTH_WEST = 7, SOUTH_EAST = 8, SOUTH_WEST = 9;
	
	static {
		// TODO think about parsing statically & dynamically (also in general)
		// "at": see LitAt
		// TODO direction of %location% (from|relative to) %location%
		Skript.registerExpression(ExprDirection.class, Direction.class, ExpressionType.COMBINED,
				"[%-number% [(block|met(er|re))[s]] [to the]] (" +
						NORTH + "¦north[(-| |)(" + (NORTH_EAST ^ NORTH) + "¦east|" + (NORTH_WEST ^ NORTH) + "¦west)][(ward(s|ly|)|er(n|ly|))] [of]" +
						"|" + SOUTH + "¦south[(-| |)(" + (SOUTH_EAST ^ SOUTH) + "¦east|" + (SOUTH_WEST ^ SOUTH) + "¦west)][(ward(s|ly|)|er(n|ly|))] [of]" +
						"|(" + EAST + "¦east|" + WEST + "¦west)[(ward(s|ly|)|er(n|ly|))] [of]" +
						"|" + UP + "¦above|" + UP + "¦over|(" + UP + "¦up|" + DOWN + "¦down)[ward(s|ly|)]|" + DOWN + "¦below|" + DOWN + "¦under[neath]|" + DOWN + "¦beneath" +
						") [%-direction%]",
				"[%-number% [(block|met(er|re))[s]]] in [the] (0¦direction|1¦horizontal direction|2¦facing|3¦horizontal facing) of %entity/block% (of|from|)",
				"[%-number% [(block|met(er|re))[s]]] in %entity/block%'[s] (0¦direction|1¦horizontal direction|2¦facing|3¦horizontal facing) (of|from|)",
				"[%-number% [(block|met(er|re))[s]]] (0¦in[ ]front [of]|0¦forward[s]|2¦behind|2¦backwards|[to the] (1¦right|-1¦left) [of])",
				"[%-number% [(block|met(er|re))[s]]] horizontal[ly] (0¦in[ ]front [of]|0¦forward[s]|2¦behind|2¦backwards|to the (1¦right|-1¦left) [of])");
	}
	
	@Nullable
	Expression<Number> amount;
	
	@Nullable
	private Vector direction;
	@Nullable
	private ExprDirection next;
	
	@Nullable
	private Expression<?> relativeTo;
	boolean horizontal;
	boolean facing;
	
	private double yaw;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		amount = (Expression<Number>) exprs[0];
		switch (matchedPattern) {
			case 0:
				direction = new Vector(byMark[parseResult.mark].getModX(), byMark[parseResult.mark].getModY(), byMark[parseResult.mark].getModZ());
				if (exprs[1] != null) {
					if (!(exprs[1] instanceof ExprDirection) || ((ExprDirection) exprs[1]).direction == null)
						return false;
					next = (ExprDirection) exprs[1];
				}
				break;
			case 1:
			case 2:
				relativeTo = exprs[1];
				horizontal = parseResult.mark % 2 != 0;
				facing = parseResult.mark >= 2;
				break;
			case 3:
			case 4:
				yaw = Math.PI / 2 * parseResult.mark;
				horizontal = matchedPattern == 4;
		}
		return true;
	}
	
	@Override
	@Nullable
	protected Direction[] get(final Event e) {
		final Number n = amount != null ? amount.getSingle(e) : 1;
		if (n == null)
			return new Direction[0];
		final double ln = n.doubleValue();
		if (direction != null) {
			final Vector v = direction.clone().multiply(ln);
			ExprDirection d = next;
			while (d != null) {
				final Number n2 = d.amount != null ? d.amount.getSingle(e) : 1;
				if (n2 == null)
					return new Direction[0];
				assert d.direction != null; // checked in init()
				v.add(d.direction.clone().multiply(n2.doubleValue()));
				d = d.next;
			}
			assert v != null;
			return new Direction[] {new Direction(v)};
		} else if (relativeTo != null) {
			final Object o = relativeTo.getSingle(e);
			if (o == null)
				return new Direction[0];
			if (o instanceof Block) {
				final BlockFace f = Direction.getFacing((Block) o);
				if (f == BlockFace.SELF || horizontal && (f == BlockFace.UP || f == BlockFace.DOWN))
					return new Direction[] {Direction.ZERO};
				return new Direction[] {new Direction(f, ln)};
			} else {
				final Location l = ((Entity) o).getLocation();
				if (!horizontal) {
					if (!facing) {
						final Vector v = l.getDirection().normalize().multiply(ln);
						assert v != null;
						return new Direction[] {new Direction(v)};
					}
					final double pitch = Direction.pitchToRadians(l.getPitch());
					assert pitch >= -Math.PI / 2 && pitch <= Math.PI / 2;
					if (pitch > Math.PI / 4)
						return new Direction[] {new Direction(new double[] {0, ln, 0})};
					if (pitch < -Math.PI / 4)
						return new Direction[] {new Direction(new double[] {0, -ln, 0})};
				}
				double yaw = Direction.yawToRadians(l.getYaw());
				if (horizontal && !facing) {
					return new Direction[] {new Direction(new double[] {Math.cos(yaw) * ln, 0, Math.sin(yaw) * ln})};
				}
				yaw = Math2.mod(yaw, 2 * Math.PI);
				if (yaw >= Math.PI / 4 && yaw < 3 * Math.PI / 4)
					return new Direction[] {new Direction(new double[] {0, 0, ln})};
				if (yaw >= 3 * Math.PI / 4 && yaw < 5 * Math.PI / 4)
					return new Direction[] {new Direction(new double[] {-ln, 0, 0})};
				if (yaw >= 5 * Math.PI / 4 && yaw < 7 * Math.PI / 4)
					return new Direction[] {new Direction(new double[] {0, 0, -ln})};
				assert yaw >= 0 && yaw < Math.PI / 4 || yaw >= 7 * Math.PI / 4 && yaw < 2 * Math.PI;
				return new Direction[] {new Direction(new double[] {ln, 0, 0})};
			}
		} else {
			return new Direction[] {new Direction(horizontal ? Direction.IGNORE_PITCH : 0, yaw, ln)};
		}
	}
	
	@Override
	public boolean isSingle() {
		return true;
	}
	
	@Override
	public Class<? extends Direction> getReturnType() {
		return Direction.class;
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		final Expression<?> relativeTo = this.relativeTo;
		return (amount != null ? amount.toString(e, debug) + " meter(s) " : "") + (direction != null ? Direction.toString(direction) :
				relativeTo != null ? " in " + (horizontal ? "horizontal " : "") + (facing ? "facing" : "direction") + " of " + relativeTo.toString(e, debug) :
						(horizontal ? "horizontally " : "") + Direction.toString(0, yaw, 1));
	}
	
}
