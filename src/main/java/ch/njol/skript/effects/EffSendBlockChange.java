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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

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
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

@Name("Send Block Change")
@Description({
	"Makes a player see a block as something it really isn't.",
	"The chunk where the fake block change occur must be loaded to the player in order to take effect.",
	"The 'without light updates' option is available for use when you are changing multiple blocks at once."
})
@Examples({
	"make player see block at player as dirt",
	"make player see target block as campfire[facing=south]"
})
@Since("2.2-dev37c, 2.5.1 (block data support)")
public class EffSendBlockChange extends Effect {

	private static final boolean SUPPORT_MULTI_BLOCKS = Skript.methodExists(Player.class, "sendBlockChanges", CollectionUtils.array(Collection.class, boolean.class));

	static {
		Skript.registerEffect(EffSendBlockChange.class,
			SUPPORT_MULTI_BLOCKS ? "make %players% see %blocks% as %itemtype/blockdata% [nolightupdate:without light update[s]]" : "make %players% see %blocks% as %itemtype/blockdata%");
	}

	private Expression<Player> players;
	private Expression<Block> blocks;
	private Expression<Object> as;
	private boolean lightUpdates;
	
	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		players = (Expression<Player>) exprs[0];
		blocks = (Expression<Block>) exprs[1];
		as = (Expression<Object>) exprs[2];
		lightUpdates = !parseResult.hasTag("nolightupdate");
		return true;
	}

	@Override
	protected void execute(Event event) {
		Object object = as.getSingle(event);
		Block[] blocks = this.blocks.getArray(event);
		if (object instanceof ItemType) {
			ItemType itemType = (ItemType) object;
			if (SUPPORT_MULTI_BLOCKS && blocks.length > 1) {
				for (Player player : players.getArray(event)) {
					Location[] locations = Arrays.stream(blocks).map(Block::getLocation).toArray(Location[]::new);
					itemType.sendBlockChanges(player, locations, lightUpdates);
				}
			} else {
				for (Player player : players.getArray(event)) {
					for (Block block : blocks) {
						itemType.sendBlockChange(player, block.getLocation());
					}
				}
			}
		} else if (object instanceof BlockData) {
			BlockData blockData = (BlockData) object;
			if (SUPPORT_MULTI_BLOCKS && blocks.length > 1) {
				Collection<BlockState> newBlockStates = new ArrayList<>();
				for (Block block : blocks) {
					BlockState state = block.getState();
					state.setBlockData(blockData);
					newBlockStates.add(state);
				}
				for (Player player : players.getArray(event)) {
					player.sendBlockChanges(newBlockStates, lightUpdates);
				}
			} else {
				for (Player player : players.getArray(event)) {
					for (Block block : blocks) {
						player.sendBlockChange(block.getLocation(), blockData);
					}
				}
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return String.format(
				"make %s see %s as %s",
				players.toString(event, debug),
				blocks.toString(event, debug),
				as.toString(event, debug)
		);
	}

}
