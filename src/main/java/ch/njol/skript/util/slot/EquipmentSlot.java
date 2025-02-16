package ch.njol.skript.util.slot;

import ch.njol.skript.bukkitutil.PlayerUtils;
import ch.njol.skript.registrations.Classes;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/**
 * Represents equipment slot of an entity.
 */
public class EquipmentSlot extends SlotWithIndex {
	
	public enum EquipSlot {
		TOOL {
			@Override
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
			public @Nullable ItemStack get(EntityEquipment equipment) {
				return equipment.getHelmet();
			}
			
			@Override
			public void set(EntityEquipment equipment, @Nullable ItemStack item) {
				equipment.setHelmet(item);
			}

		},
		CHESTPLATE(38) {
			@Override
			public @Nullable ItemStack get(EntityEquipment equipment) {
				return equipment.getChestplate();
			}
			
			@Override
			public void set(EntityEquipment equipment, @Nullable ItemStack item) {
				equipment.setChestplate(item);
			}

		},
		LEGGINGS(37) {
			@Override
			public @Nullable ItemStack get(EntityEquipment equipment) {
				return equipment.getLeggings();
			}
			
			@Override
			public void set(EntityEquipment equipment, @Nullable ItemStack item) {
				equipment.setLeggings(item);
			}

		},
		BOOTS(36) {
			@Override
			public @Nullable ItemStack get(EntityEquipment equipment) {
				return equipment.getBoots();
			}
			
			@Override
			public void set(EntityEquipment equipment, @Nullable ItemStack item) {
				equipment.setBoots(item);
			}

		},

		BODY() {
			@Override
			public ItemStack get(EntityEquipment equipment) {
				return equipment.getItem(org.bukkit.inventory.EquipmentSlot.BODY);
			}

			@Override
			public void set(EntityEquipment equipment, @Nullable ItemStack item) {
				equipment.setItem(org.bukkit.inventory.EquipmentSlot.BODY, item);
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
		public abstract ItemStack get(EntityEquipment e);
		
		public abstract void set(EntityEquipment e, @Nullable ItemStack item);
		
	}
	
	private static final EquipSlot[] values = EquipSlot.values();
	
	private final EntityEquipment e;
	private final EquipSlot slot;
	private final int slotIndex;
	private final boolean slotToString;
	
	public EquipmentSlot(final EntityEquipment e, final EquipSlot slot, final boolean slotToString) {
		this.e = e;
		int slotIndex = -1;
		if (slot == EquipSlot.TOOL) {
			Entity holder = e.getHolder();
			if (holder instanceof Player)
				slotIndex = ((Player) holder).getInventory().getHeldItemSlot();
		}
		this.slotIndex = slotIndex;
		this.slot = slot;
		this.slotToString = slotToString;
	}
	
	public EquipmentSlot(final EntityEquipment e, final EquipSlot slot) {
		this(e, slot, false);
	}
	
	@SuppressWarnings("null")
	public EquipmentSlot(HumanEntity holder, int index) {
		/*
		 * slot: 6 entries in EquipSlot, indices descending
		 *  So this math trick gets us the EquipSlot from inventory slot index
		 * slotToString: Referring to numeric slot id, right?
		 */
		this(holder.getEquipment(), values[41 - index], true);
	}

	@Override
	@Nullable
	public ItemStack getItem() {
		return slot.get(e);
	}
	
	@Override
	public void setItem(final @Nullable ItemStack item) {
		slot.set(e, item);
		if (e.getHolder() instanceof Player)
			PlayerUtils.updateInventory((Player) e.getHolder());
	}
	
	@Override
	public int getAmount() {
		ItemStack item = slot.get(e);
		return item != null ? item.getAmount() : 0;
	}
	
	@Override
	public void setAmount(int amount) {
		ItemStack item = slot.get(e);
		if (item != null)
			item.setAmount(amount);
		slot.set(e, item);
	}
	
	/**
	 * Gets underlying armor slot enum.
	 * @return Armor slot.
	 */
	public EquipSlot getEquipSlot() {
		return slot;
	}

	@Override
	public int getIndex() {
		// use specific slotIndex if available
		return slotIndex != -1 ? slotIndex : slot.slotNumber;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (slotToString) // Slot to string
			return "the " + slot.name().toLowerCase(Locale.ENGLISH) + " of " + Classes.toString(e.getHolder()); // TODO localise?
		else // Contents of slot to string
			return Classes.toString(getItem());
	}
	
}
