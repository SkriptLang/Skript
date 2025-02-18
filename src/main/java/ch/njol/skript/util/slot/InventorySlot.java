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

	private final Inventory invi;
	private final int index;
	private final int rawIndex;

	public InventorySlot(Inventory invi, int index, int rawIndex) {
		assert invi != null;
		assert index >= 0;
		this.invi = invi;
		this.index = index;
		this.rawIndex = rawIndex;
	}

	public InventorySlot(Inventory invi, int index) {
		assert invi != null;
		assert index >= 0;
		this.invi = invi;
		this.index = rawIndex = index;
	}

	public Inventory getInventory() {
		return invi;
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
	@Nullable
	public ItemStack getItem() {
		if (index == -999) //Non-existent slot, e.g. Outside GUI
			return null;
		ItemStack item = invi.getItem(index);
		return item == null  ? new ItemStack(Material.AIR, 1) : item.clone();
	}

	@Override
	public void setItem(final @Nullable ItemStack item) {
		invi.setItem(index, item != null && item.getType() != Material.AIR ? item : null);
		if (invi instanceof PlayerInventory)
			PlayerUtils.updateInventory((Player) invi.getHolder());
	}

	@Override
	public int getAmount() {
		ItemStack item = invi.getItem(index);
		return item != null ? item.getAmount() : 0;
	}

	@Override
	public void setAmount(int amount) {
		ItemStack item = invi.getItem(index);
		if (item != null)
			item.setAmount(amount);
		if (invi instanceof PlayerInventory)
			PlayerUtils.updateInventory((Player) invi.getHolder());
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		InventoryHolder holder = invi != null ? invi.getHolder() : null;

		if (holder instanceof BlockState)
			holder = new BlockInventoryHolder((BlockState) holder);

		if (holder != null) {
			if (invi instanceof CraftingInventory) // 4x4 crafting grid is contained in player too!
				return "crafting slot " + index + " of " + Classes.toString(holder);

			return "inventory slot " + index + " of " + Classes.toString(holder);
		}
		return "inventory slot " + index + " of " + Classes.toString(invi);
	}

}
