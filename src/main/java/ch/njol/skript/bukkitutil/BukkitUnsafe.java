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
package ch.njol.skript.bukkitutil;

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.UnsafeValues;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.Nullable;

import com.google.common.io.ByteStreams;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import ch.njol.util.EnumTypeAdapter;
import ch.njol.skript.Skript;
import ch.njol.skript.util.Version;

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
	 * 1.9 Spigot has some "fun" bugs.
	 */
	private static final boolean knownNullPtr = !Skript.isRunningMinecraft(1, 11);
	
	static {
		if (unsafe == null)
			throw new Error("UnsafeValues are not available.");
	}
	
	/**
	 * Before 1.13, Vanilla material names were translated using
	 * this + a lookup table.
	 */
	@Nullable
	private static MethodHandle unsafeFromInternalNameMethod;
	
	private static final boolean newMaterials = Skript.isRunningMinecraft(1, 13);
	
	/**
	 * Vanilla material names to Bukkit materials.
	 */
	@Nullable
	private static Map<String,Material> materialMap;
	
	/**
	 * If we have material map for this version, using it is preferred.
	 * Otherwise, it can be used as fallback.
	 */
	private static boolean preferMaterialMap = true;
	
	/**
	 * We only spit one exception (unless debugging) from UnsafeValues. Some
	 * users might not care, and find 1.12 material mappings accurate enough.
	 */
	private static boolean unsafeValuesErrored;
	
	/**
	 * Maps pre 1.12 ids to materials for variable conversions.
	 */
	@Nullable
	private static Map<Integer,Material> idMappings;
	
	public static void initialize() {
		if (!newMaterials) {
			MethodHandle mh;
			try {
				mh = MethodHandles.lookup().findVirtual(UnsafeValues.class,
						"getMaterialFromInternalName", MethodType.methodType(Material.class, String.class));
			} catch (NoSuchMethodException | IllegalAccessException e) {
				mh = null;
			}
			unsafeFromInternalNameMethod = mh;
			
			try {
				Version version = Skript.getMinecraftVersion();
				boolean mapExists = loadMaterialMap("materials/" + version.getMajor() + "." +  version.getMinor() + ".json");
				if (!mapExists) {
					loadMaterialMap("materials/1.9.json"); // 1.9 is oldest we have mappings for
					preferMaterialMap = false;
					Skript.warning("Material mappings for " + version + " are not available.");
					Skript.warning("Depending on your server software, some aliases may not work.");
				}
			} catch (IOException e) {
				Skript.exception(e, "Failed to load material mappings. Aliases may not work properly.");
			}
		}
	}
	
	@Nullable
	public static Material getMaterialFromMinecraftId(String id) {
		if (newMaterials) {
			// On 1.13, Vanilla and Spigot names are same
			if (id.length() > 9)
				return Material.matchMaterial(id.substring(10)); // Strip 'minecraft:' out
			else // Malformed material name
				return null;
		} else {
			// If we have correct material map, prefer using it
			if (preferMaterialMap) {
				if (id.length() > 9) {
					assert materialMap != null;
					return materialMap.get(id.substring(10)); // Strip 'minecraft:' out
				}
			}
			
			// Otherwise, hacks
			Material type = null;
			try {
				assert unsafeFromInternalNameMethod != null;
				type = (Material) unsafeFromInternalNameMethod.invokeExact(unsafe, id);
			} catch (Throwable e) {
				// Only spit out an error once unless debugging
				if (!unsafeValuesErrored || Skript.debug()) {
					Skript.exception(e, "UnsafeValues failed to get material from Vanilla id");
					unsafeValuesErrored = true;
				}
			}
			if (type == null || type == Material.AIR) { // If there is no item form, UnsafeValues won't work
				// So we're going to rely on 1.12's material mappings
				assert materialMap != null;
				return materialMap.get(id);
			}
			return type;
		}
	}
	
	private static boolean loadMaterialMap(String name) throws IOException {
		try (InputStream is = Skript.getInstance().getResource(name)) {
			if (is == null) { // No mappings for this Minecraft version
				return false;
			}
			String data = new String(ByteStreams.toByteArray(is), StandardCharsets.UTF_8);
			
			Type type = new TypeToken<Map<String,Material>>(){}.getType();
			materialMap = new GsonBuilder().
				registerTypeAdapterFactory(EnumTypeAdapter.factory)
				.create().fromJson(data, type);
		}
		
		return true;
	}
	
	public static void modifyItemStack(ItemStack stack, String arguments) {
		if (unsafe == null)
			throw new IllegalStateException("modifyItemStack could not be performed as UnsafeValues are not available.");
		assert unsafe != null;
		try {
			unsafe.modifyItemStack(stack, arguments);
		} catch (NullPointerException e) {
			if (knownNullPtr) { // Probably known Spigot bug
				// So we continue doing whatever we were doing and hope it works
				Skript.warning("Item " + stack.getType() + arguments + " failed modifyItemStack. This is a bug on old Spigot versions.");
			} else { // Not known null pointer, don't just swallow
				throw e;
			}
		}
	}
	
	private static void initIdMappings() {
		try (InputStream is = Skript.getInstance().getResource("materials/ids.json")) {
			if (is == null) {
				throw new AssertionError("missing id mappings");
			}
			String data = new String(ByteStreams.toByteArray(is), StandardCharsets.UTF_8);
			
			Type type = new TypeToken<Map<Integer,String>>(){}.getType();
			Map<Integer, String> rawMappings = new GsonBuilder().
				registerTypeAdapterFactory(EnumTypeAdapter.factory)
				.create().fromJson(data, type);
			
			// Process raw mappings
			Map<Integer, Material> parsed = new HashMap<>(rawMappings.size());
			if (newMaterials) { // Legacy material conversion API
				for (Map.Entry<Integer, String> entry : rawMappings.entrySet()) {
					parsed.put(entry.getKey(), Material.matchMaterial(entry.getValue(), true));
				}
			} else { // Just enum API
				for (Map.Entry<Integer, String> entry : rawMappings.entrySet()) {
					parsed.put(entry.getKey(), Material.valueOf(entry.getValue()));
				}
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
