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

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.SeaPickle;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

@Name("Sea Pickles")
@Description("An expression to obtain or modify data relating to the pickles of a sea pickle block.")
@Examples({
	"on block break:",
	"\ttype of block is sea pickle",
	"\tsend \"Wow! This stack of sea pickles contained %event-block's sea pickle count% pickles!\"",
	"\tsend \"It could've contained a maximum of %event-block's maximum sea pickle count% pickles!\"",
	"\tsend \"It had to have contained at least %event-block's minimum sea pickle count% pickles!\"",
	"\tcancel event",
	"\tset event-block's sea pickle count to event-block's maximum sea pickle count",
	"\tsend \"This bad boy is going to hold so many pickles now!!\""
})
@Since("INSERT VERSION")
public class ExprSeaPickles extends SimplePropertyExpression<Block, Integer> {

	static {
		register(ExprSeaPickles.class, Integer.class, "[:(min|max)[imum]] [sea] pickle(s| (count|amount))", "blocks");
	}

	private boolean minimum, maximum;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		minimum = parseResult.hasTag("min");
		maximum = parseResult.hasTag("max");
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}

	@Override
	@Nullable
	public Integer convert(Block block) {
		BlockData blockData = block.getBlockData();
		if (!(blockData instanceof SeaPickle))
			return null;

		SeaPickle pickleData = (SeaPickle) blockData;

		if (maximum)
			return pickleData.getMaximumPickles();
		if (minimum)
			return pickleData.getMinimumPickles();
		return pickleData.getPickles();
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		if (minimum || maximum)
			return null;
		switch (mode) {
			case SET:
			case ADD:
			case REMOVE:
			case RESET:
			case DELETE:
				return CollectionUtils.array(Number.class);
			default:
				return null;
		}
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		if (delta == null && mode != ChangeMode.RESET && mode != ChangeMode.DELETE)
			return;

		int change = delta != null ? ((Number) delta[0]).intValue() : 0;
		if (mode == ChangeMode.REMOVE)
			change *= -1;

		for (Block block : getExpr().getArray(event)) {
			// Obtain pickle data
			BlockData blockData = block.getBlockData();
			if (!(blockData instanceof SeaPickle))
				return;
			SeaPickle pickleData = (SeaPickle) blockData;

			int newPickles = change;

			// Calculate new pickles value
			switch (mode) {
				case ADD:
				case REMOVE:
					newPickles += pickleData.getPickles();
				case SET:
					if (newPickles != 0) { // 0 = delete pickles
						newPickles = Math.max(pickleData.getMinimumPickles(), newPickles); // Ensure value isn't too low
						newPickles = Math.min(pickleData.getMaximumPickles(), newPickles); // Ensure value isn't too high
					}
					break;
				case RESET:
					newPickles = pickleData.getMinimumPickles();
			}

			// Update the block data
			if (newPickles != 0) {
				pickleData.setPickles(newPickles);
				block.setBlockData(pickleData);
			} else { // We are removing the pickles :(
				block.setType(pickleData.isWaterlogged() ? Material.WATER : Material.AIR);
			}
		}
	}

	@Override
	public Class<? extends Integer> getReturnType() {
		return Integer.class;
	}

	@Override
	protected String getPropertyName() {
		return (maximum ? "maximum " : minimum ? "minimum " : "") + "sea pickle count";
	}

}
