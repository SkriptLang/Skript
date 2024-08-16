/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
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
