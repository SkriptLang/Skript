package org.skriptlang.skript.bukkit.misc.elements.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Direction;
import ch.njol.util.Kleenean;
import ch.njol.util.Math2;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

/**
 * @author Peter Güttinger
 */
@Name("Direction")
@Description("A helper expression for the <a href='#direction'>direction type</a>.")
@Example("thrust the player upwards")
@Example("set the block behind the player to water")
@Example("""
	loop blocks above the player:
		set {_rand} to a random integer between 1 and 10
		set the block {_rand} meters south east of the loop-block to stone
	""")
@Example("block in horizontal facing of the clicked entity from the player is air")
@Example("spawn a creeper 1.5 meters horizontally behind the player")
@Example("spawn a TNT 5 meters above and 2 meters horizontally behind the player")
@Example("thrust the last spawned TNT in the horizontal direction of the player with speed 0.2")
@Example("push the player upwards and horizontally forward at speed 0.5")
@Example("push the clicked entity in in the direction of the player at speed -0.5")
@Example("open the inventory of the block 2 blocks below the player to the player")
@Example("teleport the clicked entity behind the player")
@Example("grow a regular tree 2 meters horizontally behind the player")
@Since("1.0 (basic), 2.0 (extended)")
public class ExprDirection extends SimpleExpression<Direction> {

	public static DefaultSyntaxInfos.Expression<ExprDirection, Direction> syntaxInfo;

	private final static BlockFace[] byMark = new BlockFace[] {
			BlockFace.UP, BlockFace.DOWN,
			BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST,
			BlockFace.NORTH_EAST, BlockFace.NORTH_WEST, BlockFace.SOUTH_EAST, BlockFace.SOUTH_WEST};

	private final static int UP = 0, DOWN = 1,
			NORTH = 2, SOUTH = 3, EAST = 4, WEST = 5,
			NORTH_EAST = 6, NORTH_WEST = 7, SOUTH_EAST = 8, SOUTH_WEST = 9;

	public static void register(SyntaxRegistry registry) {
		// TODO think about parsing statically & dynamically (also in general)
		// "at": see LitAt
		// TODO direction of %location% (from|relative to) %location%

		syntaxInfo = SyntaxInfo.Expression.simple(
			ExprDirection.class,
			ExprDirection::new,
			Direction.class,
			"[%-number% [(block|met(er|re))[s]] [to the]] (" +
				NORTH + "¦north[(-| |)(" + (NORTH_EAST ^ NORTH) + "¦east|" + (NORTH_WEST ^ NORTH) + "¦west)][(ward(s|ly|)|er(n|ly|))] [of]" +
				"|" + SOUTH + "¦south[(-| |)(" + (SOUTH_EAST ^ SOUTH) + "¦east|" + (SOUTH_WEST ^ SOUTH) + "¦west)][(ward(s|ly|)|er(n|ly|))] [of]" +
				"|(" + EAST + "¦east|" + WEST + "¦west)[(ward(s|ly|)|er(n|ly|))] [of]" +
				"|" + UP + "¦above|" + UP + "¦over|(" + UP + "¦up|" + DOWN + "¦down)[ward(s|ly|)]|" + DOWN + "¦below|" + DOWN + "¦under[neath]|" + DOWN + "¦beneath" +
				") [%-direction%]",
			"[%-number% [(block|met(er|re))[s]]] in [the] (0¦direction|1¦horizontal direction|2¦facing|3¦horizontal facing) of %entity/block% (of|from|)",
			"[%-number% [(block|met(er|re))[s]]] in %entity/block%'[s] (0¦direction|1¦horizontal direction|2¦facing|3¦horizontal facing) (of|from|)",
			"[%-number% [(block|met(er|re))[s]]] (0¦in[ ]front [of]|0¦forward[s]|2¦behind|2¦backwards|[to the] (1¦right|-1¦left) [of])",
			"[%-number% [(block|met(er|re))[s]]] horizontal[ly] (0¦in[ ]front [of]|0¦forward[s]|2¦behind|2¦backwards|to the (1¦right|-1¦left) [of])"
		);

		registry.register(SyntaxRegistry.EXPRESSION, syntaxInfo);
	}

	@Nullable Expression<Number> amount;

	private @Nullable Vector direction;
	private @Nullable ExprDirection next;

	private @Nullable Expression<?> relativeTo;
	boolean horizontal;
	boolean facing;
	
