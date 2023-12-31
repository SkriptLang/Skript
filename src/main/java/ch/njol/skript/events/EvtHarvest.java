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

import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerHarvestBlockEvent;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;

public class EvtHarvest extends SkriptEvent {

	static {
		if (Skript.classExists("org.bukkit.event.player.PlayerHarvestBlockEvent"))
			Skript.registerEvent("Harvest", EvtHarvest.class, PlayerHarvestBlockEvent.class, "[player] [block|crop] harvest[ing] [[of] %-itemtypes%]")
					.description("This event is called whenever a player harvests a block.",
							"A 'harvest' is when a block drops an item (usually some sort of crop) and changes state, but is not broken in order to drop the item. You cannot modify the event-block unless cancelled, as it's not going to be present as it's being harvested.")
					.examples("on player harvesting:",
							"\tmessage \"You harvested %block% which drops %event-items%, using your %event-slot%!\"")
					.requiredPlugins("Spigot 1.17+")
					.since("INSERT VERSION");
	}

	@Nullable
	private Literal<ItemType> itemtypes;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		itemtypes = (Literal<ItemType>) args[0];
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (itemtypes == null)
			return true;
		Block block = ((PlayerHarvestBlockEvent) event).getHarvestedBlock();
		return itemtypes.check(event, itemtype -> itemtype.isOfType(block));
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "harvest of " + itemtypes.toString(event, debug);
	}

}
