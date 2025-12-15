package ch.njol.skript.bukkitutil;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.registry.RegistryClassInfo;
import ch.njol.skript.util.PaperUtils;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import io.papermc.paper.ban.BanListType;
import org.bukkit.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;

import java.net.InetAddress;
import java.net.UnknownHostException;

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

	/**
	 * Gets a {@link RegistryClassInfo} by checking if the {@link Class} from {@code classPath} exists
	 * and {@link Registry} or {@link io.papermc.paper.registry.RegistryKey} contains {@code registryName}.
	 * @param classPath The {@link String} representation of the desired {@link Class}.
	 * @param registryName The {@link String} representation of the desired {@link Registry}.
	 * @param codeName The name used in patterns.
	 * @param languageNode The language node of the type.
	 * @return {@link RegistryClassInfo} if the class and registry exists, otherwise {@code null}.
	 */
	public static <R extends Keyed> @Nullable RegistryClassInfo<?> getRegistryClassInfo(
		String classPath,
		String registryName,
		String codeName,
		String languageNode
	) {
		if (!Skript.classExists(classPath))
			return null;
		Registry<R> registry = null;
		if (BukkitUtils.registryExists(registryName)) {
			try {
				//noinspection unchecked
				registry = (Registry<R>) Registry.class.getField(registryName).get(null);
			} catch (NoSuchFieldException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		} else if (PaperUtils.registryExists(registryName)) {
			registry = PaperUtils.getBukkitRegistry(registryName);
		}
		if (registry != null) {
			Class<R> registryClass;
			try {
				//noinspection unchecked
				registryClass = (Class<R>) Class.forName(classPath);
			} catch (ClassNotFoundException e) {
				Skript.debug("Could not retrieve the class with the path: '" + classPath + "'.");
				throw new RuntimeException(e);
			}
			return new RegistryClassInfo<>(registryClass, registry, codeName, languageNode);
		}
		Skript.debug("There were no registries found for '" + registryName + "'.");
		return null;
	}

	/**
	 * Returns the BanEntry of a player.
	 *
	 * @param target The string IP or OfflinePlayer
	 * @return The BanEntry, or null
	 */
	public static BanEntry<?> getBanEntry(Object target) {
		if (target instanceof String ipTarget) {
			return getBanEntry(ipTarget);
		} else if (target instanceof OfflinePlayer playerTarget) {
			return getBanEntry(playerTarget);
		}

		return null;
	}

	private static BanEntry<InetAddress> getBanEntry(String ipTarget) {
		try {
			InetAddress address = InetAddress.getByName(ipTarget);
			return Bukkit.getBanList(BanListType.IP).getBanEntry(address);
		} catch (UnknownHostException ignored) {} // this only happens when you pass a url and it performs a lookup
		return null;
	}

	private static BanEntry<PlayerProfile> getBanEntry(OfflinePlayer playerTarget) {
		return Bukkit.getBanList(BanListType.PROFILE).getBanEntry(playerTarget.getPlayerProfile());
	}

}
