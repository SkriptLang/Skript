package ch.njol.skript.util.slot;

import ch.njol.skript.registrations.Classes;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class DisplayEntitySlot extends Slot {

	private ItemDisplay display;

	public DisplayEntitySlot(ItemDisplay display) {
		this.display = display;
	}

	@Override
	public @Nullable ItemStack getItem() {
		return display.getItemStack();
	}

	@Override
	public void setItem(@Nullable ItemStack item) {
		display.setItemStack(item);
	}

	@Override
	public int getAmount() {
		return 1;
	}

	@Override
	public void setAmount(int amount) {}

	@Override
	public boolean isSameSlot(Slot other) {
		return this.equals(other);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof DisplayEntitySlot) // Same display
			return ((DisplayEntitySlot) obj).display.equals(display);
		return false;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return Classes.toString(getItem());
	}

}
