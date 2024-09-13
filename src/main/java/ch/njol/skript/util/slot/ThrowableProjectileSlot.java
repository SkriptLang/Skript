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

import org.bukkit.Material;
import org.bukkit.entity.ThrowableProjectile;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.registrations.Classes;

/**
 * Represents the displayed item of a throwable projectile.
 */
public class ThrowableProjectileSlot extends Slot {
	
	private ThrowableProjectile projectile;
	
	public ThrowableProjectileSlot(ThrowableProjectile projectile) {
		this.projectile = projectile;
	}
	
	@Override
	public ItemStack getItem() {
		return projectile.getItem();
	}
	
	@Override
	public void setItem(@Nullable ItemStack item) {
		projectile.setItem(item != null ? item : new ItemStack(Material.AIR));
	}
	
	@Override
	public int getAmount() {
		return 1;
	}
	
	@Override
	public void setAmount(int amount) {}
	
	@Override
	public boolean isSameSlot(Slot o) {
		return o instanceof ThrowableProjectileSlot && ((ThrowableProjectileSlot) o).projectile.equals(projectile);
	}
	
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return Classes.toString(getItem());
	}
	
}
