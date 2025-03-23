package ch.njol.skript.util.slot;

import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.bukkitutil.PlayerUtils;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.BlockInventoryHolder;

/**
 * Represents a slot in some inventory.
 */
public class InventorySlot extends SlotWithIndex {

	private final Inventory inventory;
	private final int index;
	private final int rawIndex;

	public InventorySlot(Inventory inventory, int index, int rawIndex) {
		assert inventory != null;
		assert index >= 0;
		this.inventory = inventory;
		this.index = index;
		this.rawIndex = rawIndex;
	}

	public InventorySlot(Inventory inventory, int index) {
		assert inventory != null;
		assert index >= 0;
		this.inventory = inventory;
		this.index = rawIndex = index;
	}

	public Inventory getInventory() {
		return inventory;
	}

	@Override
	public int getIndex() {
		return index;
	}

	@Override
	public int getRawIndex() {
		return rawIndex;
	}

	@Override
	public @Nullable ItemStack getItem() {
		if (index == -999) //Non-existent slot, e.g. Outside GUI
			return null;
		ItemStack item = inventory.getItem(index);
		return item == null  ? new ItemStack(Material.AIR, 1) : item.clone();
	}

	@Override
	public void setItem(final @Nullable ItemStack item) {
		inventory.setItem(index, item != null && item.getType() != Material.AIR ? item : null);
		if (inventory instanceof PlayerInventory)
			PlayerUtils.updateInventory((Player) inventory.getHolder());
	}

	@Override
	public int getAmount() {
		ItemStack item = inventory.getItem(index);
		return item != null ? item.getAmount() : 0;
	}

	@Override
	public void setAmount(int amount) {
		ItemStack item = inventory.getItem(index);
		if (item != null)
			item.setAmount(amount);
		if (inventory instanceof PlayerInventory)
			PlayerUtils.updateInventory((Player) inventory.getHolder());
	}

	@Override
	public boolean isSameSlot(Slot slot) {
		if (slot instanceof InventorySlot inventorySlot) {
			return inventorySlot.getInventory().equals(inventory) && inventorySlot.getIndex() == index;
		}
		return super.equals(slot);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		InventoryHolder holder = inventory.getHolder();

		if (holder instanceof BlockState)
			holder = new BlockInventoryHolder((BlockState) holder);

		if (holder != null) {
			if (inventory instanceof CraftingInventory) // 4x4 crafting grid is contained in player too!
				return "crafting slot " + index + " of " + Classes.toString(holder);

			return "inventory slot " + index + " of " + Classes.toString(holder);
		}
		return "inventory slot " + index + " of " + Classes.toString(inventory);
	}

}