	private double yaw;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		amount = (Expression<Number>) exprs[0];
		switch (matchedPattern) {
			case 0 -> {
				direction = new Vector(byMark[parseResult.mark].getModX(), byMark[parseResult.mark].getModY(), byMark[parseResult.mark].getModZ());
				if (exprs[1] != null) {
					if (!(exprs[1] instanceof ExprDirection exprDirection) || (exprDirection.direction == null))
						return false;
					next = (ExprDirection) exprs[1];
				}
			}
			case 1, 2 -> {
				relativeTo = exprs[1];
				horizontal = parseResult.mark % 2 != 0;
				facing = parseResult.mark >= 2;
			}
			case 3, 4 -> {
				yaw = Math.PI / 2 * parseResult.mark;
				horizontal = matchedPattern == 4;
			}
		}
		return true;
	}

	public @Nullable Expression<Number> getAmount() {
		return amount;
	}

	@Override
	protected Direction @Nullable [] get(Event event) {
		Number number = amount != null ? amount.getSingle(event) : 1;
		if (number == null)
			return new Direction[0];
		double doubleValue = number.doubleValue();
		if (direction != null) {
			Vector vector = direction.clone().multiply(doubleValue);
			ExprDirection exprDirection = next;
			while (exprDirection != null) {
				Number number1 = exprDirection.amount != null ? exprDirection.amount.getSingle(event) : 1;
				if (number1 == null)
					return new Direction[0];
				assert exprDirection.direction != null; // checked in init()
				vector.add(exprDirection.direction.clone().multiply(number1.doubleValue()));
				exprDirection = exprDirection.next;
			}
			return new Direction[] {new Direction(vector)};
		} else if (relativeTo != null) {
			Object object = relativeTo.getSingle(event);
			if (object == null)
				return new Direction[0];
			if (object instanceof Block block) {
				BlockFace blockFace = Direction.getFacing(block);
				if (blockFace == BlockFace.SELF || horizontal && (blockFace == BlockFace.UP || blockFace == BlockFace.DOWN))
					return new Direction[] {Direction.ZERO};
				return new Direction[] {new Direction(blockFace, doubleValue)};
			} else {
				Location location = ((Entity) object).getLocation();
				if (!horizontal) {
					if (!facing) {
						Vector vector = location.getDirection().normalize().multiply(doubleValue);
						assert vector != null;
						return new Direction[] {new Direction(vector)};
					}
					double pitch = Direction.pitchToRadians(location.getPitch());
					assert pitch >= -Math.PI / 2 && pitch <= Math.PI / 2;
					if (pitch > Math.PI / 4)
						return new Direction[] {new Direction(new double[] {0, doubleValue, 0})};
					if (pitch < -Math.PI / 4)
						return new Direction[] {new Direction(new double[] {0, -doubleValue, 0})};
				}
				double yaw = Direction.yawToRadians(location.getYaw());
				if (horizontal && !facing) {
					return new Direction[] {new Direction(new double[] {Math.cos(yaw) * doubleValue, 0, Math.sin(yaw) * doubleValue})};
				}
				yaw = Math2.mod(yaw, 2 * Math.PI);
				if (yaw >= Math.PI / 4 && yaw < 3 * Math.PI / 4)
					return new Direction[] {new Direction(new double[] {0, 0, doubleValue})};
				if (yaw >= 3 * Math.PI / 4 && yaw < 5 * Math.PI / 4)
					return new Direction[] {new Direction(new double[] {-doubleValue, 0, 0})};
				if (yaw >= 5 * Math.PI / 4 && yaw < 7 * Math.PI / 4)
					return new Direction[] {new Direction(new double[] {0, 0, -doubleValue})};
				assert yaw >= 0 && yaw < Math.PI / 4 || yaw >= 7 * Math.PI / 4 && yaw < 2 * Math.PI;
				return new Direction[] {new Direction(new double[] {doubleValue, 0, 0})};
			}
		} else {
			return new Direction[] {new Direction(horizontal ? Direction.IGNORE_PITCH : 0, yaw, doubleValue)};
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
	public String toString(@Nullable Event event, boolean debug) {
		Expression<?> relativeTo = this.relativeTo;
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug)
			.appendIf(amount != null, amount, "meter(s)");
		if (direction != null) {
			builder.append(Direction.toString(direction));
		} else {
			if (relativeTo != null) {
				builder.append("in")
					.appendIf(horizontal, "horizontal")
					.append(facing ? "facing" : "direction", "of", relativeTo);
			} else {
				builder.appendIf(horizontal, "horizontally")
					.append(Direction.toString(0, yaw, 1));
			}
		}
		return builder.toString();
	}
	
}
