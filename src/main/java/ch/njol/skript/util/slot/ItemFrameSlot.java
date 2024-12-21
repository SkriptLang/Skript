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

import org.bukkit.entity.ItemFrame;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.registrations.Classes;

/**
 * Represents contents of an item frame.
 */
public class ItemFrameSlot extends Slot {
	
	private ItemFrame frame;
	
	public ItemFrameSlot(ItemFrame frame) {
		this.frame = frame;
	}

	@Override
	@Nullable
	public ItemStack getItem() {
		return frame.getItem();
	}

	@Override
	public void setItem(@Nullable ItemStack item) {
		frame.setItem(item);
	}
	
	@Override
	public int getAmount() {
		return 1;
	}
	
	@Override
	public void setAmount(int amount) {}
	
	@Override
	public boolean isSameSlot(Slot o) {
		if (o instanceof ItemFrameSlot) // Same item frame
			return ((ItemFrameSlot) o).frame.equals(frame);
		return false;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return Classes.toString(getItem());
	}
	
}
