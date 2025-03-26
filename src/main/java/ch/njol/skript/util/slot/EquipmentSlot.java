package ch.njol.skript.util.slot;

import ch.njol.skript.bukkitutil.PlayerUtils;
import ch.njol.skript.registrations.Classes;
import com.google.common.base.Preconditions;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Represents equipment slot of an entity.
 */
public class EquipmentSlot extends SlotWithIndex {

	/**
	 * @deprecated Use {@link org.bukkit.inventory.EquipmentSlot}, {@link EntityEquipment} instead
	 */
	@Deprecated
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

	private static final org.bukkit.inventory.EquipmentSlot[] BUKKIT_VALUES = org.bukkit.inventory.EquipmentSlot.values();

	private static final Map<org.bukkit.inventory.EquipmentSlot, Integer> BUKKIT_SLOT_INDICES = new HashMap<>();

	static {
		BUKKIT_SLOT_INDICES.put(org.bukkit.inventory.EquipmentSlot.FEET, 36);
		BUKKIT_SLOT_INDICES.put(org.bukkit.inventory.EquipmentSlot.LEGS, 37);
		BUKKIT_SLOT_INDICES.put(org.bukkit.inventory.EquipmentSlot.CHEST, 38);
		BUKKIT_SLOT_INDICES.put(org.bukkit.inventory.EquipmentSlot.HEAD, 39);
		BUKKIT_SLOT_INDICES.put(org.bukkit.inventory.EquipmentSlot.OFF_HAND, 40);
	}
	
	private final EntityEquipment entityEquipment;
	private EquipSlot skriptSlot;
	private final int slotIndex;
	private final boolean slotToString;
	private org.bukkit.inventory.EquipmentSlot bukkitSlot;

	/**
	 * @deprecated Use {@link EquipmentSlot#EquipmentSlot(EntityEquipment, org.bukkit.inventory.EquipmentSlot, boolean)} instead
	 */
	@Deprecated
	public EquipmentSlot(@NotNull EntityEquipment entityEquipment, @NotNull EquipSlot skriptSlot, boolean slotToString) {
		Preconditions.checkNotNull(entityEquipment, "entityEquipment cannot be null");
		Preconditions.checkNotNull(skriptSlot, "skriptSlot cannot be null");
		this.entityEquipment = entityEquipment;
		int slotIndex = -1;
		if (skriptSlot == EquipSlot.TOOL) {
			Entity holder = entityEquipment.getHolder();
			if (holder instanceof Player player)
				slotIndex = player.getInventory().getHeldItemSlot();
		}
		this.slotIndex = slotIndex;
		this.skriptSlot = skriptSlot;
		this.slotToString = slotToString;
	}

	/**
	 * @deprecated Use {@link EquipmentSlot#EquipmentSlot(EntityEquipment, org.bukkit.inventory.EquipmentSlot)} instead
	 */
	@Deprecated
	public EquipmentSlot(@NotNull EntityEquipment entityEquipment, @NotNull EquipSlot skriptSlot) {
		this(entityEquipment, skriptSlot, false);
	}

	public EquipmentSlot(@NotNull EntityEquipment entityEquipment, @NotNull org.bukkit.inventory.EquipmentSlot bukkitSlot, boolean slotToString) {
		Preconditions.checkNotNull(entityEquipment, "entityEquipment cannot be null");
		Preconditions.checkNotNull(bukkitSlot, "bukkitSlot cannot be null");
		this.entityEquipment = entityEquipment;
		int slotIndex = -1;
		if (bukkitSlot == org.bukkit.inventory.EquipmentSlot.HAND) {
			Entity holder = entityEquipment.getHolder();
			if (holder instanceof Player player)
				slotIndex = player.getInventory().getHeldItemSlot();
		}
		this.slotIndex = slotIndex;
		this.bukkitSlot = bukkitSlot;
		this.slotToString = slotToString;
	}

	public EquipmentSlot(@NotNull EntityEquipment equipment, @NotNull org.bukkit.inventory.EquipmentSlot bukkitSlot) {
		this(equipment, bukkitSlot, false);
	}

	public EquipmentSlot(@NotNull HumanEntity holder, int index) {
		/*
		 * slot: 6 entries in EquipSlot, indices descending
		 *  So this math trick gets us the EquipSlot from inventory slot index
		 * slotToString: Referring to numeric slot id, right?
		 */
		//noinspection DataFlowIssue
		this(holder.getEquipment(), BUKKIT_VALUES[41 - index], true);
	}

	@Override
	public @Nullable ItemStack getItem() {
		if (skriptSlot != null)
			return skriptSlot.get(entityEquipment);
		return entityEquipment.getItem(bukkitSlot);
	}
	
	@Override
	public void setItem(@Nullable ItemStack item) {
		if (skriptSlot != null) {
			skriptSlot.set(entityEquipment, item);
		} else {
			entityEquipment.setItem(bukkitSlot, item);
		}
		if (entityEquipment.getHolder() instanceof Player player)
			PlayerUtils.updateInventory(player);
	}
	
	@Override
	public int getAmount() {
		ItemStack item = getItem();
		return item != null ? item.getAmount() : 0;
	}
	
	@Override
	public void setAmount(int amount) {
		ItemStack item = getItem();
		if (item != null)
			item.setAmount(amount);
		setItem(item);
	}
	
	/**
	 * @deprecated Use {@link EquipmentSlot#EquipmentSlot(EntityEquipment, org.bukkit.inventory.EquipmentSlot)} and {@link #getEquipmentSlot()}
	 */
	@Deprecated
	public EquipSlot getEquipSlot() {
		return skriptSlot;
	}

	/**
	 * Get the corresponding {@link org.bukkit.inventory.EquipmentSlot}
	 * @return
	 */
	public org.bukkit.inventory.EquipmentSlot getEquipmentSlot() {
		return bukkitSlot;
	}

	@Override
	public int getIndex() {
		// use specific slotIndex if available
		if (slotIndex != -1) {
			return slotIndex;
		} else if (skriptSlot != null) {
			return skriptSlot.slotNumber;
		} else if (BUKKIT_SLOT_INDICES.containsKey(bukkitSlot)) {
			return BUKKIT_SLOT_INDICES.get(bukkitSlot);
		}
		return -1;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (slotToString) {
			StringBuilder builder = new StringBuilder("the ");
			if (skriptSlot != null) {
				builder.append(skriptSlot.name().toLowerCase(Locale.ENGLISH));
			} else {
				builder.append(bukkitSlot.name().replace('_', ' ').toLowerCase(Locale.ENGLISH));
			}
			builder.append(" of ").append(Classes.toString(entityEquipment.getHolder()));
			return builder.toString();
		}
		return Classes.toString(getItem());
	}
	
}
