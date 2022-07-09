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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.lang.util.ConvertedExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.eclipse.jdt.annotation.Nullable;
import ch.njol.skript.classes.Changer.ChangeMode;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;

@Name("Exploded Blocks")
@Description("Get all the blocks that were destroyed in an explode event")
@Examples({
	"on block explode:",
	"\tset {_exploded-blocks::*} to exploded blocks",
	"\t",
	"\tloop exploded blocks:",
	"\t\tif loop-block is sand:",
	"\t\t\tset loop-block to stone",
	"\t",
	"\tset exploded blocks to blocks in radius 3 around event-location  # this will clear exploded blocks and set it to the specified blocks",
	"\tremove all chests from exploded blocks",
	"\tremove 3 stone from exploded blocks # will remove only 3 stone blocks",
	"\tremove stone from exploded blocks # will remove all stones blocks - 'any' and 'all' will do the same",
	"\tadd blocks in radius 3 around event-location to exploded blocks",
	"\tdelete exploded blocks # will clear exploded blocks",
	"\t",
	"on entity explode:",
	"\tremove all stone from exploded blocks",
	"\t",
	"on explode:",
	"\tremove all stone from exploded blocks"})
@Events("explode")
@Since("2.5, INSERT VERSION (changing the list)")
public class ExprExplodedBlocks extends SimpleExpression<Block> {

	static {
		Skript.registerExpression(ExprExplodedBlocks.class, Block.class, ExpressionType.SIMPLE, "[the] exploded blocks");
	}
	
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!getParser().isCurrentEvent(BlockExplodeEvent.class, EntityExplodeEvent.class)) {
			Skript.error("Exploded blocks can only be retrieved from an explode event.");
			return false;
		}
		return true;
	}
	
	@Override
	@Nullable
	protected Block[] get(Event e) {
		return getBlocks(e).toArray(new Block[0]);
	}

	@Override
	@Nullable
	public Iterator<? extends Block> iterator(Event e) {
		List<Block> blocks = getBlocks(e);
		return new ArrayList(blocks).iterator(); // Prevent ConcurrentModificationException
	}

	@Override
	public @Nullable <R> Expression<? extends R> getConvertedExpression(Class<R>... to) {
		return null;
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(final ChangeMode mode) {
		switch (mode) {
			case ADD:
			case SET:
			case DELETE:
				return CollectionUtils.array(Block[].class);
			case REMOVE:
			case REMOVE_ALL:
				return CollectionUtils.array(Block[].class, ItemType[].class);
			case RESET:
			default:
				return null;
		}
	}
	
	@Override
	public void change(Event e, final @Nullable Object[] delta, final ChangeMode mode) {
		List<Block> blocks = getBlocks(e);

		switch (mode) {
			case DELETE:
			case SET:
			case ADD:
				if (mode != ChangeMode.ADD)
					blocks.clear();

				if (mode != ChangeMode.DELETE) {
					assert delta != null;
					for (Object o : delta) {
						if (((Block) o).getType() != Material.AIR) // Performance
							blocks.add((Block) o);
					}
				}
				break;

			case REMOVE:
			case REMOVE_ALL: // ItemType here will allow `remove [all] %itemtypes% from exploded blocks` to remove all/specific amount of specific block type that matches an itemtype, a shortcut for looping
				assert delta != null;
				int amountRemoved = 0; // Remove X alias

				for (Object o : delta) {
					if (o instanceof Block) {
						blocks.remove((Block) o); // no need to check whether they are equals, since blocks are only passed by reference
					} else if (o instanceof ItemType) {
						ItemType item = ((ItemType) o);
						int amount = item.getInternalAmount(); // -1 when using 'all' or 'any' of alias

						if (amount == -1 || mode == ChangeMode.REMOVE_ALL) { // all %itemtype% OR REMOVE_ALL
							blocks.removeIf(item::isOfType);
							break;
						} else if (amount > 0)
							for (Block b : new ArrayList<>(blocks)) {
								if (amountRemoved >= amount)
									break;
								else if (item.isOfType(b)) {
									blocks.remove(b);
									amountRemoved++; // only ++ if a matched item was removed
								}
							}
					}
				}
				break;
		}
	}


	private static List<Block> getBlocks(Event e) {
		return e instanceof EntityExplodeEvent ? ((EntityExplodeEvent) e).blockList() : ((BlockExplodeEvent) e).blockList();
	}
	
	@Override
	public boolean isSingle() {
		return false;
	}
	
	@Override
	public Class<? extends Block> getReturnType() {
		return Block.class;
	}
	
	@Override
	public String toString(@Nullable Event e, boolean d) {
		return "exploded blocks";
	}
	
}
