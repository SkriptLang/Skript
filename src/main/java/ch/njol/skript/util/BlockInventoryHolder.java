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
package ch.njol.skript.util;

import org.bukkit.block.BlockState;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

/**
 * Main usage is {@link ch.njol.skript.expressions.ExprInventoryInfo}
 * This class allows Skript to return a block while being able to recognize it as {@link InventoryHolder},
 * You may only use this class if a expression's return type is an {@link InventoryHolder}.
 */
public class BlockInventoryHolder extends BlockStateBlock implements InventoryHolder {
	
	public BlockInventoryHolder(BlockState state) {
		super(state, false);
	}
	
	@Override
	public @NotNull Inventory getInventory() {
		return ((InventoryHolder) state).getInventory();
	}
}
