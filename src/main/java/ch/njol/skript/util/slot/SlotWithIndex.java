package ch.njol.skript.util.slot;

/**
 * Represents a slot which has index.
 */
public abstract class SlotWithIndex extends Slot {
	
	/**
	 * Gets an index of this slot.
	 * @return Index of the slot.
	 */
	public abstract int getIndex();

	/**
	 * Gets the raw index of this slot.
	 * @return Raw index of the slot.
	 */
	public int getRawIndex() {
		return getIndex();
	}
	
	@Override
	public boolean isSameSlot(Slot o) {
		if (o instanceof SlotWithIndex) {
			return getIndex() == ((SlotWithIndex) o).getIndex();
		}
		return false;
	}
}
