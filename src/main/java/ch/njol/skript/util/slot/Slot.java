package ch.njol.skript.util.slot;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.lang.Debuggable;

/**
 * Represents a container for a single item. It could be an ordinary inventory
 * slot or perhaps an item frame.
 */
public abstract class Slot implements Debuggable {
	
	protected Slot() {}
	
	@Nullable
	public abstract ItemStack getItem();
	
	public abstract void setItem(final @Nullable ItemStack item);
	
	public abstract int getAmount();
	
	public abstract void setAmount(int amount);
	
	@Override
	public final String toString() {
		return toString(null, false);
	}
	
	/**
	 * Checks if given slot is in same position with this.
	 * Ignores slot contents.
	 * @param o Another slot
	 * @return True if positions equal, false otherwise.
	 */
	public abstract boolean isSameSlot(Slot o);
}
