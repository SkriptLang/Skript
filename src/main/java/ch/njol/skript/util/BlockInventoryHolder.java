package ch.njol.skript.util;

import org.bukkit.block.BlockState;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

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
	public Inventory getInventory() {
		return ((InventoryHolder) state).getInventory();
	}
}
