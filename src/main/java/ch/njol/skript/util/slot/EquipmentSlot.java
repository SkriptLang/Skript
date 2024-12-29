package ch.njol.skript.util.slot;

import java.util.Locale;

import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.bukkitutil.PlayerUtils;
import ch.njol.skript.registrations.Classes;

/**
 * Represents equipment slot of an entity.
 */
public class EquipmentSlot extends SlotWithIndex {
	
	public enum EquipSlot {
		TOOL {
			@Override
			@Nullable
			public ItemStack get(final EntityEquipment e) {
				return e.getItemInMainHand();
			}

			@Override
			public void set(final EntityEquipment e, final @Nullable ItemStack item) {
				e.setItemInMainHand(item);
			}

			@Override
			public org.bukkit.inventory.EquipmentSlot getBukkitEquipSlot() {
				return org.bukkit.inventory.EquipmentSlot.HAND;
			}
		},
		OFF_HAND(40) {

			@Override
			@Nullable
			public ItemStack get(EntityEquipment e) {
				return e.getItemInOffHand();
			}

			@Override
			public void set(EntityEquipment e, @Nullable ItemStack item) {
				e.setItemInOffHand(item);
			}

			@Override
			public org.bukkit.inventory.EquipmentSlot getBukkitEquipSlot() {
				return org.bukkit.inventory.EquipmentSlot.OFF_HAND;
			}

		},
		HELMET(39) {
			@Override
			@Nullable
			public ItemStack get(final EntityEquipment e) {
				return e.getHelmet();
			}
			
			@Override
			public void set(final EntityEquipment e, final @Nullable ItemStack item) {
				e.setHelmet(item);
			}

			@Override
			public org.bukkit.inventory.EquipmentSlot getBukkitEquipSlot() {
				return org.bukkit.inventory.EquipmentSlot.HEAD;
			}
		},
		CHESTPLATE(38) {
			@Override
			@Nullable
			public ItemStack get(final EntityEquipment e) {
				return e.getChestplate();
			}
			
			@Override
			public void set(final EntityEquipment e, final @Nullable ItemStack item) {
				e.setChestplate(item);
			}

			@Override
			public org.bukkit.inventory.EquipmentSlot getBukkitEquipSlot() {
				return org.bukkit.inventory.EquipmentSlot.CHEST;
			}
		},
		LEGGINGS(37) {
			@Override
			@Nullable
			public ItemStack get(final EntityEquipment e) {
				return e.getLeggings();
			}
			
			@Override
			public void set(final EntityEquipment e, final @Nullable ItemStack item) {
				e.setLeggings(item);
			}

			@Override
			public org.bukkit.inventory.EquipmentSlot getBukkitEquipSlot() {
				return org.bukkit.inventory.EquipmentSlot.LEGS;
			}
		},
		BOOTS(36) {
			@Override
			@Nullable
			public ItemStack get(final EntityEquipment e) {
				return e.getBoots();
			}
			
			@Override
			public void set(final EntityEquipment e, final @Nullable ItemStack item) {
				e.setBoots(item);
			}

			@Override
			public org.bukkit.inventory.EquipmentSlot getBukkitEquipSlot() {
				return org.bukkit.inventory.EquipmentSlot.FEET;
			}
		},

		BODY() {
			@Override
			public @Nullable ItemStack get(EntityEquipment equipment) {
				return equipment.getItem(org.bukkit.inventory.EquipmentSlot.BODY);
			}

			@Override
			public void set(EntityEquipment equipment, @Nullable ItemStack item) {
				equipment.setItem(org.bukkit.inventory.EquipmentSlot.BODY, item);
			}

			@Override
			public org.bukkit.inventory.EquipmentSlot getBukkitEquipSlot() {
				return org.bukkit.inventory.EquipmentSlot.BODY;
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

		public abstract org.bukkit.inventory.EquipmentSlot getBukkitEquipSlot();

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

	public static EquipSlot convertToSkriptEquipSlot(org.bukkit.inventory.EquipmentSlot bukkitSlot) {
		return switch (bukkitSlot) {
			case HEAD -> EquipSlot.HELMET;
			case CHEST -> EquipSlot.CHESTPLATE;
			case LEGS -> EquipSlot.LEGGINGS;
			case FEET -> EquipSlot.BOOTS;
			case HAND -> EquipSlot.TOOL;
			case OFF_HAND -> EquipSlot.OFF_HAND;
			default -> null;
		};
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (slotToString) // Slot to string
			return "the " + slot.name().toLowerCase(Locale.ENGLISH) + " of " + Classes.toString(e.getHolder()); // TODO localise?
		else // Contents of slot to string
			return Classes.toString(getItem());
	}
	
}
