package ch.njol.skript.bukkitutil;

import ch.njol.skript.Skript;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.eclipse.jdt.annotation.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/**
 * Utilities for inventories.
 * In newer versions (1.21+), InventoryView is an interface instead of an abstract class
 * Directing calling InventoryView#getTitle on 1.20.6 and below results in an IncompatibleClassChangeError
 *  as an interface, not an abstract class, is expected.
 */
public class InventoryUtils {

	private static final @Nullable MethodHandle GET_TITLE;
	private static final @Nullable MethodHandle GET_INVENTORY;
	private static final @Nullable MethodHandle CONVERT_SLOT;
	private static final @Nullable MethodHandle GET_TOP_INVENTORY;
	private static final @Nullable MethodHandle GET_BOTTOM_INVENTORY;

	static {
		MethodHandle getTitle = null;
		MethodHandle getInventory = null;
		MethodHandle convertSlot = null;
		MethodHandle getTopInventory = null;
		MethodHandle getBottomInventory = null;
		if (!InventoryView.class.isInterface()) { // initialize legacy support as it's likely an abstract class
			try {
				MethodHandles.Lookup lookup = MethodHandles.lookup();
				getTitle = lookup.findVirtual(InventoryView.class, "getTitle", MethodType.methodType(String.class));
				getInventory = lookup.findVirtual(InventoryView.class, "getInventory", MethodType.methodType(Inventory.class, int.class));
				convertSlot = lookup.findVirtual(InventoryView.class, "convertSlot", MethodType.methodType(int.class, int.class));
				getTopInventory = lookup.findVirtual(InventoryView.class, "getTopInventory", MethodType.methodType(Inventory.class));
				getBottomInventory = lookup.findVirtual(InventoryView.class, "getBottomInventory", MethodType.methodType(Inventory.class));
			} catch (NoSuchMethodException | IllegalAccessException e) {
				Skript.exception(e, "Failed to load old inventory view support.");
			}
		}
		GET_TITLE = getTitle;
		GET_INVENTORY = getInventory;
		CONVERT_SLOT = convertSlot;
		GET_TOP_INVENTORY = getTopInventory;
		GET_BOTTOM_INVENTORY = getBottomInventory;
	}

	/**
	 * @see InventoryView#getTitle()
	 */
	public static @Nullable String getTitle(InventoryView inventoryView) {
		if (GET_TITLE == null)
			return inventoryView.getTitle();
		try {
			return (String) GET_TITLE.invoke(inventoryView);
		} catch (Throwable ignored) { }
		return null;
	}

	/**
	 * @see InventoryView#getInventory(int)
	 */
	public static @Nullable Inventory getInventory(InventoryView inventoryView, int rawSlot) {
		if (GET_INVENTORY == null)
			return inventoryView.getInventory(rawSlot);
		try {
			return (Inventory) GET_INVENTORY.invoke(inventoryView, rawSlot);
		} catch (Throwable ignored) { }
		return null;
	}

	/**
	 * @see InventoryView#convertSlot(int)
	 */
	public static @Nullable Integer convertSlot(InventoryView inventoryView, int rawSlot) {
		if (CONVERT_SLOT == null)
			return inventoryView.convertSlot(rawSlot);
		try {
			return (Integer) CONVERT_SLOT.invoke(inventoryView, rawSlot);
		} catch (Throwable ignored) { }
		return null;
	}

	/**
	 * @see InventoryView#getTopInventory()
	 */
	public static @Nullable Inventory getTopInventory(InventoryView inventoryView) {
		if (GET_TOP_INVENTORY == null)
			return inventoryView.getTopInventory();
		try {
			return (Inventory) GET_TOP_INVENTORY.invoke(inventoryView);
		} catch (Throwable ignored) { }
		return null;
	}

	/**
	 * @see InventoryView#getBottomInventory()
	 */
	public static @Nullable Inventory getBottomInventory(InventoryView inventoryView) {
		if (GET_BOTTOM_INVENTORY == null)
			return inventoryView.getBottomInventory();
		try {
			return (Inventory) GET_BOTTOM_INVENTORY.invoke(inventoryView);
		} catch (Throwable ignored) { }
		return null;
	}

}
