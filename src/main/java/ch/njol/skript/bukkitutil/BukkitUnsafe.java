package ch.njol.skript.bukkitutil;

import ch.njol.skript.Skript;
import ch.njol.util.EnumTypeAdapter;
import com.google.common.io.ByteStreams;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.UnsafeValues;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Contains helpers for Bukkit's not so safe stuff.
 */
@SuppressWarnings("deprecation")
public class BukkitUnsafe {

	/**
	 * Bukkit's UnsafeValues allows us to do stuff that would otherwise
	 * require NMS. It has existed for a long time, too, so 1.9 support is
	 * not particularly hard to achieve.
	 *
	 * UnsafeValues' existence and behavior is not guaranteed across future versions.
	 */
	@Nullable
	private static final UnsafeValues unsafe = Bukkit.getUnsafe();

	/**
	 * Maps pre 1.12 ids to materials for variable conversions.
	 */
	private static @Nullable Map<Integer, Material> idMappings;

	static {
		if (unsafe == null)
			throw new Error("UnsafeValues are not available.");
	}

	/**
	 * Get a material from a minecraft id.
	 *
	 * @param id Namespaced ID with or without a namespace. IDs without a namespace will be treated
	 * 		as minecraft namespaced IDs. ('minecraft:dirt' and 'dirt' are equivalent.)
	 * @return The Material which the id represents, or null if no material can be matched.
	 * @deprecated Prefer {@link BukkitUnsafe#getMaterialFromNamespacedId(String)} for including modded item support
	 */
	@Deprecated
	public static @Nullable Material getMaterialFromMinecraftId(String id) {
		return getMaterialFromNamespacedId(id);
	}

	/**
	 * Get a material from a namespaced ID.
	 * For example, 'minecraft:iron_ingot' -> Material.IRON_INGOT; 'mod:an_item' -> Material.MOD_AN_ITEM
	 *
	 * @param id Namespaced ID with or without a namespace. IDs without a namespace will be treated
	 * 		as minecraft namespaced IDs. ('minecraft:dirt' and 'dirt' are equivalent.)
	 * @return The Material which the id represents, or null if no material can be matched.
	 */
	public static @Nullable Material getMaterialFromNamespacedId(String id) {
		return Material.matchMaterial(id.toLowerCase().startsWith(NamespacedKey.MINECRAFT + ":")
										  ? id
										  : id.replace(":", "_")  //For Hybrid Server
		);
	}

	public static void modifyItemStack(ItemStack stack, String arguments) {
		if (unsafe == null)
			throw new IllegalStateException("modifyItemStack could not be performed as UnsafeValues are not available.");
		unsafe.modifyItemStack(stack, arguments);
	}

	private static void initIdMappings() {
		try (InputStream is = Skript.getInstance().getResource("materials/ids.json")) {
			if (is == null) {
				throw new AssertionError("missing id mappings");
			}
			String data = new String(ByteStreams.toByteArray(is), StandardCharsets.UTF_8);

			Type type = new TypeToken<Map<Integer, String>>() {
			}.getType();
			Map<Integer, String> rawMappings = new GsonBuilder().
												   registerTypeAdapterFactory(EnumTypeAdapter.factory)
												   .create().fromJson(data, type);

			// Process raw mappings
			Map<Integer, Material> parsed = new HashMap<>(rawMappings.size());
			// Legacy material conversion API
			for (Map.Entry<Integer, String> entry : rawMappings.entrySet()) {
				parsed.put(entry.getKey(), Material.matchMaterial(entry.getValue(), true));
			}
			idMappings = parsed;
		} catch (IOException e) {
			throw new AssertionError(e);
		}
	}

	@Nullable
	public static Material getMaterialFromId(int id) {
		if (idMappings == null) {
			initIdMappings();
		}
		assert idMappings != null;
		return idMappings.get(id);
	}
}
