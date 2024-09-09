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

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.bukkitutil.ItemUtils;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.LiteralList;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.StructureType;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.jetbrains.annotations.Nullable;

public class EvtGrow extends SkriptEvent {

	/**
	 * Growth event restriction.
	 * ANY means any grow event goes.
	 * Structure/block restrict for structure/block grow events only.
	 */
	private static final int ANY = 0, STRUCTURE = 1, BLOCK = 2;

	// Of (involves x in any way), From (x -> something), Into (something -> x), From_Into (x -> y)
	private static final int OF = 0, FROM = 1, INTO = 2, FROM_INTO = 3;
	
	static {
		Skript.registerEvent("Grow", EvtGrow.class, CollectionUtils.array(StructureGrowEvent.class, BlockGrowEvent.class),
				"grow[th] [of (1:%-structuretypes%|2:%-itemtypes/blockdatas%)]",
				"grow[th] from %itemtypes/blockdatas%",
				"grow[th] [in]to (1:%structuretypes%|2:%itemtypes/blockdatas%)",
				"grow[th] from %itemtypes/blockdatas% [in]to (1:%structuretypes%|2:%itemtypes/blockdatas%)"
				)
				.description(
					"Called when a tree, giant mushroom or plant grows to next stage.",
					"\"of\" matches any grow event, \"from\" matches only the old state, \"into\" matches only the new state," +
					"and \"from into\" requires matching both the old and new states.",
					"Using \"and\" lists in this event is equivalent to using \"or\" lists. The event will trigger if any one of the elements is what grew.")
				.examples(
					"on grow:",
					"on grow of tree:",
					"on grow of wheat[age=7]:",
					"on grow from a sapling:",
					"on grow into tree:",
					"on grow from a sapling into tree:",
					"on grow of wheat, carrots, or potatoes:",
					"on grow into tree, giant mushroom, cactus:",
					"on grow from wheat[age=0] to wheat[age=1] or wheat[age=2]:")
				.since("1.0, 2.2-dev20 (plants), 2.8.0 (from, into, blockdata)");
	}
	
	@Nullable
	private Literal<Object> toTypes;
	@Nullable
	private Literal<Object> fromTypes;

	// Restriction on the type of grow event, ANY, STRUCTURE or BLOCK
	private int eventRestriction;

	// Restriction on the type of action, OF, FROM, INTO, or FROM_INTO
	private int actionRestriction;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		eventRestriction = parseResult.mark; // ANY, STRUCTURE or BLOCK
		actionRestriction = matchedPattern; // OF, FROM, INTO, or FROM_INTO

		switch (actionRestriction) {
			case OF:
				if (eventRestriction == STRUCTURE) {
					fromTypes = (Literal<Object>) args[0];
				} else if (eventRestriction == BLOCK) {
					fromTypes = (Literal<Object>) args[1];
				}
				break;
			case FROM:
				fromTypes = (Literal<Object>) args[0];
				break;
			case INTO:
				if (eventRestriction == STRUCTURE) {
					toTypes = (Literal<Object>) args[0];
				} else if (eventRestriction == BLOCK) {
					toTypes = (Literal<Object>) args[1];
				}
				break;
			case FROM_INTO:
				fromTypes = (Literal<Object>) args[0];
				if (eventRestriction == STRUCTURE) {
					toTypes = (Literal<Object>) args[1];
				} else if (eventRestriction == BLOCK) {
					toTypes = (Literal<Object>) args[2];
				}
				break;
			default:
				assert false;
				return false;
		}
		return true;
	}
	
	@Override
	public boolean check(Event event) {
		// Exit early if we need fromTypes, but don't have it
		if (fromTypes == null && actionRestriction != INTO)
			// We want true for "on grow:", false for anything else
			// So check against "OF", which is the first pattern; the one that allows "on grow:"
			return actionRestriction == OF;

		// Can exit early if we're checking against a structure, but the event isn't a structure grow event
		// Can also exit early if we're checking against a block, but the event isn't a block grow event AND we're not checking for OF
		// With OF, we can have `on grow of sapling` or `big mushroom` be a StructureGrowEvent
		if (eventRestriction == STRUCTURE && !(event instanceof StructureGrowEvent)) {
			return false;
		} else if (eventRestriction == BLOCK && !(event instanceof BlockGrowEvent) && actionRestriction != OF) {
			return false;
		}

		switch (actionRestriction) {
			case OF:
				return checkFrom(event, fromTypes) || checkTo(event, fromTypes);
			case FROM:
				return checkFrom(event, fromTypes);
			case INTO:
				return checkTo(event, toTypes);
			case FROM_INTO:
				return checkFrom(event, fromTypes) && checkTo(event, toTypes);
			default:
				assert false;
				return false;
		}
	}

	private static boolean checkFrom(Event event, Literal<Object> types) {
		// treat and lists as or lists
		if (types.getAnd() && types instanceof LiteralList)
			((LiteralList<Object>) types).invertAnd();

		if (event instanceof StructureGrowEvent) {
			Material sapling = ItemUtils.getTreeSapling(((StructureGrowEvent) event).getSpecies());
			return types.check(event, type -> {
				if (type instanceof ItemType) {
					return ((ItemType) type).isOfType(sapling);
				} else if (type instanceof BlockData) {
					return ((BlockData) type).getMaterial() == sapling;
				}
				return false;
			});
		} else if (event instanceof BlockGrowEvent) {
			BlockState oldState = ((BlockGrowEvent) event).getBlock().getState();
			return types.check(event, type -> {
				if (type instanceof ItemType) {
					return ((ItemType) type).isOfType(oldState.getBlockData());
				} else if (type instanceof BlockData) {
					return ((BlockData) type).matches(oldState.getBlockData());
				}
				return false;
			});
		}
		return false;
	}

	private static boolean checkTo(Event event, Literal<Object> types) {
		// treat and lists as or lists
		if (types.getAnd() && types instanceof LiteralList)
			((LiteralList<Object>) types).invertAnd();

		if (event instanceof StructureGrowEvent) {
			TreeType species = ((StructureGrowEvent) event).getSpecies();
			return types.check(event, type -> {
				if (type instanceof StructureType) {
					return ((StructureType) type).is(species);
				}
				return false;
			});
		} else if (event instanceof BlockGrowEvent) {
			BlockState newState = ((BlockGrowEvent) event).getNewState();
			return types.check(event, type -> {
				if (type instanceof ItemType) {
					return ((ItemType) type).isOfType(newState.getBlockData());
				} else if (type instanceof BlockData) {
					return ((BlockData) type).matches(newState.getBlockData());
				}
				return false;
			});
		}
		return false;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (fromTypes == null && toTypes == null)
			return "grow";

		switch (actionRestriction) {
			case OF:
				return "grow of " + fromTypes.toString(event, debug);
			case FROM:
				return "grow from " + fromTypes.toString(event, debug);
			case INTO:
				return "grow into " + toTypes.toString(event, debug);
			case FROM_INTO:
				return "grow from " + fromTypes.toString(event, debug) + " into " + toTypes.toString(event, debug);
		}
		return "grow";
	}
	
}
