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

import org.bukkit.entity.ItemDisplay;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.registrations.Classes;

public class DisplayEntitySlot extends Slot {
	
	private ItemDisplay display;
	
	public DisplayEntitySlot(ItemDisplay display) {
		this.display = display;
	}

	@Override
	@Nullable
	public ItemStack getItem() {
		return display.getItemStack();
	}

	@Override
	public void setItem(@Nullable ItemStack item) {
		display.setItemStack(item);
	}

	@Override
	public int getAmount() {
		return 1;
	}

	@Override
	public void setAmount(int amount) {}

	@Override
	public boolean isSameSlot(Slot other) {
		if (other instanceof DisplayEntitySlot) // Same display
			return ((DisplayEntitySlot) other).display.equals(display);
		return false;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return Classes.toString(getItem());
	}

}
