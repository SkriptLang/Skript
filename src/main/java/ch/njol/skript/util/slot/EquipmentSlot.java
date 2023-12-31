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

import java.util.Locale;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.ApiStatus.ScheduledForRemoval;

import ch.njol.skript.bukkitutil.PlayerUtils;
import ch.njol.skript.registrations.Classes;

/**
 * Represents equipment slot of an entity.
 */
public class EquipmentSlot extends SlotWithIndex {

	/**
	 * Enum to handle the setting of the equipment slot.
	 * 
	 * @deprecated The multiple naming of the same enums is confusing.
	 * Spigot now has an exact enum for handling the exact same Equipment Slots.
	 * We're planning to remove this enum and only use Spigot's EquipmentSlot.
	 */
	@Deprecated
	@ScheduledForRemoval
	// Developers note: These set and get methods should be implemented in a switch inside the EquipmentSlot#setItem method or make the enum private.
	public enum EquipSlot {
		TOOL {
			@Override
			@Nullable
			public ItemStack get(EntityEquipment equipment) {
				return equipment.getItemInMainHand();
			}

			@Override
			public void set(EntityEquipment equipment, @Nullable ItemStack item) {
				equipment.setItemInMainHand(item);
			}
		},
		OFF_HAND(40) {
			@Override
			@Nullable
			public ItemStack get(EntityEquipment equipment) {
				return equipment.getItemInOffHand();
			}

			@Override
			public void set(EntityEquipment equipment, @Nullable ItemStack item) {
				equipment.setItemInOffHand(item);
			}
		},
		HELMET(39) {
			@Override
			@Nullable
			public ItemStack get(EntityEquipment equipment) {
				return equipment.getHelmet();
			}

			@Override
			public void set(EntityEquipment equipment, @Nullable ItemStack item) {
				equipment.setHelmet(item);
			}
		},
		CHESTPLATE(38) {
			@Override
			@Nullable
			public ItemStack get(EntityEquipment equipment) {
				return equipment.getChestplate();
			}

			@Override
			public void set(EntityEquipment equipment, @Nullable ItemStack item) {
				equipment.setChestplate(item);
			}
		},
		LEGGINGS(37) {
			@Override
			@Nullable
			public ItemStack get(EntityEquipment equipment) {
				return equipment.getLeggings();
			}

			@Override
			public void set(EntityEquipment equipment, @Nullable ItemStack item) {
				equipment.setLeggings(item);
			}
		},
		BOOTS(36) {
			@Override
			@Nullable
			public ItemStack get(EntityEquipment equipment) {
				return equipment.getBoots();
			}

			@Override
			public void set(EntityEquipment equipment, @Nullable ItemStack item) {
				equipment.setBoots(item);
			}
		};

		public final int slotNumber;

		EquipSlot() {
			slotNumber = -1;
		}

		EquipSlot(int number) {
			slotNumber = number;
		}

		@Nullable
		public abstract ItemStack get(EntityEquipment equipment);

		public abstract void set(EntityEquipment equipment, @Nullable ItemStack item);

		@Nullable
		public static EquipSlot fromBukkit(org.bukkit.inventory.EquipmentSlot slot) {
			switch (slot) {
				case CHEST:
					return EquipSlot.CHESTPLATE;
				case FEET:
					return EquipSlot.BOOTS;
				case HAND:
					return EquipSlot.TOOL;
				case HEAD:
					return EquipSlot.HELMET;
				case LEGS:
					return EquipSlot.LEGGINGS;
				case OFF_HAND:
					return EquipSlot.OFF_HAND;
				default:
					assert false;
					return null;
			}
		}

	}

	private static final EquipSlot[] values = EquipSlot.values();

	private final EntityEquipment equipment;
	private final EquipSlot slot;
	private final boolean debug;

	/**
	 * Represents a slot of an equipment slot on an entity.
	 * 
	 * @param equipment The object that represents the entity with the equipment.
	 * @param slot The slot to manipulate or get.
	 */
	public EquipmentSlot(EntityEquipment equipment, org.bukkit.inventory.EquipmentSlot slot) {
		this(equipment, slot, false);
	}

	/**
	 * Represents a slot of an equipment slot on an entity.
	 * 
	 * @param equipment The object that represents the entity with the equipment.
	 * @param slot The slot to manipulate or get.
	 * @param debug If the slot should have extra details when printing the toString
	 */
	public EquipmentSlot(EntityEquipment equipment, org.bukkit.inventory.EquipmentSlot slot, boolean debug) {
		this.slot = EquipSlot.fromBukkit(slot);
		this.equipment = equipment;
		this.debug = debug;
	}

	/**
	 * Use {@link #EquipmentSlot(EntityEquipment, org.bukkit.inventory.EquipmentSlot, boolean)}
	 */
	@Deprecated
	public EquipmentSlot(EntityEquipment equipment, EquipSlot slot, boolean slotToString) {
		this.equipment = equipment;
		this.slot = slot;
		this.debug = slotToString;
	}

	/**
	 * Use {@link #EquipmentSlot(EntityEquipment, org.bukkit.inventory.EquipmentSlot)}
	 */
	@Deprecated
	public EquipmentSlot(EntityEquipment equipment, EquipSlot slot) {
		this(equipment, slot, false);
	}

	public EquipmentSlot(HumanEntity holder, int index) {
		this.equipment = holder.getEquipment();
		this.slot = values[41 - index]; // 6 entries in EquipSlot, indices descending
		// So this math trick gets us the EquipSlot from inventory slot index
		this.debug = true; // Referring to numeric slot id, right?
	}

	@Override
	@Nullable
	public ItemStack getItem() {
		if (slot == null)
			return null;
		return slot.get(equipment);
	}

	@Override
	public void setItem(@Nullable ItemStack item) {
		if (slot == null)
			return;
		slot.set(equipment, item);
		if (equipment.getHolder() instanceof Player)
			PlayerUtils.updateInventory((Player) equipment.getHolder());
	}

	@Override
	public int getAmount() {
		if (slot == null)
			return 0;
		ItemStack item = slot.get(equipment);
		return item != null ? item.getAmount() : 0;
	}

	@Override
	public void setAmount(int amount) {
		if (slot == null)
			return;
		ItemStack item = slot.get(equipment);
		if (item != null)
			item.setAmount(amount);
		slot.set(equipment, item);
	}

	/**
	 * Gets underlying armor slot enum.
	 * @return Armor slot.
	 */
	@Nullable
	public EquipSlot getEquipSlot() {
		return slot;
	}

	@Override
	public int getIndex() {
		if (slot == null)
			return -1;
		return slot.slotNumber;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (slot == null)
			return "unknown equipment slot";
		if (this.debug) // Slot to string
			return "the " + slot.name().toLowerCase(Locale.ENGLISH) + " of " + Classes.toString(equipment.getHolder()); // TODO localise?
		else // Contents of slot to string
			return Classes.toString(getItem());
	}

}
