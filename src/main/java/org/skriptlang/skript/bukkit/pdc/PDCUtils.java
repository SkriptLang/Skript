package org.skriptlang.skript.bukkit.pdc;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.util.slot.Slot;
import io.papermc.paper.persistence.PersistentDataContainerView;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;

import java.util.function.Consumer;

/**
 * Utilities for working with {@link PersistentDataContainer}s.
 */
public final class PDCUtils {

	/**
	 * Gets the data container of an object.
	 * Use {@link #editPersistentDataContainer(Object, Consumer)} if editing is desired.
	 * @param holder Source of the container
	 * @return The container, or null if the holder doesn't have one.
	 */
	public static PersistentDataContainerView getPersistentDataContainer(Object holder) {
		return getPersistentDataContainer(holder, container -> {});
	}

	/**
	 * Gets the data container of an object.
	 * Use {@link #editPersistentDataContainer(Object, Consumer)} if editing is desired.
	 * @param holder Source of the container
	 * @param consumer Code to run with the container
	 * @return The container, or null if the holder doesn't have one.
	 */
	public static PersistentDataContainerView getPersistentDataContainer(Object holder, Consumer<PersistentDataContainerView> consumer) {
		var container = switch (holder) {
			case PersistentDataHolder dataHolder -> dataHolder.getPersistentDataContainer();
			case ItemType itemType -> itemType.getItemMeta().getPersistentDataContainer();
			case ItemStack itemStack -> {
				if (!itemStack.hasItemMeta())
					yield null;
				yield itemStack.getPersistentDataContainer();
			}
			case Slot slot -> {
				var item = slot.getItem();
				if (item == null || !item.hasItemMeta())
					yield null;
				yield item.getPersistentDataContainer();
			}
			case Block block when block.getState() instanceof TileState tileState -> tileState.getPersistentDataContainer();
			case null, default -> null;
		};
		if (container == null)
			return null;
		consumer.accept(container);
		return container;
	}

	/**
	 * Helper to easily edit PDCs.
	 * @param holder The holder of the PDC.
	 * @param consumer The method to run to edit the PDC.
	 */
	public static void editPersistentDataContainer(Object holder, Consumer<PersistentDataContainer> consumer) {
		switch (holder) {
			case PersistentDataHolder dataHolder -> consumer.accept(dataHolder.getPersistentDataContainer());
			case ItemType itemType -> {
				var meta = itemType.getItemMeta();
				consumer.accept(meta.getPersistentDataContainer());
				itemType.setItemMeta(meta);
			}
			case ItemStack itemStack -> {
				if (!itemStack.hasItemMeta())
					return;
				itemStack.editPersistentDataContainer(consumer);
			}
			case Slot slot -> {
				var item = slot.getItem();
				if (item == null || !item.hasItemMeta())
					return;
				item.editPersistentDataContainer(consumer);
				slot.setItem(item);
			}
			case Block block when block.getState() instanceof TileState tileState -> {
				consumer.accept(tileState.getPersistentDataContainer());
				tileState.update();
			}
			case null, default -> {
			}
		}
	}
}
