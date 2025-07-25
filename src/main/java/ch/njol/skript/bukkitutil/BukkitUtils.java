package ch.njol.skript.bukkitutil;

import ch.njol.skript.Skript;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.bukkit.Registry;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;

/**
 * Utility class with methods pertaining to Bukkit API
 */
public class BukkitUtils {

	private static final BiMap<EquipmentSlot, Integer> BUKKIT_EQUIPMENT_SLOT_INDICES = HashBiMap.create();

	static {
		BUKKIT_EQUIPMENT_SLOT_INDICES.put(EquipmentSlot.FEET, 36);
		BUKKIT_EQUIPMENT_SLOT_INDICES.put(EquipmentSlot.LEGS, 37);
		BUKKIT_EQUIPMENT_SLOT_INDICES.put(EquipmentSlot.CHEST, 38);
		BUKKIT_EQUIPMENT_SLOT_INDICES.put(EquipmentSlot.HEAD, 39);
		BUKKIT_EQUIPMENT_SLOT_INDICES.put(EquipmentSlot.OFF_HAND, 40);
	}

	/**
	 * Check if a registry exists
	 *
	 * @param registry Registry to check for (Fully qualified name of registry)
	 * @return True if registry exists else false
	 */
	public static boolean registryExists(String registry) {
		return Skript.classExists("org.bukkit.Registry") && Skript.fieldExists(Registry.class, registry);
	}

	/**
	 * Get an instance of the {@link PotionEffectType} {@link Registry}
	 * <p>Paper/Bukkit have 2 different names for the same registry.</p>
	 *
	 * @return PotionEffectType Registry
	 */
	@SuppressWarnings("NullableProblems")
	public static @Nullable Registry<PotionEffectType> getPotionEffectTypeRegistry() {
		if (registryExists("MOB_EFFECT")) { // Paper (1.21.4)
			return Registry.MOB_EFFECT;
		} else if (registryExists("EFFECT")) { // Bukkit (1.21.x)
			return Registry.EFFECT;
		}
		return null;
	}

	/**
	 * Get the inventory slot index of the {@link EquipmentSlot}
	 * @param equipmentSlot The equipment slot to get the index of
	 * @return The equipment slot index of the provided slot, otherwise null if invalid
	 */
	public static Integer getEquipmentSlotIndex(EquipmentSlot equipmentSlot) {
		return  BUKKIT_EQUIPMENT_SLOT_INDICES.get(equipmentSlot);
	}

	/**
	 * Get the {@link EquipmentSlot} represented by the inventory slot index
	 * @param slotIndex The index of the equipment slot
	 * @return The equipment slot the provided slot index, otherwise null if invalid
	 */
	public static EquipmentSlot getEquipmentSlotFromIndex(int slotIndex) {
		return BUKKIT_EQUIPMENT_SLOT_INDICES.inverse().get(slotIndex);
	}

}
