package ch.njol.skript.util.slot;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.bukkitutil.PlayerUtils;
import ch.njol.skript.registrations.Classes;

/**
 * Item that represents a player's inventory cursor.
 */
public class CursorSlot extends Slot {

	/**
	 * Represents the cursor as it was used in an InventoryClickEvent.
	 */
	@Nullable
	private final ItemStack eventItemStack;
	private final Player player;

	public CursorSlot(Player player) {
		this(player, null);
	}

	/**
	 * Represents the cursor as it was used in an InventoryClickEvent.
	 * Should use this constructor if the event was an InventoryClickEvent.
	 * 
	 * @param player The player that this cursor slot belongs to.
	 * @param eventItemStack The ItemStack from {@link InventoryClickEvent#getCursor()} if event is an InventoryClickEvent.
	 */
	public CursorSlot(Player player, @Nullable ItemStack eventItemStack) {
		this.eventItemStack = eventItemStack;
		this.player = player;
	}

	public Player getPlayer() {
		return player;
	}

	@Override
	@Nullable
	public ItemStack getItem() {
		if (eventItemStack != null)
			return eventItemStack;
		return player.getItemOnCursor();
	}

	@Override
	public void setItem(@Nullable ItemStack item) {
		player.setItemOnCursor(item);
		PlayerUtils.updateInventory(player);
	}

	@Override
	public int getAmount() {
		return getItem().getAmount();
	}

	@Override
	public void setAmount(int amount) {
		getItem().setAmount(amount);
	}

	public boolean isInventoryClick() {
		return eventItemStack != null;
	}

	@Override
	public boolean isSameSlot(Slot slot) {
		if (!(slot instanceof CursorSlot))
			return false;
		CursorSlot cursor = (CursorSlot) slot;
		return cursor.getPlayer().equals(this.player) && cursor.isInventoryClick() == isInventoryClick();
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "cursor slot of " + Classes.toString(player);
	}

}
