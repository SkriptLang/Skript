package ch.njol.skript.util;

import ch.njol.skript.bukkitutil.ItemUtils;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ch.njol.skript.util.slot.Slot;
import ch.njol.skript.aliases.ItemType;

/**
 * Container class for containing the origin of a {@link Slot}, {@link ItemStack}, and {@link ItemType}.
 */
public class ItemSource<T> {

	private final T source;

	public ItemSource(T source) {
		ItemStack itemStack = ItemUtils.asItemStack(source);
		if (itemStack == null)
			throw new IllegalArgumentException("Object was not a Slot, ItemType or ItemStack");
		this.source = source;
	}

	/**
	 * Get the source object, can be a {@link Slot}, {@link ItemStack}, or {@link ItemType}.
	 */
	public T getSource() {
		return source;
	}

	/**
	 * Get the {@link ItemStack} retrieved from {@link #source}.
	 */
	public ItemStack getItemStack() {
		return ItemUtils.asItemStack(source);
	}

	/**
	 * Appropriately update the {@link ItemMeta} of {@link #source}.
	 * @param itemMeta The {@link ItemMeta} to update {@link #source}
	 */
	public void setItemMeta(ItemMeta itemMeta) {
		ItemUtils.setItemMeta(source, itemMeta);
	}

}
