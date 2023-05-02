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
package ch.njol.skript.events;

import org.bukkit.TreeType;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Checker;
import ch.njol.util.coll.CollectionUtils;

public class EvtGrow extends SkriptEvent {

	static {
		Class<? extends Event>[] events = CollectionUtils.array(StructureGrowEvent.class, BlockGrowEvent.class);
		Skript.registerEvent("Grow", EvtGrow.class, events, "grow [of (tree:%-treetypes%|%-itemtype%)]")
				.description("Called when a tree, giant mushroom or plant grows to next stage.")
				.examples("on grow:", "on grow of every tree:", "on grow of a huge jungle tree:")
				.since("1.0 (2.2-dev20 for plants)");
	}

	@Nullable
	private Literal<TreeType> types;

	@Nullable
	private Literal<ItemType> blocks;
	private boolean isTree;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		isTree = parseResult.hasTag("tree");
		if (isTree) {
			types = (Literal<TreeType>) args[0];
		} else {
			blocks = (Literal<ItemType>) args[1];
		}
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (isTree && types != null && event instanceof StructureGrowEvent) {
			return types.check(event, new Checker<TreeType>() {
				@Override
				public boolean check(TreeType type) {
					return type.equals(((StructureGrowEvent) event).getSpecies());
				}
			});
		} else if (blocks != null && event instanceof BlockGrowEvent) {
			return blocks.check(event, new Checker<ItemType>() {
				@Override
				public boolean check(ItemType type) {
					return type.isOfType(((BlockGrowEvent) event).getBlock());
				}
			});
		}
		return true;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (isTree)
			return "grow" + (types != null ? " of " + types.toString(event, debug) : "");
		return "grow" + (blocks != null ? " of " + blocks.toString(event, debug) : "");
	}

}
