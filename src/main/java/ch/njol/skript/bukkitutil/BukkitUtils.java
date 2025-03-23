package ch.njol.skript.bukkitutil;

import ch.njol.skript.Skript;
import org.bukkit.Registry;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class with methods pertaining to Bukkit API
 */
public class BukkitUtils {

	private static final Map<EquipmentSlot, Integer> BUKKIT_EQUIPMENT_INDICES = new HashMap<>();
	private static final Map<Integer, EquipmentSlot> BUKKIT_EQUIPMENT_INDICES_REVERSED = new HashMap<>();

	static {
		BUKKIT_EQUIPMENT_INDICES.put(EquipmentSlot.FEET, 36);
		BUKKIT_EQUIPMENT_INDICES.put(EquipmentSlot.LEGS, 37);
		BUKKIT_EQUIPMENT_INDICES.put(EquipmentSlot.CHEST, 38);
		BUKKIT_EQUIPMENT_INDICES.put(EquipmentSlot.HEAD, 39);
		BUKKIT_EQUIPMENT_INDICES.put(EquipmentSlot.OFF_HAND, 40);

		BUKKIT_EQUIPMENT_INDICES_REVERSED.put(36, EquipmentSlot.FEET);
		BUKKIT_EQUIPMENT_INDICES_REVERSED.put(37, EquipmentSlot.LEGS);
		BUKKIT_EQUIPMENT_INDICES_REVERSED.put(38, EquipmentSlot.CHEST);
		BUKKIT_EQUIPMENT_INDICES_REVERSED.put(39, EquipmentSlot.HEAD);
		BUKKIT_EQUIPMENT_INDICES_REVERSED.put(40, EquipmentSlot.OFF_HAND);
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
		return  BUKKIT_EQUIPMENT_INDICES.get(equipmentSlot);
	}

	/**
	 * Get the {@link EquipmentSlot} represented by the inventory slot index
	 * @param slotIndex The index of the equipment slot
	 * @return The equipment slot the provided slot index, otherwise null if invalid
	 */
	public static EquipmentSlot getEquipmentSlotFromIndex(Integer slotIndex) {
		return  BUKKIT_EQUIPMENT_INDICES_REVERSED.get(slotIndex);
	}

}
