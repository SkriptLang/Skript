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

import org.bukkit.event.Event;
import org.bukkit.event.block.BlockGrowEvent;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;


public class EvtPlantGrowth extends SkriptEvent {
	static {
		Skript.registerEvent("Block Growth", EvtPlantGrowth.class, BlockGrowEvent.class, "(plant|crop|block) grow[(th|ing)] [[of] %-itemtypes%]")
				.description("Called when a crop grows. Alternative to new form of generic grow event.")
				.examples("on crop growth:")
				.since("2.2-Fixes-V10");
	}
	
	@Nullable
	private Literal<ItemType> types;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		types = (Literal<ItemType>) args[0];
		
		return true;
	}

	@Override
	public boolean check(Event e) {
		if (types != null) {
			for (ItemType type : types.getAll()) {
				if (new ItemType(((BlockGrowEvent) e).getBlock()).equals(type))
					return true;
			}
			return false; // Not one of given types
		}
		
		return true;
	}
	
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "plant growth";
	}
}
