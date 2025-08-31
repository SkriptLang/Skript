package ch.njol.skript.util.slot;

import ch.njol.skript.lang.Debuggable;
import ch.njol.skript.lang.util.common.AnyAmount;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a container for a single item. It could be an ordinary inventory
 * slot or perhaps an item frame.
 */
public abstract class Slot implements Debuggable, AnyAmount {

	protected Slot() {}

	public abstract @Nullable ItemStack getItem();

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

	@Override
	public @NotNull Number amount() {
		return this.getAmount();
	}

	@Override
	public boolean supportsAmountChange() {
		return true;
	}

	@Override
	public void setAmount(@Nullable Number amount) {
		this.setAmount(amount != null ? amount.intValue() : 0);
	}

}
