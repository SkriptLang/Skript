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
package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.Nullable;

@Name("Is Preferred Tool")
@Description(
		"Checks whether an item is the preferred tool for a block. A preferred tool is one that will drop the block's item " +
		"when used. For example, a wooden pickaxe is a preferred tool for grass and stone blocks, but not for iron ore."
)
@Examples({
	"on left click:",
		"\tevent-block is set",
		"\tif player's tool is the preferred tool for event-block:",
			"\t\tbreak event-block naturally using player's tool",
		"\telse:",
			"\t\tcancel event"
})
@Since("INSERT VERSION")
@RequiredPlugins("1.16.5+, Paper 1.19.2+ (blockdata)")
public class CondIsPreferredTool extends Condition {

	static {
		String types = "blocks";
		if (Skript.methodExists(BlockData.class, "isPreferredTool", ItemStack.class))
			types += "/blockdatas";

		Skript.registerCondition(CondIsPreferredTool.class,
				"%itemtypes% (is|are) %" + types + "%'s preferred tool[s]",
				"%itemtypes% (is|are) [the|a] preferred tool[s] (for|of) %" + types + "%",
				"%itemtypes% (is|are)(n't| not) %" + types + "%'s preferred tool[s]",
				"%itemtypes% (is|are)(n't| not) [the|a] preferred tool[s] (for|of) %" + types + "%"
		);
	}

	private Expression<ItemType> items;
	private Expression<?> blocks;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setNegated(matchedPattern >= 2);
		items = (Expression<ItemType>) exprs[0];
		blocks = exprs[1];
		return true;
	}

	@Override
	public boolean check(Event event) {
		for (Object block : blocks.getArray(event)){
			if (block instanceof Block) {
				if (!items.check(event, (item) -> ((Block) block).isPreferredTool(item.getRandom()), isNegated()))
					return false;
			} else if (block instanceof BlockData) {
				if (!items.check(event, (item) -> ((BlockData) block).isPreferredTool(item.getRandom()), isNegated()))
					return false;
			} else {
				// invalid type
				return false;
			}
		}
		// all checks passed
		return true;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return items.toString(event, debug) + " is the preferred tool for " + blocks.toString(event, debug);
	}
}
