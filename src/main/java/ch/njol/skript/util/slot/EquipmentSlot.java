package ch.njol.skript.util.slot;

import ch.njol.skript.bukkitutil.PlayerUtils;
import ch.njol.skript.lang.SyntaxStringBuilder;
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
			public ItemStack get(final EntityEquipment entityEquipment) {
				return entityEquipment.getItemInMainHand();
			}

			@Override
			public void set(final EntityEquipment entityEquipment, final @Nullable ItemStack item) {
				entityEquipment.setItemInMainHand(item);
			}
		},
		OFF_HAND(40) {

			@Override
			public ItemStack get(EntityEquipment entityEquipment) {
				return entityEquipment.getItemInOffHand();
			}

			@Override
			public void set(EntityEquipment entityEquipment, @Nullable ItemStack item) {
				entityEquipment.setItemInOffHand(item);
			}
			
		},
		HELMET(39) {
			@Override
			public @Nullable ItemStack get(final EntityEquipment entityEquipment) {
				return entityEquipment.getHelmet();
			}
			
			@Override
			public void set(final EntityEquipment entityEquipment, final @Nullable ItemStack item) {
				entityEquipment.setHelmet(item);
			}
		},
		CHESTPLATE(38) {
			@Override
			public @Nullable ItemStack get(final EntityEquipment entityEquipment) {
				return entityEquipment.getChestplate();
			}
			
			@Override
			public void set(final EntityEquipment entityEquipment, final @Nullable ItemStack item) {
				entityEquipment.setChestplate(item);
			}
		},
		LEGGINGS(37) {
			@Override
			public @Nullable ItemStack get(final EntityEquipment entityEquipment) {
				return entityEquipment.getLeggings();
			}
			
			@Override
			public void set(final EntityEquipment entityEquipment, final @Nullable ItemStack item) {
				entityEquipment.setLeggings(item);
			}
		},
		BOOTS(36) {
			@Override
			public @Nullable ItemStack get(final EntityEquipment entityEquipment) {
				return entityEquipment.getBoots();
			}
			
			@Override
			public void set(final EntityEquipment entityEquipment, final @Nullable ItemStack item) {
				entityEquipment.setBoots(item);
			}
		},
		BODY() {
			@Override
			public ItemStack get(EntityEquipment entityEquipment) {
				return entityEquipment.getItem(org.bukkit.inventory.EquipmentSlot.BODY);
			}

			@Override
			public void set(EntityEquipment entityEquipment, @Nullable ItemStack item) {
				entityEquipment.setItem(org.bukkit.inventory.EquipmentSlot.BODY, item);
			}
		};
		
		public final int slotNumber;
		
		EquipSlot() {
			slotNumber = -1;
		}

		EquipSlot(int number) {
			slotNumber = number;
		}

		public abstract @Nullable ItemStack get(EntityEquipment entityEquipment);

		public abstract void set(EntityEquipment entityEquipment, @Nullable ItemStack item);

	}

	private static final Map<org.bukkit.inventory.EquipmentSlot, Integer> BUKKIT_SLOT_INDICES = new HashMap<>();
	private static final Map<Integer, org.bukkit.inventory.EquipmentSlot> BUKKIT_SLOT_INDICES_REVERSED = new HashMap<>();

	static {
		BUKKIT_SLOT_INDICES.put(org.bukkit.inventory.EquipmentSlot.FEET, 36);
		BUKKIT_SLOT_INDICES.put(org.bukkit.inventory.EquipmentSlot.LEGS, 37);
		BUKKIT_SLOT_INDICES.put(org.bukkit.inventory.EquipmentSlot.CHEST, 38);
		BUKKIT_SLOT_INDICES.put(org.bukkit.inventory.EquipmentSlot.HEAD, 39);
		BUKKIT_SLOT_INDICES.put(org.bukkit.inventory.EquipmentSlot.OFF_HAND, 40);
		BUKKIT_SLOT_INDICES_REVERSED.put(36, org.bukkit.inventory.EquipmentSlot.FEET);
		BUKKIT_SLOT_INDICES_REVERSED.put(37, org.bukkit.inventory.EquipmentSlot.LEGS);
		BUKKIT_SLOT_INDICES_REVERSED.put(38, org.bukkit.inventory.EquipmentSlot.CHEST);
		BUKKIT_SLOT_INDICES_REVERSED.put(39, org.bukkit.inventory.EquipmentSlot.HEAD);
		BUKKIT_SLOT_INDICES_REVERSED.put(40, org.bukkit.inventory.EquipmentSlot.OFF_HAND);
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
		this(holder.getEquipment(), BUKKIT_SLOT_INDICES_REVERSED.get(index), true);
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
			SyntaxStringBuilder syntaxBuilder = new SyntaxStringBuilder(event, debug);
			syntaxBuilder.append("the ");
			if (skriptSlot != null) {
				syntaxBuilder.append(skriptSlot.name().toLowerCase(Locale.ENGLISH));
			} else {
				syntaxBuilder.append(bukkitSlot.name().replace('_', ' ').toLowerCase(Locale.ENGLISH));
			}
			syntaxBuilder.append(" of ").append(Classes.toString(entityEquipment.getHolder()));
			return syntaxBuilder.toString();
		}
		return Classes.toString(getItem());
	}
	
}
