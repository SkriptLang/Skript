package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.Nullable;

@Name("Lowest/Highest Solid Block")
@Description({
	"An expression to obtain the lowest or highest solid (impassable) block at a location.",
	"Note that the y-coordinate of the location is not taken into account for this expression."
})
@Examples({
	"teleport the player to the block above the highest block at the player",
	"set the highest solid block at the player's location to the lowest solid block at the player's location"
})
@Since("2.2-dev34, 2.9.0 (lowest solid block, 'non-air' option removed, additional syntax option)")
public class ExprLowestHighestSolidBlock extends SimplePropertyExpression<Location, Block> {

	private static final boolean HAS_MIN_HEIGHT =
			Skript.classExists("org.bukkit.generator.WorldInfo") &&
			Skript.methodExists(WorldInfo.class, "getMinHeight");

	private static final boolean HAS_BLOCK_IS_SOLID = Skript.methodExists(Block.class, "isSolid");

	// Before 1.15, getHighestSolidBlock actually returned the block directly ABOVE the highest solid block
	private static final boolean RETURNS_FIRST_AIR = !Skript.isRunningMinecraft(1, 15);

	static {
		Skript.registerExpression(ExprLowestHighestSolidBlock.class, Block.class, ExpressionType.PROPERTY,
				"[the] (highest|:lowest) [solid] block (at|of) %locations%",
				"%locations%'[s] (highest|:lowest) [solid] block"
		);
	}

	private boolean lowest;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		lowest = parseResult.hasTag("lowest");
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}

	@Override
	@Nullable
	public Block convert(Location location) {
		World world = location.getWorld();
		if (world == null) {
			return null;
		}

		if (!lowest) {
			return getHighestBlockAt(world, location);
		}

		location = location.clone();
		location.setY(HAS_MIN_HEIGHT ? world.getMinHeight() : 0);
		Block block = location.getBlock();
		int maxHeight = world.getMaxHeight();
		while (block.getY() < maxHeight && !isSolid(block)) { // work our way up
			block = block.getRelative(BlockFace.UP);
		}
		// if this block isn't solid, there are no solid blocks at this location
		// getHighestBlockAt is apparently NotNull, so let's just mimic that behavior by returning it
		return isSolid(block) ? block : getHighestBlockAt(world, block.getLocation());
	}

	private static Block getHighestBlockAt(World world, Location location) {
		Block block = world.getHighestBlockAt(location);
		if (RETURNS_FIRST_AIR) {
			block = block.getRelative(BlockFace.DOWN);
			if (!isSolid(block)) { // if the one right below isn't solid let's just preserve the behavior
				block.getRelative(BlockFace.UP);
			}
		}
		return block;
	}

	private static boolean isSolid(Block block) {
		return HAS_BLOCK_IS_SOLID ? block.isSolid() : block.getType().isSolid();
	}

	@Override
	public Class<? extends Block> getReturnType() {
		return Block.class;
	}

	@Override
	protected String getPropertyName() {
		return (lowest ? "lowest" : "highest") + " solid block";
	}

}
