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

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Name("Break Block")
@Description({
	"Breaks the block and spawns items as if a player had mined it",
	"\nYou can add a tool, which will spawn items based on how that tool would break the block ",
	"(ie: When using a hand to break stone, it drops nothing, whereas with a pickaxe it drops cobblestone)",
	"\nWhen 'with effect' is used, the sound and particles while breaking the block is produced.",
	"\nWhen 'and drop experience' is used, XP will drop at it if the block usually does so."
})
@Examples({
	"on right click:",
		"\tbreak clicked block naturally",
	"loop blocks in radius 10 around player:",
		"\tbreak loop-block using player's tool",
	"loop blocks in radius 10 around player:",
		"\tbreak loop-block with effects naturally using diamond pickaxe and drop xps"
})
@Since("2.4, INSERT VERSION (effects, drop xps)")
@RequiredPlugins("Paper 1.15+ (effects), Paper 1.17+ (effects without tool), Paper 1.19+ (drop xps)")
public class EffBreakNaturally extends Effect {

	private static final boolean hasMethod115 = Skript.methodExists(Block.class, "breakNaturally", CollectionUtils.array(ItemStack.class, boolean.class), boolean.class);
	private static final boolean hasMethod117 = Skript.methodExists(Block.class, "breakNaturally", boolean.class, boolean.class);
	private static final boolean hasMethod119 = Skript.methodExists(Block.class, "breakNaturally", CollectionUtils.array(ItemStack.class, boolean.class, boolean.class), boolean.class);
	
	static {
		List<String> patterns = new ArrayList<>();
		if (hasMethod119) {
			patterns.add("break %blocks% [effect:with effect[s]] [naturally] [using %-itemtype%] [xpdrops:and drop (xp|experience)[s]]");
		} else if (hasMethod115) {
			patterns.add("break %blocks% [effect:with effect[s]] [naturally] [using %-itemtype%]");
		} else {
			patterns.add("break %blocks% [naturally] [using %-itemtype%]");
		}
		Skript.registerEffect(EffBreakNaturally.class, patterns.toArray(new String[0]));
	}

	private Expression<Block> blocks;
	@Nullable
	private Expression<ItemType> tool;
	private boolean effect;
	private boolean xpdrops;

	@Override
	@SuppressWarnings({"unchecked"})
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parser) {
		blocks = (Expression<Block>) exprs[0];
		tool = (Expression<ItemType>) exprs[1];
		effect = parser.hasTag("effect");
		xpdrops = parser.hasTag("xpdrops");
		return true;
	}
	
	@Override
	protected void execute(Event event) {
		ItemType tool = this.tool != null ? this.tool.getSingle(event) : null;
		for (Block block : this.blocks.getArray(event)) {
			if (tool != null) {
				ItemStack item = tool.getRandom();
				if (hasMethod119) {
					block.breakNaturally(item, effect, xpdrops);
				} else if (hasMethod115) {
					block.breakNaturally(item, effect);
				} else {
					block.breakNaturally(item);
				}
			} else if (hasMethod119) {
				block.breakNaturally(effect, xpdrops);
			} else if (hasMethod117) {
				block.breakNaturally(effect);
			} else {
				block.breakNaturally();
			}
		}
	}
	
	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "break " + blocks.toString(event, debug) + (effect ? " with effects" : "") + " naturally" + (tool != null ? " using " + tool.toString(event, debug) : "") + (xpdrops ? " and drop experience" : "");
	}
}
