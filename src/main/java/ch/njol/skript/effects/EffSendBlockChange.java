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

import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

@Name("Send Block Change")
@Description("Makes players see blocks as something else.")
@Examples({
	"make player see block at player as dirt",
	"make player see target block as campfire[facing=south]"
})
@Since("2.2-dev37c, 2.5.1 (block data support), INSERT VERSION (reset)")
public class EffSendBlockChange extends Effect {

	static {
		Skript.registerEffect(EffSendBlockChange.class,
				"make %players% see %blocks% as %itemtype/blockdata%",
				"make %players% see %blocks% (as normal|the same as the server)");
	}

	@Nullable
	private Expression<Object> as;
	private Expression<Block> blocks;
	private Expression<Player> players;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		players = (Expression<Player>) exprs[0];
		blocks = (Expression<Block>) exprs[1];
		if (matchedPattern == 1)
			as = (Expression<Object>) exprs[2];
		return true;
	}

	@Override
	protected void execute(Event event) {
		Object object = null;
		if (this.as != null) {
			object = this.as.getSingle(event);
			if (object == null)
				return;
		}
		// Reset the block the players are seeing to be synced with server.
		if (object == null) {
			for (Player player : players.getArray(event)) {
				for (Block block : blocks.getArray(event)) {
					player.sendBlockChange(block.getLocation(), block.getBlockData());
				}
			}
		} else if (object instanceof ItemType) {
			ItemType itemType = (ItemType) object;
			for (Player player : players.getArray(event)) {
				for (Block block : blocks.getArray(event)) {
					itemType.sendBlockChange(player, block.getLocation());
				}
			}
		} else if (object instanceof BlockData) {
			BlockData blockData = (BlockData) object;
			for (Player player : players.getArray(event)) {
				for (Block block : blocks.getArray(event)) {
					player.sendBlockChange(block.getLocation(), blockData);
				}
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (as == null)
			return String.format("make %s see %s as normal",
					players.toString(event, debug),
					blocks.toString(event, debug)
			);
		return String.format("make %s see %s as %s",
				players.toString(event, debug),
				blocks.toString(event, debug),
				as.toString(event, debug)
		);
	}

}
