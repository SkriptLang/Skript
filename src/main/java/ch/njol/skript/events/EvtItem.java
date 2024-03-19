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

import io.papermc.paper.event.player.PlayerStonecutterRecipeSelectEvent;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.sections.EffSecSpawn;
import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleEvent;
import ch.njol.util.coll.CollectionUtils;

@SuppressWarnings("deprecation")
public class EvtItem extends SkriptEvent {
	
	private final static boolean hasConsumeEvent = Skript.classExists("org.bukkit.event.player.PlayerItemConsumeEvent");
	private final static boolean hasPrepareCraftEvent = Skript.classExists("org.bukkit.event.inventory.PrepareItemCraftEvent");
	private final static boolean hasEntityPickupItemEvent = Skript.classExists("org.bukkit.event.entity.EntityPickupItemEvent");
	private final static boolean HAS_PLAYER_STONECUTTER_RECIPE_SELECT_EVENT = Skript.classExists("io.papermc.paper.event.player.PlayerStonecutterRecipeSelectEvent");

	static {
		Skript.registerEvent("Dispense", EvtItem.class, BlockDispenseEvent.class, "dispens(e|ing) [[of] %-itemtypes%]")
				.description("Called when a dispenser dispenses an item.")
				.examples("on dispense of iron block:",
						"\tsend \"that'd be 19.99 please!\"")
				.since("<i>unknown</i> (before 2.1)");
		Skript.registerEvent("Item Spawn", EvtItem.class, ItemSpawnEvent.class, "item spawn[ing] [[of] %-itemtypes%]")
				.description("Called whenever an item stack is spawned in a world, e.g. as drop of a block or mob, a player throwing items out of their inventory, or a dispenser dispensing an item (not shooting it).")
				.examples("on item spawn of iron sword:",
						"\tbroadcast \"Someone dropped an iron sword!\"")
				.since("<i>unknown</i> (before 2.1)");
		Skript.registerEvent("Drop", EvtItem.class, CollectionUtils.array(PlayerDropItemEvent.class, EntityDropItemEvent.class),
				"[player|1:entity] drop[ping] [[of] %-itemtypes%]")
				.description("Called when a player drops an item from their inventory, or an entity drops an item, such as a chicken laying an egg.")
				.examples("on drop:",
						"\tif event-item is compass:",
						"\t\tcancel event",
						"",
						"on entity drop of an egg:",
						"\tif event-entity is a chicken:",
						"\t\tset item of event-dropped item to a diamond")
				.since("<i>unknown</i> (before 2.1), 2.7 (entity)");
		if (hasPrepareCraftEvent) { // Must be loaded before CraftItemEvent
			Skript.registerEvent("Prepare Craft", EvtItem.class, PrepareItemCraftEvent.class, "[player] (preparing|beginning) craft[ing] [[of] %-itemtypes%]")
					.description("Called just before displaying crafting result to player. Note that setting the result item might or might not work due to Bukkit bugs.")
					.examples("on preparing craft of torch:")
					.since("2.2-Fixes-V10");
		}
		// TODO limit to InventoryAction.PICKUP_* and similar (e.g. COLLECT_TO_CURSOR)
		Skript.registerEvent("Craft", EvtItem.class, CraftItemEvent.class, "[player] craft[ing] [[of] %-itemtypes%]")
				.description("Called when a player crafts an item.")
				.examples("on craft:")
				.since("<i>unknown</i> (before 2.1)");
		if (hasEntityPickupItemEvent) {
			Skript.registerEvent("Pick Up", EvtItem.class, CollectionUtils.array(PlayerPickupItemEvent.class, EntityPickupItemEvent.class),
					"[(player|1¦entity)] (pick[ ]up|picking up) [[of] %-itemtypes%]")
				.description("Called when a player/entity picks up an item. Please note that the item is still on the ground when this event is called.")
				.examples("on pick up:", "on entity pickup of wheat:")
				.since("<i>unknown</i> (before 2.1), 2.5 (entity)")
				.keywords("pickup");
		} else {
			Skript.registerEvent("Pick Up", EvtItem.class, PlayerPickupItemEvent.class, "[player] (pick[ ]up|picking up) [[of] %-itemtypes%]")
				.description("Called when a player picks up an item. Please note that the item is still on the ground when this event is called.")
				.examples("on pick up:")
				.since("<i>unknown</i> (before 2.1)");
		}
		// TODO brew event
//		Skript.registerEvent("Brew", EvtItem.class, BrewEvent.class, "brew[ing] [[of] %itemtypes%]")
//				.description("Called when a potion finished brewing.")
//				.examples("on brew:")
//				.since("2.0");
		if (hasConsumeEvent) {
			Skript.registerEvent("Consume", EvtItem.class, PlayerItemConsumeEvent.class, "[player] ((eat|drink)[ing]|consum(e|ing)) [[of] %-itemtypes%]")
					.description("Called when a player is done eating/drinking something, e.g. an apple, bread, meat, milk or a potion.")
					.examples("on consume:")
					.since("2.0");
		}
		Skript.registerEvent("Inventory Click", EvtItem.class, InventoryClickEvent.class, "[player] inventory(-| )click[ing] [[at] %-itemtypes%]")
				.description("Called when clicking on inventory slot.")
				.examples("on inventory click:",
						"\tif event-item is stone:",
						"\t\tgive player 1 stone",
						"\t\tremove 20$ from player's balance")
				.since("2.2-Fixes-V10");
		Skript.registerEvent("Item Despawn", EvtItem.class, ItemDespawnEvent.class, "(item[ ][stack]|[item] %-itemtypes%) despawn[ing]", "[item[ ][stack]] despawn[ing] [[of] %-itemtypes%]")
				.description("Called when an item is about to be despawned from the world, usually 5 minutes after it was dropped.")
				.examples("on item despawn of diamond:",
					 	"	send \"Not my precious!\"",
					 	"	cancel event")
				.since("2.2-dev35");
		Skript.registerEvent("Item Merge", EvtItem.class, ItemMergeEvent.class, "(item[ ][stack]|[item] %-itemtypes%) merg(e|ing)", "item[ ][stack] merg(e|ing) [[of] %-itemtypes%]")
				.description("Called when dropped items merge into a single stack. event-entity will be the entity which is trying to merge, " +
						"and future event-entity will be the entity which is being merged into.")
				.examples("on item merge of gold blocks:",
					 	"	cancel event")
				.since("2.2-dev35");
		Skript.registerEvent("Inventory Item Move", SimpleEvent.class, InventoryMoveItemEvent.class,	 
                        "inventory item (move|transport)",
		                "inventory (mov(e|ing)|transport[ing]) [an] item")
				.description(
						"Called when an entity or block (e.g. hopper) tries to move items directly from one inventory to another.",
						"When this event is called, the initiator may have already removed the item from the source inventory and is ready to move it into the destination inventory.",
						"If this event is cancelled, the items will be returned to the source inventory."
				)
				.examples(
						"on inventory item move:",
							"\tbroadcast \"%holder of past event-inventory% is transporting %event-item% to %holder of event-inventory%!\""
				)
				.since("2.8.0");
		if (HAS_PLAYER_STONECUTTER_RECIPE_SELECT_EVENT) {
			Skript.registerEvent("Stonecutter Recipe Select", EvtItem.class, PlayerStonecutterRecipeSelectEvent.class, "stonecutting [[of] %-itemtypes%]")
					.description("Called when a player selects a recipe in a stonecutter.")
					.examples(
							"on stonecutting stone slabs",
								"\tcancel the event",
							"",
							"on stonecutting:",
								"\tbroadcast \"%player% is using stonecutter to craft %event-item%!\""
					)
					.since("2.8.0")
					.requiredPlugins("Paper 1.16+");
		}
	}
	
