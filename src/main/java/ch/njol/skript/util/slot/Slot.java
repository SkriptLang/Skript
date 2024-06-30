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

import ch.njol.skript.aliases.Aliases;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.lang.util.common.AnyAmount;
import ch.njol.skript.lang.util.common.AnyNamed;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.lang.Debuggable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

/**
 * Represents a container for a single item. It could be an ordinary inventory
 * slot or perhaps an item frame.
 */
public abstract class Slot implements Debuggable, AnyNamed, AnyAmount {

	protected Slot() {}

	@Nullable
	public abstract ItemStack getItem();

	public abstract void setItem(final @Nullable ItemStack item);

	public abstract int getAmount();

	public abstract void setAmount(int amount);

	@Override
	public final String toString() {
		return toString(null, false);
	}

	/**
	 * Checks if given slot is in same position with this.
	 * Ignores slot contents.
	 * @param o Another slot
	 * @return True if positions equal, false otherwise.
	 */
	public abstract boolean isSameSlot(Slot o);

	/**
	 * @return The name of the item in this slot
	 */
	@Override
	public @UnknownNullability String name() {
		ItemStack stack = this.getItem();
		if (stack != null && stack.hasItemMeta()) {
			ItemMeta meta = stack.getItemMeta();
			return meta.hasDisplayName() ? meta.getDisplayName() : null;
		}
		return null;
	}

	@Override
	public boolean nameSupportsChange() {
		return true;
	}

	/**
	 * @param name The name to change
	 */
	@Override
	public void setName(String name) {
		ItemStack stack = this.getItem();
		if (stack != null && !Aliases.javaItemType("air").isOfType(stack)) {
			ItemMeta meta = stack.hasItemMeta() ? stack.getItemMeta() : Bukkit.getItemFactory().getItemMeta(stack.getType());
			meta.setDisplayName(name);
			stack.setItemMeta(meta);
			this.setItem(stack);
		}
	}

	@Override
	public @NotNull Number amount() {
		return this.getAmount();
	}

	@Override
	public boolean amountSupportsChange() {
		return true;
	}

	@Override
	public void setAmount(@Nullable Number amount) throws UnsupportedOperationException {
		this.setAmount(amount != null ? amount.intValue() : 0);
	}

}
