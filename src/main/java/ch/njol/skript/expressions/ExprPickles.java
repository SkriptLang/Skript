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
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.SeaPickle;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

@Name("Pickles")
@Description("An expression to obtain or modify data relating to the pickles of a sea pickle block.")
@Examples({
	"on block break:",
	"\ttype of block is sea pickle",
	"\tsend \"Wow! This stack of sea pickles contained %event-block's pickles% pickles!\"",
	"\tsend \"It could've held a maximum of %event-block's maximum pickles% pickles!\"",
	"\tsend \"It had to have held at least %event-block's minimum pickles% pickles!\"",
	"\tcancel event",
	"\tset event-block's pickles to event-block's maximum pickles",
	"\tsend \"This bad boy is going to hold so many pickles now!!\""
})
@Since("INSERT VERSION")
public class ExprPickles extends SimplePropertyExpression<Block, Long> {

	static {
		register(ExprPickles.class, Long.class, "[:minimum|:maximum] pickles", "block");
	}

	private boolean minimum, maximum;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		minimum = parseResult.hasTag("minimum");
		maximum = parseResult.hasTag("maximum");
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}

	@Override
	@Nullable
	public Long convert(Block block) {
		BlockData blockData = block.getBlockData();
		if (!(blockData instanceof SeaPickle))
			return null;

		SeaPickle pickleData = (SeaPickle) blockData;

		if (maximum)
			return (long) pickleData.getMaximumPickles();
		if (minimum)
			return (long) pickleData.getMinimumPickles();
		return (long) pickleData.getPickles();
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
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
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
					newPickles = Math.max(pickleData.getMinimumPickles(), newPickles); // Ensure value isn't too low
					newPickles = Math.min(pickleData.getMaximumPickles(), newPickles); // Ensure value isn't too high
					break;
				case RESET:
				case DELETE:
					newPickles = pickleData.getMinimumPickles();
					break;
				default:
					assert false;
			}

			// Update the block data
			pickleData.setPickles(newPickles);
			block.setBlockData(pickleData);

		}
	}

	@Override
	public Class<? extends Long> getReturnType() {
		return Long.class;
	}

	@Override
	protected String getPropertyName() {
		return (maximum ? "maximum " : minimum ? "minimum " : "") + "pickles";
	}

}
