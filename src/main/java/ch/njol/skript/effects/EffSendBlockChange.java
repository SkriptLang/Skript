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
import org.bukkit.Material;
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
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;

@Name("Send Block Change")
@Description("Makes a player see a block as something it really isn't. BlockData support is only for MC 1.13+")
@Examples({"make player see block at player as dirt",
		"make player see target block as campfire[facing=south]"})
@Since("2.2-dev37c, 2.5.1 (block data support)")
public class EffSendBlockChange extends Effect {

	private static final boolean BLOCK_DATA_SUPPORT = Skript.classExists("org.bukkit.block.data.BlockData");
	private static final boolean SUPPORTED =
			Skript.methodExists(
					Player.class,
					"sendBlockChange",
					Location.class,
					Material.class,
					byte.class
			);

	static {
		Skript.registerEffect(EffSendBlockChange.class,
				BLOCK_DATA_SUPPORT ? "make %players% see %blocks% as %itemtype/blockdata%" : "make %players% see %blocks% as %itemtype%"
		);
	}

	@SuppressWarnings("null")
	private Expression<Player> players;

	@SuppressWarnings("null")
	private Expression<Block> blocks;

	@SuppressWarnings("null")
	private Expression<Object> as;
	
	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		if (!SUPPORTED) {
			Skript.error("The send block change effect is not supported on this version. " +
				"If Spigot has added a replacement method without magic values " +
				"please open an issue at https://github.com/SkriptLang/Skript/issues " +
				"and support will be added for it.");
			return false;
		}
		players = (Expression<Player>) exprs[0];
		blocks = (Expression<Block>) exprs[1];
		as = (Expression<Object>) exprs[2];
		return true;
	}

	@Override
	protected void execute(Event e) {
		Object object = this.as.getSingle(e);
		if (object instanceof ItemType) {
			ItemType itemType = (ItemType) object;
			for (Player player : players.getArray(e)) {
				for (Block block : blocks.getArray(e)) {
					itemType.sendBlockChange(player, block.getLocation());
				}
			}
		} else if (BLOCK_DATA_SUPPORT && object instanceof BlockData) {
			BlockData blockData = (BlockData) object;
			for (Player player : players.getArray(e)) {
				for (Block block : blocks.getArray(e)) {
					player.sendBlockChange(block.getLocation(), blockData);
				}
			}
		}
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return String.format(
				"make %s see %s as %s",
				players.toString(e, debug),
				blocks.toString(e, debug),
				as.toString(e, debug)
		);
	}

}
