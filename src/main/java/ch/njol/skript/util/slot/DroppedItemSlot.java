package ch.njol.skript.util.slot;

import org.bukkit.entity.Item;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.registrations.Classes;

/**
 * Represents an item of dropped item entity.
 */
public class DroppedItemSlot extends Slot {

	private Item entity;

	public DroppedItemSlot(Item item) {
		this.entity = item;
	}

	@Override
	public @Nullable ItemStack getItem() {
		return entity.getItemStack();
	}

	@Override
	public void setItem(@Nullable ItemStack item) {
		assert item != null;
		entity.setItemStack(item);
	}

	@Override
	public int getAmount() {
		return entity.getItemStack().getAmount();
	}

	@Override
	public void setAmount(int amount) {
		entity.getItemStack().setAmount(amount);
	}

	@Override
	public boolean isSameSlot(Slot o) {
		return this.equals(o);
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof DroppedItemSlot slot && slot.entity.equals(entity);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return Classes.toString(getItem());
	}

}
