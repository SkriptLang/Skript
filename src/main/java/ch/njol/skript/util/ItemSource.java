package ch.njol.skript.util;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.bukkitutil.ItemUtils;
import ch.njol.skript.util.slot.Slot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

/**
 * Container class for containing the origin of a {@link Slot}, {@link ItemStack}, and {@link ItemType}.
 */
public class ItemSource<T> {

	private final T source;

	public ItemSource(T source) {
		this.source = source;
	}

	/**
	 * Checks if the {@link ItemStack} in {@code slot} is a valid item.
	 * @param slot The {@link Slot} to check.
	 * @return {@link ItemSource} if the item is valid, otherwise {@code null}.
	 */
	public static @Nullable ItemSource<Slot> fromSlot(Slot slot) {
		ItemStack itemStack = slot.getItem();
		if (itemStack == null || ItemUtils.isAir(itemStack.getType()) || itemStack.getItemMeta() == null)
			return null;
		return new ItemSource<>(slot);
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
	 * Get the {@link ItemMeta} retrieved from {@link #source}.
	 */
	public ItemMeta getItemMeta() {
		return getItemStack().getItemMeta();
	}

	/**
	 * Appropriately update the {@link ItemMeta} of {@link #source}.
	 * @param itemMeta The {@link ItemMeta} to update {@link #source}
	 */
	public void setItemMeta(ItemMeta itemMeta) {
		ItemUtils.setItemMeta(source, itemMeta);
	}

}
