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
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.bukkitutil.PlayerUtils;
import ch.njol.skript.registrations.Classes;

/**
 * Item that is in player's cursor.
 */
public class CursorSlot extends Slot {
	
	private final Player player;
	
	public CursorSlot(Player p) {
		this.player = p;
	}
	
	public Player getPlayer() {
		return player;
	}
	
	@Override
	@Nullable
	public ItemStack getItem() {
		return player.getItemOnCursor();
	}

	@Override
	public void setItem(@Nullable ItemStack item) {
		player.setItemOnCursor(item);
		PlayerUtils.updateInventory(player);
	}
	
	@Override
	public int getAmount() {
		return player.getItemOnCursor().getAmount();
	}
	
	@Override
	public void setAmount(int amount) {
		player.getItemOnCursor().setAmount(amount);
	}
	
	@Override
	public boolean isSameSlot(Slot o) {
		if (!(o instanceof CursorSlot))
			return false;
		return ((CursorSlot) o).getPlayer().equals(this.player);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "cursor slot of " + Classes.toString(player);
	}
	
}