	@Nullable
	private Literal<ItemType> types;
	private boolean entity;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(final Literal<?>[] args, final int matchedPattern, final ParseResult parser) {
		types = (Literal<ItemType>) args[0];
		entity = parser.mark == 1;
		return true;
	}

	@Override
	@SuppressWarnings("null")
	public boolean check(final Event event) {
		if (event instanceof ItemSpawnEvent) // To make 'last dropped item' possible.
			EffSecSpawn.lastSpawned = ((ItemSpawnEvent) event).getEntity();
		if (hasEntityPickupItemEvent && ((!entity && event instanceof EntityPickupItemEvent) || (entity && event instanceof PlayerPickupItemEvent)))
			return false;
		if ((!entity && event instanceof EntityDropItemEvent) || (entity && event instanceof PlayerDropItemEvent))
			return false;
		if (types == null)
			return true;
		final ItemStack itemStack;
		if (event instanceof BlockDispenseEvent) {
			itemStack = ((BlockDispenseEvent) event).getItem();
		} else if (event instanceof ItemSpawnEvent) {
			itemStack = ((ItemSpawnEvent) event).getEntity().getItemStack();
		} else if (event instanceof PlayerDropItemEvent) {
			itemStack = ((PlayerDropItemEvent) event).getItemDrop().getItemStack();
		} else if (event instanceof EntityDropItemEvent) {
			itemStack = ((EntityDropItemEvent) event).getItemDrop().getItemStack();
		} else if (event instanceof CraftItemEvent) {
			itemStack = ((CraftItemEvent) event).getRecipe().getResult();
		} else if (hasPrepareCraftEvent && event instanceof PrepareItemCraftEvent) {
			Recipe recipe = ((PrepareItemCraftEvent) event).getRecipe();
			if (recipe != null) {
				itemStack = recipe.getResult();
			} else {
				return false;
			}
		} else if (HAS_PLAYER_STONECUTTER_RECIPE_SELECT_EVENT && event instanceof PlayerStonecutterRecipeSelectEvent) {
			itemStack = ((PlayerStonecutterRecipeSelectEvent) event).getStonecuttingRecipe().getResult();
		} else if (event instanceof EntityPickupItemEvent) {
			itemStack = ((EntityPickupItemEvent) event).getItem().getItemStack();
		} else if (event instanceof PlayerPickupItemEvent) {
			itemStack = ((PlayerPickupItemEvent) event).getItem().getItemStack();
		} else if (hasConsumeEvent && event instanceof PlayerItemConsumeEvent) {
			itemStack = ((PlayerItemConsumeEvent) event).getItem();
//		} else if (e instanceof BrewEvent)
//			itemStack = ((BrewEvent) e).getContents().getContents()
		} else if (event instanceof InventoryClickEvent) {
			itemStack = ((InventoryClickEvent) event).getCurrentItem();
		} else if (event instanceof ItemDespawnEvent) {
			itemStack = ((ItemDespawnEvent) event).getEntity().getItemStack();
		} else if (event instanceof ItemMergeEvent) {
			itemStack = ((ItemMergeEvent) event).getTarget().getItemStack();
		} else if (event instanceof InventoryMoveItemEvent) {
			itemStack = ((InventoryMoveItemEvent) event).getItem();
		} else {
			assert false;
			return false;
		}
		if (itemStack == null)
			return false;
		return types.check(event, itemType -> itemType.isOfType(itemStack));
	}
	
	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "dispense/spawn/drop/craft/pickup/consume/break/despawn/merge/move/stonecutting" + (types == null ? "" : " of " + types);
	}
	
}
