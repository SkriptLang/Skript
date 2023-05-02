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
package ch.njol.skript.effects;

import org.bukkit.Location;
import org.bukkit.TreeType;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Direction;
import ch.njol.util.Kleenean;

@Name("Tree")
@Description({
	"Creates a tree.",
	"This may require that there is enough space above the given location and that the block below is dirt/grass,",
	"but it is possible that the tree will just grow anyways, possibly replacing every block in its path."
})
@Examples({
	"grow a tall redwood tree above the clicked block"
})
@Since("1.0")
public class EffTree extends Effect {

	static {
		Skript.registerEffect(EffTree.class,
				"(grow|create|generate) tree [of type %treetype%] %directions% %locations%",
				"(grow|create|generate) %treetype% %directions% %locations%"
		);
	}

	private Expression<Location> blocks;
	private Expression<TreeType> type;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parser) {
		type = (Expression<TreeType>) exprs[0];
		blocks = Direction.combine((Expression<? extends Direction>) exprs[1], (Expression<? extends Location>) exprs[2]);
		return true;
	}

	@Override
	public void execute(Event event) {
		TreeType type = this.type.getSingle(event);
		if (type == null)
			return;
		for (Location location : blocks.getArray(event)) {
			Block block = location.getBlock();
			block.getWorld().generateTree(block.getLocation(), type);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "grow tree of type " + type.toString(event, debug) + " " + blocks.toString(event, debug);
	}

}
