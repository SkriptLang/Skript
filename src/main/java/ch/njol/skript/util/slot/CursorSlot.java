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
package ch.njol.skript.util.slot;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.bukkitutil.PlayerUtils;
import ch.njol.skript.registrations.Classes;

/**
 * Item that represents a player's inventory cursor.
 */
public class CursorSlot extends Slot {

	/**
	 * Represents the cursor as it was used in an InventoryClickEvent.
	 */
	@Nullable
	private final ItemStack eventItemStack;
	private final Player player;

	public CursorSlot(Player player) {
		this(player, null);
	}

	/**
	 * Represents the cursor as it was used in an InventoryClickEvent.
	 * Should use this constructor if the event was an InventoryClickEvent.
	 * 
	 * @param player The player that this cursor slot belongs to.
	 * @param eventItemStack The ItemStack from {@link InventoryClickEvent#getCursor()} if event is an InventoryClickEvent.
	 */
	public CursorSlot(Player player, @Nullable ItemStack eventItemStack) {
		this.eventItemStack = eventItemStack;
		this.player = player;
	}

	public Player getPlayer() {
		return player;
	}

	@Override
	@Nullable
	public ItemStack getItem() {
		if (eventItemStack != null)
			return eventItemStack;
		return player.getItemOnCursor();
	}

	@Override
	public void setItem(@Nullable ItemStack item) {
		player.setItemOnCursor(item);
		PlayerUtils.updateInventory(player);
	}

	@Override
	public int getAmount() {
		return getItem().getAmount();
	}

	@Override
	public void setAmount(int amount) {
		getItem().setAmount(amount);
	}

	public boolean isInventoryClick() {
		return eventItemStack != null;
	}

	@Override
	public boolean isSameSlot(Slot slot) {
		if (!(slot instanceof CursorSlot))
			return false;
		CursorSlot cursor = (CursorSlot) slot;
		return cursor.getPlayer().equals(this.player) && cursor.isInventoryClick() == isInventoryClick();
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "cursor slot of " + Classes.toString(player);
	}

}
