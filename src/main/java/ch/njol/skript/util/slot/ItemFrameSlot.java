package ch.njol.skript.util.slot;

import org.bukkit.entity.ItemFrame;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.registrations.Classes;

/**
 * Represents contents of an item frame.
 */
public class ItemFrameSlot extends Slot {
	
	private ItemFrame frame;
	
	public ItemFrameSlot(ItemFrame frame) {
		this.frame = frame;
	}

	@Override
	@Nullable
	public ItemStack getItem() {
		return frame.getItem();
	}

	@Override
	public void setItem(@Nullable ItemStack item) {
		frame.setItem(item);
	}
	
	@Override
	public int getAmount() {
		return 1;
	}
	
	@Override
	public void setAmount(int amount) {}
	
	@Override
	public boolean isSameSlot(Slot o) {
		if (o instanceof ItemFrameSlot) // Same item frame
			return ((ItemFrameSlot) o).frame.equals(frame);
		return false;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return Classes.toString(getItem());
	}
	
}
