/**
 * This file is part of Skript.
 * <p>
 * Skript is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Skript is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * <p>
 * Copyright 2011-2017 Peter Güttinger and contributors
 */
package ch.njol.skript.util.slot;

import ch.njol.skript.registrations.Classes;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Represents contents of an item frame.
 */
public class ItemFrameSlot extends Slot {

	private final ItemFrame frame;

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
