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
 *
 * Copyright 2011-2017 Peter Güttinger and contributors
 */
package ch.njol.skript.expressions;

import java.lang.reflect.Array;
import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.slot.Slot;
import ch.njol.skript.util.slot.InventorySlot;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

@Name("Clicked Block/Entity/Inventory/Slot")
@Description("The clicked block, entity, inventory slot, inventory, type or action.")
@Examples({"message \"You clicked on a %type of clicked entity%!\"",
		"clicked block is a chest:",
		"	show the inventory of the clicked block to the player"})
@Since("1.0, 2.2-dev35 (more clickable things)")
@Events({"click", "inventory click"})
public class ExprClicked extends SimpleExpression<Object> {

	private static enum ClickableType {
		
		BLOCK_AND_ITEMS(1, Block.class, "clicked block/itemtype/entity", " clicked (block|%-*itemtype/entitydata%)"),
		SLOT(2, Slot.class, "clicked slot", "clicked slot"),
		INVENTORY(3, Inventory.class, "clicked inventory", "clicked inventory"),
		TYPE(4, ClickType.class, "click type", "click (type|action)"),
		ACTION(5, InventoryAction.class, "inventory action", "inventory action");
		
		private String name, syntax;
		private Class<?> c;
		private int value;

		private ClickableType(int value, Class<?> c, String name, String syntax) {
			this.syntax = syntax;
			this.value = value;
			this.c = c;
			this.name = name;
		}
		
		public int getValue() {
			return value;
		}
		
		public Class<?> getClickableClass() {
			return c;
		}
		
		public String getName() {
			return name;
		}
		
		public String getSyntax(boolean last) {
			return value + "¦" + syntax + (!last ? "|" : "");
		}
		
		public static ClickableType getClickable(int num) {
			for (ClickableType clickable : ClickableType.values())
				if (clickable.getValue() == num) return clickable;
			return BLOCK_AND_ITEMS;
		}
	}
	
	static {
		Skript.registerExpression(ExprClicked.class, Object.class, ExpressionType.SIMPLE, "[the] ("
					+ ClickableType.BLOCK_AND_ITEMS.getSyntax(false)
					+ ClickableType.SLOT.getSyntax(false)
					+ ClickableType.INVENTORY.getSyntax(false)
					+ ClickableType.TYPE.getSyntax(false)
					+ ClickableType.ACTION.getSyntax(true) + ")");
	}
	
	@Nullable
	private EntityData<?> entityType;
	@Nullable
	private ItemType itemType; //null results in any itemtype
	private ClickableType clickable = ClickableType.BLOCK_AND_ITEMS;
	private boolean rawSlot = false;
	
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		clickable = ClickableType.getClickable(parseResult.mark);
		switch (clickable) {
			case BLOCK_AND_ITEMS:
				final Object type = exprs[0] == null ? null : ((Literal<?>) exprs[0]).getSingle();
				if (type instanceof EntityData) {
					entityType = (EntityData<?>) type;
					if (!ScriptLoader.isCurrentEvent(PlayerInteractEntityEvent.class) && !ScriptLoader.isCurrentEvent(PlayerInteractAtEntityEvent.class)) {
						Skript.error("The expression 'clicked entity' may only be used in a click event", ErrorQuality.SEMANTIC_ERROR);
						return false;
					}
				} else {
					itemType = (ItemType) type;
					if (!ScriptLoader.isCurrentEvent(PlayerInteractEvent.class)) {
						Skript.error("The expression 'clicked block' may only be used in a click event", ErrorQuality.SEMANTIC_ERROR);
						return false;
					}
				}
				break;
			case INVENTORY:
			case ACTION:
			case TYPE:
			case SLOT:
				if (!ScriptLoader.isCurrentEvent(InventoryClickEvent.class)) {
					Skript.error("The expression '" + clickable.getName() + "' may only be used in an inventory click event", ErrorQuality.SEMANTIC_ERROR);
					return false;
				}
				break;
		}
		return true;
	}
	
	@Override
	public boolean isSingle() {
		return true;
	}
	
	@Override
	public Class<? extends Object> getReturnType() {
		return (clickable != ClickableType.BLOCK_AND_ITEMS) ? clickable.getClickableClass() : entityType != null ? entityType.getType() : Block.class;
	}
	
	@SuppressWarnings("null")
	@Override
	@Nullable
	protected Object[] get(final Event e) {
		switch (clickable) {
			case BLOCK_AND_ITEMS:
				if (e instanceof PlayerInteractEvent) {
					if (entityType != null) //This is suppose to be null as this event should be for blocks
						return null;
					final Block block = ((PlayerInteractEvent) e).getClickedBlock();
					return (itemType == null || itemType.isOfType(block)) ? new Block[] {block} : null;
				} else if (e instanceof PlayerInteractEntityEvent) {
					if (entityType == null) //We're testing for the entity in this event
						return null;
					final Entity entity = ((PlayerInteractEntityEvent) e).getRightClicked();
					if (entityType.isInstance(entity)) {
						final Entity[] one = (Entity[]) Array.newInstance(entityType.getType(), 1);
						one[0] = entity;
						return one;
					}
					return null;
				}
				break;
			case TYPE:
				return new ClickType[] {((InventoryClickEvent) e).getClick()};
			case ACTION:
				return new InventoryAction[] {((InventoryClickEvent) e).getAction()};
			case INVENTORY:
				return new Inventory[] {((InventoryClickEvent) e).getClickedInventory()};
			case SLOT:
				// Slots are specific to inventories, so refering to wrong one is impossible
				// (as opposed to using the numbers directly)
				return CollectionUtils.array(new InventorySlot(((InventoryClickEvent) e).getClickedInventory(), ((InventoryClickEvent) e).getSlot()));
		}
		return null;
	}
	
	@Override
	@Nullable
	public Object[] beforeChange(@Nullable Object[] delta) {
		if (delta == null) // Nothing to nothing
			return null;
		Object first = delta[0];
		if (first == null) // ConvertedExpression might cause this
			return null;
		
		// Slots must be transformed to item stacks
		// Documentation by Njol states so, plus it is convenient
		if (first instanceof Slot) {
			return new ItemStack[] {((Slot) first).getItem()};
		}
		
		// Everything else (inventories, actions, etc.) does not need special handling
		return delta;
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "the " + (clickable != ClickableType.BLOCK_AND_ITEMS ? clickable.getName() : "clicked " + (entityType != null ? entityType : itemType != null ? itemType : "block"));
	}
	
}
