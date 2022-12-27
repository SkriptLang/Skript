/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.bukkitutil.ItemUtils;
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
import org.eclipse.jdt.annotation.Nullable;

@Name("Lowest/Highest Solid Block")
@Description("An expression to obtain the lowest or highest solid block at a location.")
@Examples({
	"teleport the player to the block above the highest block at the player",
	"set the highest solid block at the player's location to the lowest non-air block at the player's location"
})
@Since("2.2-dev34, INSERT VERSION (lowest solid block)")
public class ExprLowestHighestSolidBlock extends SimplePropertyExpression<Location, Block> {

	static {
		Skript.registerExpression(ExprLowestHighestSolidBlock.class, Block.class, ExpressionType.COMBINED,
			"[the] (highest|:lowest) [solid|non-air] block (at|of) %locations%"
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
		if (world == null)
			return null;

		if (!lowest)
			return world.getHighestBlockAt(location);

		// sigh...
		location = location.clone();
		location.setY(world.getMinHeight());
		Block block = location.getBlock();
		int maxHeight = world.getMaxHeight();
		while (block.getY() < maxHeight && !block.isSolid())
			block = block.getRelative(BlockFace.UP);
		return block.isSolid() ? block : world.getHighestBlockAt(block.getLocation());
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
